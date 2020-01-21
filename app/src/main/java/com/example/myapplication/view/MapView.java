package com.example.myapplication.view;

import com.example.myapplication.R;
import com.example.myapplication.classes.Route;
import com.example.myapplication.object.Line;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.Build;

import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;

import com.example.myapplication.classes.ReadFile;
import com.example.myapplication.object.Text;

import java.lang.String;

import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import static java.lang.Math.cos;


public class MapView extends View {
    private int scrWidth ;
    private int scrHeight ;
    public void zoomin() {
        mScale*=1.5;
        invalidate();
    }
    public void zoomout() {
        mScale/=1.5;
        invalidate();
    }

    public static int mHeight = 2000;
    public static int mWidth = 1580;
    Point currentCell;
    private float mlat = 13.32f;//lattitude of the center of the screen
    private float mlon = 109.43f;//longtitude of the center of the screen
    private float mScale = 2;// 1km = mScale*pixcels
    private int scrCtY,scrCtX;
    private ReadFile readFile ;
    int levelPreference =0;
    Point pointtemp;
    PointF dragStart,dragStop;
    PointF pointTestlatlon;
    private Context mCtx;
    Paint depthLinePaint, testpaint, paintRoute, paintRegion;
    Path pathRegion;
    TextPaint textPaint;
    float textSize =30f;
    private Matrix mMatrix;
    boolean location = false;
    float location_lon, location_lat;
    PointF deltaPointF;

    boolean ontouch = false;
    Button buttonZoomIn,buttonZoomOut;
    //private Point pt = new Point(scrWidth/2,scrHeight/2);
    private ScaleGestureDetector scaleGestureDetector;
    GestureDetector gestureDetector;
    private BroadcastReceiver broadcast;
    int count;
    //lo trinh
    List<Text> routes;
    Route places;
    boolean onRoute = false;
    boolean chooseplace = false;

    Handler handler2;
    Thread thread1, thread2, thread3;
    boolean isItOk = false ;
    int dx1,dy1,dx2,dy2,delta;
    SurfaceHolder holder;
    int topX, topY, botX, botY;
    boolean start = true;
    Path mPath = new Path();

    //--------------//
    Vector<Line> lineScreens = new Vector<>();
    Vector<Vector<PointF>> polylineScreens = new Vector<>();
    Vector<Text> textScreen = new Vector<>();
    //--------------//

    public MapView(Context context,AttributeSet attr) {
        super (context, attr);
        mCtx = context; //<-- fill it with the Context you are passed
        readFile = new ReadFile(context);
        currentCell = new Point();
        textPaint = new TextPaint();
        depthLinePaint = new Paint();
        testpaint = new Paint();
        paintRegion = new Paint();
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleLister());
        gestureDetector = new GestureDetector(context, new GestureListener());
        mMatrix = new Matrix();
        places = new Route();
        //getDataUseThread();
//        handler2 = new Handler();

    }
    public void pause(){
        isItOk = false;
        while(true){
            try{
                thread1.join();
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            break;
        }
        thread1 = null;
    }

//    public void resume(){
//        isItOk = true;
//        thread1 = new Thread(this);
//        thread1.start();
//
//    }

//    @SuppressLint("DrawAllocation")
//    @TargetApi(Build.VERSION_CODES.O)
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onDraw(Canvas canvas) {// draw function
        super.onDraw(canvas);
        Log.d("draw","draw");

        mHeight = getHeight();
        mWidth = getWidth();
        scrCtY = getHeight() / 2;
        scrCtX = getWidth() / 2;
        //canvas.drawColor(Color.YELLOW);

        /*
        int radius = Math.min(scrCtX, scrCtY);
        float pi = (float) Math.PI;
        //canvas.drawColor(Color.rgb(30,30,30));
        textPaint.setStyle(Paint.Style.STROKE);
        depthLinePaint.setStyle(Paint.Style.STROKE);
        depthLinePaint.setColor(Color.RED);
        depthLinePaint.setAntiAlias(true);
        //draw a condinate center
        Paint paint_center = new Paint();
        paint_center.setColor(Color.BLACK);
        paint_center.setTextSize(scrCtX / 10);
        canvas.drawCircle(scrCtX, scrCtY, 5, paint_center);
        canvas.drawText(mlat + " + " + mlon, scrCtX + 10, scrCtY + 15, paint_center);
        canvas.drawText("mScale : " + mScale, 20, 20, paint_center);

        if (location) {
            Bitmap mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.location_maps);
            Point p1 = ConvWGSToScrPoint(location_lon, location_lat);
            Paint locationPaint = new Paint();
            canvas.drawBitmap(mbitmap, p1.x, p1.y, locationPaint);
        }
        if (onRoute){
            Path path= new Path();
            paintRoute = new Paint();
            paintRoute.setColor(Color.rgb(120,120,10));
            paintRoute.setStyle(Paint.Style.STROKE);
            paintRoute.setStrokeWidth(5);
            int i =0;
            for(Text t:routes) {
                Bitmap mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pin);
                Point p1 = ConvWGSToScrPoint(t.point1.x, t.point1.y);
                Paint locationPaint = new Paint();
                canvas.drawBitmap(mbitmap, p1.x, p1.y, locationPaint);
                if(i == 0) path.moveTo(p1.x, p1.y);
                else path.lineTo(p1.x, p1.y);
                i++;
            }
            canvas.drawPath(path,paintRoute);
        }

        if(chooseplace){
            ondrawchoosePlacetoRoute(canvas);
        }
        //textPaint.setTextSize(textSize);


        //112,18 -> 106,8
        PointF topRightLatLon = ConvScrPointToWGS(scrCtX*2,0);
        PointF botLeftLatLon = ConvScrPointToWGS(0,scrCtY*2);
//
        int topX = (int) topRightLatLon.x;
        int topY = (int) topRightLatLon.y;
//
        int botX = (int) botLeftLatLon.x;
        int botY = (int) botLeftLatLon.y;
//        //sc all cells inside the screen and draw all text object
//
        PointF point = new PointF(0, 0);
        int dem =0;


        for (int cellLon = botX; cellLon <= topX; cellLon += 1) {
            for (int cellLat = botY; cellLat <= topY; cellLat += 1) {
                //draw data of current cell
                currentCell = new Point(cellLon, cellLat);

                // draw text
                if (readFile.listText.containsKey(currentCell)) {
                    Vector<Text> objectList = readFile.listText.get(currentCell);
                    for (Text obj : objectList) {
                        Point p1 = ConvWGSToScrPoint(obj.point1.x, obj.point1.y);
                        Point p2 = ConvWGSToScrPoint(obj.point2.x, obj.point2.y);
                        int distance = Distance(p1, p2);
//                        if (mScale > getWidth() / 70) {
                        int color = obj.pen[2];
                        int red = (int) color / 65536;
                        int green = (int) (color - red * 65536) / 256;
                        int blue = (int) (color - red * 65536 - green * 256);
                        try {
                            Typeface tf = setFont(obj.pen[0], obj.font);
                            textPaint.setTypeface(tf);
                        } catch (Exception e) {
                        }
                        ;

                        textPaint.setColor(Color.rgb(red, green, blue));
                        mPath = new Path();
                        mPath.moveTo(p1.x, p1.y);
                        mPath.lineTo(p2.x, p2.y);
                        if (mScale <= 1.5f && levelPreference == 1 && obj.name.length() > 6) {
//                            textPaint.setTextSize(distance / 11f);
                            textPaint.setTextSize(distance / obj.name.length());
                            canvas.drawTextOnPath(obj.name, mPath, 0, 0, textPaint);
                        } else if (mScale > 2 && mScale <= 6 && levelPreference == 2 && obj.name.length() > 3) {
//                            textPaint.setTextSize(distance / 10f);
                            textPaint.setTextSize(distance / obj.name.length());
                            canvas.drawTextOnPath(obj.name, mPath, 0, 0, textPaint);
                        } else if (levelPreference == 3 && mScale > 6) {
//                            textPaint.setTextSize(distance / 4f);
                            textPaint.setTextSize(distance / obj.name.length() / 2);
                            canvas.drawTextOnPath(obj.name, mPath, 0, 0, textPaint);
                        }
                        //canvas.drawTextOnPath(obj.name, mPath, 0, 0, textPaint);
                    }
                }
                // draw lines
//                if(readFile.listLine.containsKey(currentCell)){
//                    Vector<Line> objLineList = readFile.listLine.get(currentCell);
//                    for (Line obj:objLineList){
//                        Point p1 = ConvWGSToScrPoint(obj.point1.x, obj.point1.y);
//                        Point p2 = ConvWGSToScrPoint(obj.point2.x, obj.point2.y);
//                        if(p1.equals(p2)) continue;
//                        //textPaint.setFontFeatureSettings(obj.font);
//                        int color = obj.pen[2];
//                        int red =(int) color /65536;
//                        int green = (int) (color - red * 65536) / 256;
//                        int blue = (int) (color - red * 65536 - green * 256);
//                        depthLinePaint.setColor(Color.rgb(red, green, blue));
//                        depthLinePaint.setStrokeWidth(obj.pen[0]);
//                        canvas.drawLine(p1.x, p1.y,p2.x,p2.y,depthLinePaint);
//
//                    }
//                }

                // draw Polyline


                for (Polyline pl: readFile.listPLine){
                    Vector<PointF> pointfs = pl.lines.get(currentCell);
                    if (pointfs == null) continue;
                    int size = pointfs.size() * 4;
                    float pointis[] = new float[size];
                    for (int i = 0; i < size - 4; i += 4) {
                        PointF pointf1 = pointfs.elementAt(i / 4);
                        PointF pointf2 = pointfs.elementAt(i / 4 + 1);
                        if (pointf1.equals(point) || pointf2.equals(point) || pointf1.equals(pointf2))
                            continue;
                        Point p1 = ConvWGSToScrPoint(pointf1.x, pointf1.y);
                        Point p2 = ConvWGSToScrPoint(pointf2.x, pointf2.y);
                        pointis[i] = (float) p1.x;
                        pointis[i + 1] = (float) p1.y;
                        pointis[i + 2] = (float) p2.x;
                        pointis[i + 3] = (float) p2.y;
                    }
                    int color = pl.pen[2];
                    int red = (int) color / 65536;
                    int green = (int) (color - red * 65536) / 256;
                    int blue = (int) (color - red * 65536 - green * 256);
                    depthLinePaint.setColor(Color.rgb(red, green, blue));
                    depthLinePaint.setStrokeWidth(pl.pen[0]);
                    //             canvas.drawPath(lines,depthLinePaint);
                    dem ++ ;
                    canvas.drawLines(pointis, depthLinePaint);
                }


                // draw region

                for(Region obr: readFile.listRegion){
                    Vector<PointF> pointfs = obr.lines.get(currentCell);
                    if(pointfs==null)continue;
                    int size = pointfs.size();
                    pathRegion = new Path();
                    for(int i =0; i<size; i++){
                        if(i == 0){
                            Point point1 = ConvWGSToScrPoint(pointfs.elementAt(i).x, pointfs.elementAt(i).y);
                            pathRegion.moveTo(point1.x, point1.y);
                        }
                        else {
                            PointF pointf1 = pointfs.elementAt(i);
                            if( !pointf1.equals(point)) {
                                Point point1 = ConvWGSToScrPoint(pointfs.elementAt(i).x, pointfs.elementAt(i).y);
                                pathRegion.lineTo(point1.x, point1.y);
                            }
                        }
                    }
                    //paintRegion.set(Color.YELLOW);
                    paintRegion.setStyle(Paint.Style.STROKE);
                    //paintRegion.setStyle(Paint.Style.);
                    int color = obr.pen[1];
                    int red =(int) color /65536;
                    int green = (int) (color - red * 65536) / 256;
                    int blue = (int) (color - red * 65536 - green * 256);
                    paintRegion.setColor(Color.rgb(red, green, blue));
                    //paintRegion.setColorFilter(new ColorFilter());
//                    paintRegion.setColorFilter()
                    paintRegion.setStrokeWidth(obr.pen[0]);
                    paintRegion.setAntiAlias(true);
                    paintRegion.setDither(true);
                    //canvas.drawCircle();
                    canvas.drawPath(pathRegion,paintRegion);
                }

            }
        }
        int m = dem;
        //Log.d("ve binh thuong: ", "" + dem);
        //
//        Log.d("hoang huy", "huy");
//
//        for(Vector<PointF> p: polylineScreens){
//            Log.d("hoang huy: ", "size: "+polylineScreens.size());
//            int size = p.size() * 4;
//                    float pointis[] = new float[size];
//                    for (int i = 0; i < size - 4; i += 4) {
//                        PointF pointf1 = p.elementAt(i / 4);
//                        PointF pointf2 = p.elementAt(i / 4 + 1);
//                        if (pointf1.equals(point) || pointf2.equals(point) || pointf1.equals(pointf2))
//                            continue;
//                        Point p1 = ConvWGSToScrPoint(pointf1.x, pointf1.y);
//                        Point p2 = ConvWGSToScrPoint(pointf2.x, pointf2.y);
//                        pointis[i] = (float) p1.x;
//                        pointis[i + 1] = (float) p1.y;
//                        pointis[i + 2] = (float) p2.x;
//                        pointis[i + 3] = (float) p2.y;
//                    }
////                    int color = pl.pen[2];
////                    int red = (int) color / 65536;
////                    int green = (int) (color - red * 65536) / 256;
////                    int blue = (int) (color - red * 65536 - green * 256);
////                    depthLinePaint.setColor(Color.rgb(red, green, blue));
////                    depthLinePaint.setStrokeWidth(pl.pen[0]);
//                    //             canvas.drawPath(lines,depthLinePaint);
//            canvas.drawLines(pointis, depthLinePaint);
//        }

//        for(Text obj: textScreen){
//            Point p1 = ConvWGSToScrPoint(obj.point1.x, obj.point1.y);
//                        Point p2 = ConvWGSToScrPoint(obj.point2.x, obj.point2.y);
//                        int distance = Distance(p1, p2);
////                        if (mScale > getWidth() / 70) {
//                        int color = obj.pen[2];
//                        int red = (int) color / 65536;
//                        int green = (int) (color - red * 65536) / 256;
//                        int blue = (int) (color - red * 65536 - green * 256);
//                        try {
//                            Typeface tf = setFont(obj.pen[0], obj.font);
//                            textPaint.setTypeface(tf);
//                        } catch (Exception e) {
//                        }
//                        ;
//
//                        textPaint.setColor(Color.rgb(red, green, blue));
//                        mPath = new Path();
//                        mPath.moveTo(p1.x, p1.y);
//                        mPath.lineTo(p2.x, p2.y);
//                        if (mScale <= 1.5f && levelPreference == 1) {
////                            textPaint.setTextSize(distance / 11f);
//                            textPaint.setTextSize(distance / obj.name.length());
//                            //canvas.drawTextOnPath(obj.name, path, 0, 0, textPaint);
//                        } else if (mScale > 2 && mScale <= 6 && levelPreference == 2) {
////                            textPaint.setTextSize(distance / 10f);
//                            textPaint.setTextSize(distance / obj.name.length());
//                            //canvas.drawTextOnPath(obj.name, path, 0, 0, textPaint);
//                        } else if (levelPreference == 3 && mScale > 6) {
////                            textPaint.setTextSize(distance / 4f);
//                            textPaint.setTextSize(distance / obj.name.length() / 2);
//                            //canvas.drawTextOnPath(obj.name, path, 0, 0, textPaint);
//                        }
//                        canvas.drawTextOnPath(obj.name, mPath, 0, 0, textPaint);
//        }


        //

         */



    }




    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public  Typeface setFont(int valueFont, String font){
        Typeface tf;
        if(font.contains("arial")){
            if(valueFont == 0)
                tf = ResourcesCompat.getFont(mCtx,R.font.arial);
            else if(valueFont ==1 ){
                tf = ResourcesCompat.getFont(mCtx,R.font.arialb);
            }
            else tf = ResourcesCompat.getFont(mCtx,R.font.ariali);
            levelPreference = 3;
        }
        else if(font.contains("tahoma")){
            if(valueFont == 0)
                tf = ResourcesCompat.getFont(mCtx,R.font.tahoma);
            else if(valueFont ==1 ){
                tf = ResourcesCompat.getFont(mCtx,R.font.tahomab);
            }
            else tf = ResourcesCompat.getFont(mCtx,R.font.tahomai);
            levelPreference = 2;
        }
        else if(font.contains("vnarial")){
            if(valueFont == 0)
                tf = ResourcesCompat.getFont(mCtx,R.font.vnarial);
            else if(valueFont ==1 ){
                tf = ResourcesCompat.getFont(mCtx,R.font.vnarialb);
            }
            else tf = ResourcesCompat.getFont(mCtx,R.font.vnariali);
            levelPreference =3;
        }
        else if(font.contains("vnarialh")){
            if(valueFont == 0)
                tf = ResourcesCompat.getFont(mCtx,R.font.vnarialh);
            else if(valueFont ==1 ){
                tf = ResourcesCompat.getFont(mCtx,R.font.vnarialhb);
            }
            else tf = ResourcesCompat.getFont(mCtx,R.font.vnarialhi);
            levelPreference = 2;
        }
        else if(font.contains("vntimeh")){
            if(valueFont == 0)
                tf = ResourcesCompat.getFont(mCtx,R.font.vntimeh);
            else if(valueFont ==1 ){
                tf = ResourcesCompat.getFont(mCtx,R.font.vntimehb);
            }
            else tf = ResourcesCompat.getFont(mCtx,R.font.vntimehi);
            levelPreference =2;
        }
        else if(font.contains("vntime")){
            if(valueFont == 0)
                tf = ResourcesCompat.getFont(mCtx,R.font.vntime);
            else if(valueFont ==1 ){
                tf = ResourcesCompat.getFont(mCtx,R.font.vntimehb);
            }
            else tf = ResourcesCompat.getFont(mCtx,R.font.vntimehi);
            levelPreference =2;
        }
        else if(font.contains("vnhelvetinsh")){
            levelPreference =1;
            tf = ResourcesCompat.getFont(mCtx,R.font.vnarialb);
        }
        else {
            levelPreference =2;
            tf = ResourcesCompat.getFont(mCtx,R.font.arial);
        }
        return tf;
    }

//    public int camparePoint(Point point1, Point point2){
//        if (point1.x != point2.x) {
//            return Integer.compare(point1.x, point2.x);
//        } else {
//            return Integer.compare(point1.y, point2.y);
//        }
//    }

    public Point ConvWGSToScrPoint(float m_Long,float m_Lat)// converting lat lon  WGS coordinates to screen XY coordinates
    {
        Point s = new Point();
        float refLat = (mlat + (m_Lat))*0.00872664625997f;//pi/360
        s.set((int )(mScale*((m_Long - mlon) * 111.31949079327357)*cos(refLat))+scrCtX,
                (int)(mScale*((mlat- (m_Lat)) * 111.132954))+getHeight()/2
        );
        return s;
    }
    public PointF ConvScrPointToWGS(int x,int y)
    {
        float olat  = mlat -  (float)(((y-scrCtY)/mScale)/(111.132954f));
        float refLat = (mlat +(olat))*0.00872664625997f;//3.14159265358979324/180.0/2;
        float olon = (x-scrCtX)/mScale/(111.31949079327357f*(float)cos(refLat))+ mlon;
        return new PointF(olon,olat);
    }

    //    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        points.add(new Point((int)event.getX(), (int)event.getY()));
//        invalidate();
//        return true;
//    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //points.add(new Point((int)event.getX(), (int)event.getY()));
            //pt.set((int)event.getX(), (int)event.getY());
            dragStart = new PointF(event.getX(),event.getY());
            dx1 = (int) event.getX();
            dy1 = (int) event.getY();

        }
//        else if (event.getAction() == MotionEvent.ACTION_UP) {
//            //pt.set((int)event.getX(), (int)event.getY());
//            //points.add(new Point((int)event.getX(), (int)event.getY()));
//            dragStop = new PointF(event.getX(),event.getY());
//            PointF newLatLon = ConvScrPointToWGS((int)(dragStart.x-dragStop.x)+scrCtX,(int)(dragStart.y-dragStop.y)+scrCtY);
//            mlat=newLatLon.y;
//            mlon=newLatLon.x;
//            deltaPointF = ConvScrPointToWGS((int)(dragStart.x - dragStop.x), (int) (dragStart.y - dragStop.y));
//            ontouch = true;
//            postInvalidate();
//        }
        else if(event.getAction()==MotionEvent.ACTION_MOVE)
        {
            dragStop = new PointF(event.getX(),event.getY());
            dx2 = (int) event.getX();
            dy2 = (int) event.getY();
            PointF newLatLon = ConvScrPointToWGS((int)(dragStart.x-dragStop.x)+scrCtX,(int)(dragStart.y-dragStop.y)+scrCtY);
            //deltaPointF = new PointF(newLatLon.x - mlon, newLatLon.y -mlat);
            mlat=newLatLon.y;
            mlon=newLatLon.x;
            dragStart = dragStop;
            ontouch = true;

//            PointF leftBottomCoor = ConvScrPointToWGS(0,mHeight);
//
//            int iBotX = (int) leftBottomCoor.x;
//            int iBotY = (int) leftBottomCoor.y;
//
//            PointF rightTopCoor = ConvScrPointToWGS(mWidth,0);
//
//            int iTopX = (int) rightTopCoor.x;
//            int iTopY = (int) rightTopCoor.y;
//
//            if(iTopX > topX || iBotX < botX || iBotY < botY || iTopY > topY){
//                start = false;
//                getDataUseThread();
//            }
//            else
//                invalidate();


        }
        invalidate();

        return true;
    }



    private class ScaleLister extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScale(ScaleGestureDetector sgd){
            mScale *= sgd.getScaleFactor();
            textSize *= sgd.getScaleFactor();
            mScale = Math.max(1f, Math.min(mScale, 20));
            start = false;
            //getDataUseThread();
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDoubleTap(MotionEvent e){
            mScale *= 1.5f;
            start = false;
            //getDataUseThread();
            invalidate();
            return true;
        }
    }

    private int Distance(Point p1, Point p2){
        return (int) Math.sqrt(Math.pow((p1.x -p2.x),2) + Math.pow((p1.y - p2.y),2));
    }

    public void setLonLat(float latLoc, float lonLoc){
        mlat = location_lat = latLoc;
        mlon = location_lon = lonLoc;
        mScale = 4;
        location =true;
        invalidate();
    }

    public void choosePlacetoRoute(){
        chooseplace = true;
        int i = places.size();
        Text t = new Text();
        //t.name = "Dia diem "+ (i+1);
        //t.point1 = ConvScrPointToWGS(scrCtX, scrCtY);
        places.add(t);
        invalidate();
    }

    private void ondrawchoosePlacetoRoute(Canvas canvas){
        int i=0;
        Path path = new Path();
        paintRoute = new Paint();

        paintRoute.setColor(Color.rgb(120,120,10));
        paintRoute.setStyle(Paint.Style.STROKE);
        paintRoute.setStrokeWidth(3);
        /*
        for(Text t:places) {
            Bitmap mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pin);
            Point p1 = ConvWGSToScrPoint(t.point1.x, t.point1.y);
            Paint locationPaint = new Paint();
            canvas.drawBitmap(mbitmap, p1.x, p1.y, locationPaint);
            if (i == 0) path.moveTo(p1.x, p1.y);
            else path.lineTo(p1.x, p1.y);
            i++;
        }
        if(i>1)
            canvas.drawPath(path,paintRoute);

         */

    }

    public List<Text> listPlace(){

        List<Text> list = new ArrayList<>();

        /*Collection<Vector<Text>> listVector = readFile.listText.values();
        for(Vector<Text> v: listVector){
            for(int i=0; i<v.size(); i++){
                if(!v.get(i).name.matches("(^-)*\\d+"))
                    list.add(v.get(i));
            }
        }

         */
        return list;

    }

    public Route coordinateRoute(){
        return places;
    }


    public void drawRoute(List<Text> list){
        onRoute = true;
        //mlat = list.get(0).point1.y;
        //mlon = list.get(0).point1.x;
        routes = list;
        invalidate();
    }
    public void canceldrawRoute(){
        onRoute = false;
        invalidate();
    }

    public void setPoliLines(Vector<Vector<PointF>> pl){
        polylineScreens = pl;
    }

//    public static class MyHandler extends Handler{
//
//        private MapView map;
//        public MyHandler(MapView map){
//            super();
//            this.map = map;
//        }
//        @Override
//        public void handleMessage(Message msg){
//            Bundle b = msg.getData();
//            Vector<Vector<PointF>> pl = b.getParcelable("polylines");
//            map.setPoliLines(pl);
//        }
//    }

}


