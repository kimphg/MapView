package com.example.myapplication.view;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.text.StaticLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.inspector.StaticInspectionCompanionProvider;

import androidx.core.content.res.ResourcesCompat;

import com.example.myapplication.R;
import com.example.myapplication.object.Line;
import com.example.myapplication.object.Polyline;
import com.example.myapplication.object.Region;
import com.example.myapplication.object.Text;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;

public class SeaMap  extends View {

    private static boolean SEARCHPLACE = false;
    private static boolean MYLOCATION = false;
    private static boolean DIRECTIONS = false;
    private static boolean BACKSEARCH = false;

    private float mlat = 18.32f;//lattitude of the center of the screen
    private float mlon = 105.43f;//longtitude of the center of the screen
    private float mScale = 2f;// 1km = mScale*pixcels
    private float mScaleN = 2f;
    private float n = 400;
    private Context mCtx;
    private int scrCtY,scrCtX;
    PointF dragStart,dragStop;
    int dx1, dy1 , dx2, dy2;
    int levelPreference =0;

    float location_lon = 105.43f, location_lat = 18.32f;
    float searchPlace_lon, searchPlace_lat;

    private int AreaX[];
    private int AreaY[];
    private ScaleGestureDetector scaleGestureDetector;

    HashMap<String,Vector<Line>> Lines = new HashMap<String, Vector<Line>>();
    HashMap<String,Vector<Polyline>> PLines = new HashMap<String, Vector<Polyline>>();
    HashMap<String,Vector<Text>> tTexts = new HashMap <String, Vector<Text>>();
    HashMap<String,Vector<Region>> Poligons = new HashMap<String, Vector<Region>>();
    List<Text> ListPlace = new ArrayList<>();
//    TreeMap<String, >


    Paint depthLinePaint = new Paint(), paintCenter = new Paint(), textPaint = new Paint(), paintRegion = new Paint();
    Path mPath= new Path();
    Paint cusPaint = new Paint();

    public SeaMap(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCtx = context;
        initArea();
        //loadJSONFromAsset(isScreens((int)(n /(int) mScale )));
        readFileByte();
        scaleGestureDetector = new ScaleGestureDetector(context, new SeaMap.ScaleLister());
        setOnLongClickListener(infoAcoordinate);
    }



    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);

        scrCtY = getHeight() / 2;
        scrCtX = getWidth() / 2;


        PointF pointLU = ConvScrPointToWGS(0,0);
        PointF pointT1 = ConvScrPointToWGS(scrCtX * 2,0);
        PointF pointT3 = ConvScrPointToWGS(0, scrCtY * 2);

        PointF pointRB = ConvScrPointToWGS(getWidth()/2,getHeight()/2);
        PointF pointT2 = ConvScrPointToWGS(getWidth(),getHeight());

        canvas.drawCircle(getWidth()/2, getHeight()/2, 5, paintCenter);
        paintCenter.setTextSize(50);
        canvas.drawText(pointRB.x + ","+pointRB.y,getWidth()/2, getHeight()/2, paintCenter);

        canvas.drawCircle(0, 0, 5, paintCenter);
        paintCenter.setTextSize(50);
        canvas.drawText(pointLU.x + ","+pointLU.y,50, 50, paintCenter);

        canvas.drawCircle(getWidth(), getHeight(), 5, paintCenter);
        paintCenter.setTextSize(50);
        canvas.drawText(pointT2.x + ","+pointT2.y,getWidth() -300, getHeight() - 50, paintCenter);

        canvas.drawCircle(0, getHeight(), 5, paintCenter);
        paintCenter.setTextSize(50);
        canvas.drawText(pointT1.x + ","+pointT1.y,50, getHeight() -50, paintCenter);

        canvas.drawCircle(getWidth(), 0, 5, paintCenter);
        paintCenter.setTextSize(50);
        canvas.drawText(pointT3.x + ","+pointT3.y,getWidth() - 300, 50, paintCenter);

//        if( mScale != mScaleN) {
//            loadJSONFromAsset(isScreens((int)(n /(int) mScale )));
//            mScaleN = mScale;
//        }
        int dem = 0;
//        int demduong = 0;
//        for(Map.Entry m : PLines.entrySet()){
//           Vector<Polyline> pl =(Vector<Polyline>) m.getValue();
//           dem += pl.size();
//           //if(pl.size() != 0) demduong++;
//           for(int i =0 ;i< pl.size(); i++){if(pl.get(i) == null) demduong++;}
//        }
//        int j = listPlace.size();

//        paintRegion.setAntiAlias(true);
//        paintRegion.setColor(Color.BLACK);
//        paintRegion.setStyle(Paint.Style.FILL_AND_STROKE);
//        paintRegion.setStrokeWidth(4);

        cusPaint.setAntiAlias(true);
        cusPaint.setStyle(Paint.Style.FILL);
        cusPaint.setColor(Color.rgb(255, 239, 213));

        //cusPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));

        if (MYLOCATION) {
            Bitmap mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.location_maps);
            Point p1 = ConvWGSToScrPoint(location_lon, location_lat);
            Paint locationPaint = new Paint();
            canvas.drawBitmap(mbitmap, p1.x, p1.y, locationPaint);
        }

        if(SEARCHPLACE){
            Point p1 = ConvWGSToScrPoint(searchPlace_lon, searchPlace_lat);
//            Point p2 = ConvWGSToScrPoint(location_lon, location_lat);
//            Paint searchPl = new Paint();
//            searchPl.setColor(Color.RED);
//
//            float pointf [] = {p1.x, p1.y, p2.x, p2.y};
//            canvas.drawLines(pointf,searchPl);
            Bitmap mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.location_maps);
            Paint searchPl = new Paint();
            canvas.drawBitmap(mbitmap, p1.x, p1.y, searchPl);
        }

        if(DIRECTIONS){
            Point p1 = ConvWGSToScrPoint(searchPlace_lon, searchPlace_lat);
            Point p2 = ConvWGSToScrPoint(location_lon, location_lat);
            Paint searchPl = new Paint();
            searchPl.setColor(Color.RED);
            float pointf [] = {p1.x, p1.y, p2.x, p2.y};
            canvas.drawLines(pointf,searchPl);
        }
/*
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                // draw polygon
                for(int lon = (int) pointT3.x ; lon<= (int) pointT1.x  ; lon++) {
                    for (int lat = (int) pointT3.y; lat <= (int) pointT1.y; lat++) {
                        String area = lon + "-" + lat;
                        Vector<Regions> rE = Poligons.get(area);
                        if(rE == null) continue;
                        for(int k =0;  k< rE.size() ; k++){
                            Regions polygon = rE.get(k);
                            Path pathRegion = new Path();
                            for(int i =0; i< polygon.getCoordinates().size() ; i = i+2){
                                if(i == 0){
                                    Point point1 = ConvWGSToScrPoint(polygon.getCoordinates().get(i), polygon.getCoordinates().get(i + 1));
                                    pathRegion.moveTo(point1.x, point1.y);
                                }
                                else {
                                    Point point1 = ConvWGSToScrPoint(polygon.getCoordinates().get(i), polygon.getCoordinates().get(i + 1));
                                    pathRegion.lineTo(point1.x, point1.y);
                                }
                            }
                            paintRegion.setStyle(Paint.Style.STROKE);
                            int color = polygon.getPen()[2];
                            int red =(int) color /65536;
                            int green = (int) (color - red * 65536) / 256;
                            int blue = (int) (color - red * 65536 - green * 256);
                            paintRegion.setColor(Color.rgb(red, green, blue));
                            paintRegion.setStrokeWidth(polygon.getPen()[0]);
                            //paintRegion.setDither(true);
                            paintRegion.setAntiAlias(true);

                            canvas.drawPath(pathRegion,paintRegion);
                    }
                } }
            }
        });
        thread1.start();
*/

        for(int lon = (int) pointT3.x ; lon<= (int) pointT1.x  ; lon++){
            for(int lat = (int) pointT3.y  ; lat <= (int) pointT1.y  ; lat++ ){
                String area = lon + "-" + lat;
                //              draw polygons
                Vector<Region> rE = Poligons.get(area);
                if(rE == null) continue;
                for(int k =0;  k< rE.size() ; k++) {
                    Region polygon = rE.get(k);
                    if(polygon == null) continue;
                    Path pathRegion = new Path();
                    for (int i = 0; i < polygon.getCoordinate().length; i = i + 2) {
                        if (i == 0) {
                            Point point1 = ConvWGSToScrPoint(polygon.getCoordinate()[i], polygon.getCoordinate()[i + 1]);
                            pathRegion.moveTo(point1.x, point1.y);
                        } else {
                            Point point1 = ConvWGSToScrPoint(polygon.getCoordinate()[i], polygon.getCoordinate()[i + 1]);
                            pathRegion.lineTo(point1.x, point1.y);
                        }
                    }
//                    int color = polygon.getPen()[2];
//                    int red = (int) color / 65536;
//                    int green = (int) (color - red * 65536) / 256;
//                    int blue = (int) (color - red * 65536 - green * 256);

                    //canvas.drawPath(pathRegion, paintRegion);
                    canvas.drawPath(pathRegion, cusPaint);

                }
                //draw polyline
                Vector<Polyline> PL = PLines.get(area);
                if(PL == null) continue;
                for(int k=0; k < PL.size(); k++){
                    Polyline pl = PL.get(k);
                    if(PL.get(k) != null) {
                        if (mScale <= 5 && pl.getType() == 1) continue;
                        int size = pl.getCoordinate().length;
                        float pointis[] = new float[size * 2];
                        for (int i = 0; i < size - 2; i += 2) {
                            Point p1 = ConvWGSToScrPoint(pl.getCoordinate()[i], pl.getCoordinate()[i + 1]);
                            Point p2 = ConvWGSToScrPoint(pl.getCoordinate()[i + 2], pl.getCoordinate()[i + 3]);
                            pointis[2 * i] = (float) p1.x;
                            pointis[2 * i + 1] = (float) p1.y;
                            pointis[i * 2 + 2] = (float) p2.x;
                            pointis[i * 2 + 3] = (float) p2.y;
                        }
                        int color = pl.getPen()[2];
                        int red = (int) color / 65536;
                        int green = (int) (color - red * 65536) / 256;
                        int blue = (int) (color - red * 65536 - green * 256);
                        depthLinePaint.setColor(Color.rgb(red, green, blue));
                        depthLinePaint.setStrokeWidth(2);
                        //depthLinePaint.setAntiAlias(true);
                        canvas.drawLines(pointis, depthLinePaint);

                    }
                    else {
                        dem++;
                    }
                }
                //draw text
                Vector<Text> tT = tTexts.get(area);
                if(tT == null) continue;
                for (int k =0; k< tT.size(); k++) {
                    Text text = tT.get(k);
                    if(mScale < 5 && text.getType() !=3) continue;
                    Point p1 = ConvWGSToScrPoint(text.getCoordinate()[0], text.getCoordinate()[1]);
                    Point p2 = ConvWGSToScrPoint(text.getCoordinate()[2], text.getCoordinate()[3]);
                    int distance = Distance(p1, p2);
//                        if (mScale > getWidth() / 70) {
                    int color = text.getPen()[2];
                    int red = (int) color / 65536;
                    int green = (int) (color - red * 65536) / 256;
                    int blue = (int) (color - red * 65536 - green * 256);
                    try {
                        Typeface tf = setFont(text.getFont());
                        textPaint.setTypeface(tf);
                    } catch (Exception e) {
                    }
                    ;

                    textPaint.setColor(Color.rgb(red, green, blue));
                    mPath = new Path();
                    mPath.moveTo(p1.x, p1.y);
                    mPath.lineTo(p2.x, p2.y);

                    if (mScale <= 5f && text.getType() == 3) {
//                        if(text.getName().equals("hà nội")){
//                            Paint locationPaint = new Paint();
//                            locationPaint.setColor(Color.RED);
//                            canvas.drawCircle(p1.x, p2.y, 15, locationPaint);
//                            textPaint.setTextSize((int)(distance / 2) );
//                        }
//                        else
                        textPaint.setTextSize((int)(distance * 1.5) / text.getName().length());
                        canvas.drawTextOnPath(text.getName(), mPath, 0,0, textPaint);
                    }
                    else if (mScale > 5 && mScale <= 10 && text.getType() == 0 ) {
                        textPaint.setTextSize((int)(distance * 1.2 / text.getName().length()));
                        canvas.drawTextOnPath(text.getName(), mPath, 0, 0, textPaint);
                    }
                    else if ( mScale > 10 && text.getType() == 1) {
                        if(text.getName().length() != 0)
                            textPaint.setTextSize(distance / text.getName().length());
                        canvas.drawTextOnPath(text.getName(), mPath, 0, 0, textPaint);
                    }
                }

            }
        }


        //draw pline

        int m = dem;
        canvas.save();
    }

    public void initArea(){
        AreaX = new int[20];
        AreaY = new int[20];
        int xT = 100;
        int yT = 6;
        for (int i = 0; i < 20; i++) {
            AreaX[i] = xT;
            AreaY[i] = yT;
            xT +=1;
            yT +=1;
        }
    }

    void readFileByte(){
        AssetManager am = mCtx.getAssets();
        for (int u = 0; u < 20; u++) {
            for(int v = 0; v<20; v++) {
                String area = AreaX[u] + "-" + AreaY[v];
                String keyLatLon = AreaX[u] + "-" + AreaY[v] + ".bin";
                ObjectInputStream ois = null;
                int size = 0;
                try {
                    InputStream is = am.open("locationBytes" + "/" + keyLatLon);
                    ois = new ObjectInputStream(is);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                try {
                    size = ois.readInt();
                } catch (IOException e) {
                }

                //Text
                if (size != 0) {
                    Vector<Text> t = new Vector<>();
                    for (int j = 0; j < size; j++) {
                        Text obj = null;
                        try {
                            obj = (Text) ois.readObject();
                        } catch (ClassNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                        }

                        if(obj != null) {
                            t.add(obj);
                            if (obj.getType() != 1) ListPlace.add(obj);
                        }
                        //System.out.println(s);
                    }
                    tTexts.put(area, t);
                }

                // Line
                try {
                    size = ois.readInt();
                } catch (IOException e) {
                }
                if (size != 0) {
                    Vector<Line> l = new Vector<>();
                    for (int j = 0; j < size; j++) {
                        Line obj = null;
                        try {
                            obj = (Line) ois.readObject();
                        } catch (ClassNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                        }
                        if(obj != null)
                            l.add(obj);
                        //areaLine[i].add(obj);
                        // System.out.println(s);
                    }
                    Lines.put(area, l);

                }

                //Pline
                try {
                    size = ois.readInt();
                } catch (IOException e) {
                }
                if (size != 0) {
                    Vector<Polyline> pl = new Vector<>();
                    for (int j = 0; j < size; j++) {
                        Polyline obj = null;
                        try {
                            obj = (Polyline) ois.readObject();
                        } catch (ClassNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                        }
                        if(obj != null)
                            pl.add(obj);
                        //areaPline[i].add(obj);
                        // System.out.println(s);
                    }
                    PLines.put(area,pl);
                }

                //Region
                try {
                    size = ois.readInt();
                } catch (IOException e) {
                }

                if (size != 0) {
                    Vector<Region> r = new Vector<>();
                    for (int j = 0; j < size; j++) {
                        Region obj = null;
                        try {
                            obj = (Region) ois.readObject();
                        } catch (ClassNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                        }
                        if(obj != null)
                            r.add(obj);
//                        areaRegion[i].add(obj);
                        // System.out.println(s);
                    }
                    Poligons.put(area,r);
                }
            }
        }
        //System.out.println("read xong");
    }


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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
//        gestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //points.add(new Point((int)event.getX(), (int)event.getY()));
            //pt.set((int)event.getX(), (int)event.getY());
            dragStart = new PointF(event.getX(),event.getY());

        }
        else if(event.getAction()==MotionEvent.ACTION_MOVE)
        {
            dragStop = new PointF(event.getX(),event.getY());

            PointF newLatLon = ConvScrPointToWGS((int)(dragStart.x-dragStop.x)+scrCtX,(int)(dragStart.y-dragStop.y)+scrCtY);
            //deltaPointF = new PointF(newLatLon.x - mlon, newLatLon.y -mlat);
            mlat=newLatLon.y;
            mlon=newLatLon.x;

            dragStart = dragStop;
            //ontouch = true;
        }
        invalidate();

        return true;
    }

    public  Typeface setFont(String font){
        Typeface tf;
        if(font.contains("arial")){
            tf = ResourcesCompat.getFont(mCtx, R.font.arial);
        }
        else if(font.contains("tahoma")){
            tf = ResourcesCompat.getFont(mCtx,R.font.tahoma);
        }
        else if(font.contains("vnarial")){
            tf = ResourcesCompat.getFont(mCtx,R.font.vnarial);
        }
        else if(font.contains("vnarialh")){
            tf = ResourcesCompat.getFont(mCtx,R.font.vnarialh);
        }
        else if(font.contains("vntimeh")){
            tf = ResourcesCompat.getFont(mCtx,R.font.vntimeh);
        }
        else if(font.contains("vntime")){
            tf = ResourcesCompat.getFont(mCtx,R.font.vntime);
        }
        else if(font.contains("vnhelvetinsh")){
            tf = ResourcesCompat.getFont(mCtx,R.font.vnarialb);
        }
        else if(font.contains("vnbodonih")){
            tf = ResourcesCompat.getFont(mCtx, R.font.vnbodonih);
        }
        else {
            tf = ResourcesCompat.getFont(mCtx,R.font.arial);
        }
        return tf;
    }

    private int Distance(Point p1, Point p2){
        return (int) Math.sqrt(Math.pow((p1.x -p2.x),2) + Math.pow((p1.y - p2.y),2));
    }

    private class ScaleLister extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScale(ScaleGestureDetector sgd){
            mScale *= sgd.getScaleFactor();
            mScale = Math.max(2f, Math.min(mScale, 20));
            invalidate();
            return true;
        }
    }

    private View.OnLongClickListener infoAcoordinate = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {

            return false;
        }
    };

    public void setLonLatMyLocation(float latLoc, float lonLoc){
        mlat = location_lat = latLoc;
        mlon = location_lon = lonLoc;
        mScale = 10;
        MYLOCATION =true;
        invalidate();
    }

    public List<Text> getListPlace() {
        return ListPlace;
    }

    public void myLocationToDirection(){
        DIRECTIONS = true;
        int temp1 = (int) abs(location_lat - searchPlace_lat);
        int temp2 = (int) abs(location_lon - searchPlace_lon);
        if(temp1 >= temp2) mScale = temp1 / temp2;
        else mScale = temp2 / temp1;
        invalidate();
    }

    public void setLonLatSearchPlace(float latLoc, float lonLoc){
        mlat = searchPlace_lat = latLoc;
        mlon = searchPlace_lon = lonLoc;
        mScale = 10;
        SEARCHPLACE = true;
        invalidate();
    }


}
