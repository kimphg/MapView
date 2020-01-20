package com.example.myapplication.view;

import android.annotation.TargetApi;
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
import android.graphics.Typeface;
import android.os.Build;
import android.os.Environment;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.core.content.res.ResourcesCompat;

import com.example.myapplication.R;
import com.example.myapplication.classes.Places;
import com.example.myapplication.object.Line;
import com.example.myapplication.object.Polyline;
import com.example.myapplication.object.Polylines;
import com.example.myapplication.object.Region;
import com.example.myapplication.object.Regions;
import com.example.myapplication.object.Text;
import com.example.myapplication.object.Texts;
import com.google.android.gms.maps.model.Polygon;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.AccessMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;

public class SeaMap  extends View {

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
    boolean location = false;
    float location_lon, location_lat;

    private int AreaX[];
    private int AreaY[];
    private ScaleGestureDetector scaleGestureDetector;

    HashMap<String,Vector<Polylines>> PLines = new HashMap<String, Vector<Polylines>>();
    HashMap<String,Vector<Texts>> tTexts = new HashMap<String, Vector<Texts>>();
    HashMap<String,Vector<Regions>> Poligons = new HashMap<String, Vector<Regions>>();
//    TreeMap<String, >

    Paint depthLinePaint = new Paint(), paintCenter = new Paint(), textPaint = new Paint(), paintRegion = new Paint();
    Path mPath= new Path();

    public SeaMap(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCtx = context;
        initArea();
        loadJSONFromAsset(isScreens((int)(n /(int) mScale )));
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
        int demduong = 0;
//        for(Map.Entry m : PLines.entrySet()){
//           Vector<Polylines> pl =(Vector<Polylines>) m.getValue();
//           dem += pl.size();
//           for(int i =0 ;i< pl.size(); i++){if(pl.get(i).getType() == 1) demduong++;}
//        }

        if (location) {
            Bitmap mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.location_maps);
            Point p1 = ConvWGSToScrPoint(location_lon, location_lat);
            Paint locationPaint = new Paint();
            canvas.drawBitmap(mbitmap, p1.x, p1.y, locationPaint);
        }

        //draw polylyline
        for(int lon = (int) pointT3.x - 1 ; lon<= (int) pointT1.x  + 1; lon++){
            for(int lat = (int) pointT3.y - 1 ; lat <= (int) pointT1.y  + 1; lat++ ){
                String area = lon + "-" + lat;
                Vector<Polylines> PL = PLines.get(area);
                if(PL == null) continue;
                for(int k=0; k < PL.size(); k++){
                    Polylines pl = PL.get(k);
                    if(mScale <=5 && pl.getType() == 1) continue;
                    int size = pl.getCoordinates().size();
                    float pointis[] = new float[size * 2];
                    for (int i = 0; i < size - 4; i += 2) {
                        Point p1 = ConvWGSToScrPoint(pl.getCoordinates().get(i), pl.getCoordinates().get(i + 1));
                        Point p2 = ConvWGSToScrPoint(pl.getCoordinates().get(i + 2), pl.getCoordinates().get(i + 3));
                        pointis[2 * i] = (float) p1.x;
                        pointis[2 * i + 1] = (float) p1.y;
                        pointis[i * 2 + 2 ] = (float) p2.x;
                        pointis[i * 2 + 3 ] = (float) p2.y;
                    }
                    int color = pl.getPen()[2];
                    int red = (int) color / 65536;
                    int green = (int) (color - red * 65536) / 256;
                    int blue = (int) (color - red * 65536 - green * 256);
                    depthLinePaint.setColor(Color.rgb(red, green, blue));
                    depthLinePaint.setStrokeWidth(2);
                    //depthLinePaint.setAntiAlias(true);
                    canvas.drawLines(pointis, depthLinePaint);
                    dem ++;
                }
                //draw text
                Vector<Texts> tT = tTexts.get(area);
                if(tT == null) continue;
                for (int k =0; k< tT.size(); k++) {
                    Texts text = tT.get(k);
                    if(mScale < 5 && text.getType() !=3) continue;
                    Point p1 = ConvWGSToScrPoint(text.getCoordinates().get(0), text.getCoordinates().get(1));
                    Point p2 = ConvWGSToScrPoint(text.getCoordinates().get(2), text.getCoordinates().get(3));
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

                // draw polygon
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

            }
        }
        //draw pline

        int m = dem;
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

    public String[] isScreens (int n){

        int index[] = new int [2];

        for(int i=0; i<20; i++){if(AreaX[i] < mlon && mlon < AreaX[i] + 1) index[0] = i;}
        for(int i=0; i<20; i++){if(AreaY[i] < mlat && mlat < AreaX[i] + 1) index[1] = i;}

        int canh = (int) sqrt(n);
        int delta = canh / 2;
        int deltaX =0, deltaY =0;

        if(index[0] - delta <= 0) deltaX = 0;
        else deltaX = index[0] - delta ;

        if(index[1] + delta > 20) deltaY = 20;
        else deltaY = index[1] + delta ;

        String area[] = new String [(deltaY - deltaX) * (deltaY - deltaX)];
        int dem = 0;
        for(int i = deltaX; i< deltaY ;i++){
            for(int j = deltaX; j< deltaY; j++){
                area[dem] = AreaX[i] + "-" + AreaY[j];
                dem++;
            }
        }
        return area;
    }


    public void loadJSONFromAsset(String [] area) {
        String json = null;
        try {
            String [] List ;
            AssetManager am = mCtx.getAssets();
            List = mCtx.getAssets().list("location");
            for(String a: area){
                InputStream is = am.open("location" + "/" + a + ".json");
                int size = is.available();
                if(size <= 2) continue;
                byte[] buffer = new byte[size];

                is.read(buffer);

                is.close();
                json = new String(buffer, "UTF-8");
                readJson(json, a);

            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void readJson(String json, String area) {
        try {
            JSONObject jsonRoot = new JSONObject(json);
            JSONArray PLine = jsonRoot.getJSONArray("Polyline");
            Vector<Polylines> vtrPl = new Vector<Polylines>();
            for (int i = 0; i < PLine.length(); i++) {
                Polylines pl = null;
                try {
                    pl = new Gson().fromJson(PLine.get(i).toString(), Polylines.class);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                vtrPl.add(pl);
            }
            PLines.put(area,vtrPl);

            JSONArray mT = jsonRoot.getJSONArray("Text");
            Vector<Texts> vtTexts = new Vector<Texts>();
            for (int i = 0; i < mT.length(); i++) {
                Texts text = null;
                try {
                    text = new Gson().fromJson(mT.get(i).toString(), Texts.class);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                vtTexts.add(text);
            }
            tTexts.put(area, vtTexts);

            JSONArray mPolygons = jsonRoot.getJSONArray("Region");
            Vector<Regions> vtRegions = new Vector<Regions>();
            for (int i = 0; i < mPolygons.length(); i++) {
                Regions region = null;
                try {
                    region = new Gson().fromJson(mPolygons.get(i).toString(), Regions.class);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                vtRegions.add(region);
            }
            Poligons.put(area,vtRegions);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
            //getDataUseThread();
            return true;
        }

//        @Override
//        public boolean onScaleBegin(ScaleGestureDetector detector) {
//            detector.
//
//            return true;
//        }
    }

    private View.OnLongClickListener infoAcoordinate = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {

            return false;
        }
    };

    public void setLonLat(float latLoc, float lonLoc){
        mlat = location_lat = latLoc;
        mlon = location_lon = lonLoc;
        mScale = 10;
        location =true;
        invalidate();
    }

}
