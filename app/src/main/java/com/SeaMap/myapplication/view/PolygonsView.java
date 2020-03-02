package com.Seamap.app.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Build;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.Seamap.app.Activity.MainActivity;
import com.Seamap.app.R;
import com.Seamap.app.classes.ReadFile;
import com.Seamap.app.object.Polyline;
import com.Seamap.app.object.Region;

import java.util.Vector;

import static java.lang.Math.cos;

public class PolygonsView extends View {

    protected static float mlat = 18.32f;//lattitude of the center of the screen
    protected static float mlon = 105.43f;//longtitude of the center of the screen
    protected static float mScale = 3f;// 1km = mScale*pixcels

    protected static int scrCtY,scrCtX;
    protected float location_lon = 105.43f, location_lat = 18.32f;
    protected boolean MYLOCATION = false;

    private Paint cusPaint = new Paint(), riverPaint = new Paint(), depthLinePaint = new Paint();
    private ScaleGestureDetector scaleGestureDetector;
    PointF dragStart,dragStop;

    public PolygonsView(Context context) {
        super(context);
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleLister());
        setOnLongClickListener(infoAcoordinate);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        scrCtY = getHeight() / 2;
        scrCtX = getWidth() / 2;

        PointF pointT1 = ConvScrPointToWGS(scrCtX * 2,0);
        PointF pointT3 = ConvScrPointToWGS(0, scrCtY * 2);

        cusPaint.setAntiAlias(true);
        cusPaint.setStyle(Paint.Style.FILL);
        cusPaint.setColor(Color.rgb(249, 164, 23));

        riverPaint.setAntiAlias(true);
        riverPaint.setStyle(Paint.Style.FILL);
        riverPaint.setColor(Color.rgb(102, 178, 255));

        //DRAW POLYGON
        for(int lon = (int) pointT3.x - 2; lon<= (int) pointT1.x + 2; lon++) {
            for (int lat = (int) pointT3.y - 2; lat <= (int) pointT1.y + 2; lat++) {
                String area = lon + "-" + lat;
                Vector<Region> regions;
                if(mScale < 8) {
                    regions = ReadFile.BaseRegions.get(area);
                }
                else
                    regions = ReadFile.Poligons.get(area);
                if(regions == null) continue;
                for(int k =0;  k< regions.size() ; k++){
                    Region polygon = regions.get(k);
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
                    canvas.drawPath(pathRegion, cusPaint);
                }
            }
        }
        //DRAW RIVER
        for(int lon = (int) pointT3.x ; lon<= (int) pointT1.x ; lon++) {
            for (int lat = (int) pointT3.y ; lat <= (int) pointT1.y ; lat++) {
                String area = lon + "-" + lat;
                Vector<Region> river;
                if(mScale < 8) {
                    river = ReadFile.BasePlgRiver.get(area);
                }
                else river = ReadFile.PolygonRivers.get(area);
                if(river == null) continue;
                for(int k =0;  k< river.size() ; k++){
                    Region polygon = river.get(k);
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
                    canvas.drawPath(pathRegion, riverPaint);
                }
            }
        }

        if(mScale > 6){
            for(int lon = (int) pointT3.x ; lon<= (int) pointT1.x ; lon++) {
                for (int lat = (int) pointT3.y ; lat <= (int) pointT1.y ; lat++) {
                    String area = lon + "-" + lat;
                    //draw polyline
                    Vector<Polyline> PL = ReadFile.PLines.get(area);
                    if (PL == null) continue;
                    for (int k = 0; k < PL.size(); k++) {
                        Polyline pl = PL.get(k);
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
                        canvas.drawLines(pointis, depthLinePaint);
                    }
                }
            }
        }

        if (MYLOCATION) {
            Bitmap mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.location_maps);
            Point p1 = ConvWGSToScrPoint(location_lon, location_lat);
            Paint locationPaint = new Paint();
            canvas.drawBitmap(mbitmap, p1.x, p1.y, locationPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
//        gestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            dragStart = new PointF(event.getX(),event.getY());

        }
        else if(event.getAction()==MotionEvent.ACTION_MOVE)
        {
            dragStop = new PointF(event.getX(),event.getY());

            PointF newLatLon = ConvScrPointToWGS((int)(dragStart.x-dragStop.x)+scrCtX,(int)(dragStart.y-dragStop.y)+scrCtY);
            mlat=newLatLon.y;
            mlon=newLatLon.x;
            dragStart = dragStop;
            //MainActivity.polygonsView.refreshDrawableState();
            invalidate();
            switch (MainActivity.CHOOSE_DISTANE_OR_ROUTE){
                case 0:{
                    break;
                }
                default:{
                    MainActivity.distancePTPView.invalidate();
                    break;
                }
            }
        }
        return true;
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

    public static Point ConvWGSToScrPoint(float m_Long,float m_Lat)// converting lat lon  WGS coordinates to screen XY coordinates
    {
        Point s = new Point();
        float refLat = (mlat + (m_Lat))*0.00872664625997f;//pi/360
        s.set((int )(mScale*((m_Long - mlon) * 111.31949079327357)*cos(refLat))+scrCtX,
                (int)(mScale*((mlat- (m_Lat)) * 111.132954))+scrCtY
        );
        return s;
    }
    public static PointF ConvScrPointToWGS(int x,int y)
    {
        float olat  = mlat -  (float)(((y-scrCtY)/mScale)/(111.132954f));
        float refLat = (mlat +(olat))*0.00872664625997f;//3.14159265358979324/180.0/2;
        float olon = (x-scrCtX)/mScale/(111.31949079327357f*(float)cos(refLat))+ mlon;
        return new PointF(olon,olat);
    }

    public void setLonLatMyLocation(float latLoc, float lonLoc){
        mlat = location_lat = latLoc;
        mlon = location_lon = lonLoc;
        mScale = 10;
        MYLOCATION =true;
        invalidate();
    }
}