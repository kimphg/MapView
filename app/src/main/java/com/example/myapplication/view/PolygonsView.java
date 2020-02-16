package com.example.myapplication.view;

import android.content.Context;
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

import com.example.myapplication.Activity.MainActivity;
import com.example.myapplication.classes.ReadFile;
import com.example.myapplication.object.Polyline;
import com.example.myapplication.object.Region;

import java.util.Vector;

import static java.lang.Math.cos;

public class PolygonsView extends View {

    public static float mlat = 18.32f;//lattitude of the center of the screen
    public static float mlon = 105.43f;//longtitude of the center of the screen
    public static float mScale = 3f;// 1km = mScale*pixcels

    private static int scrCtY,scrCtX;

    private Paint cusPaint = new Paint(), depthLinePaint = new Paint();
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
        cusPaint.setColor(Color.rgb(255, 239, 213));

        for(int lon = (int) pointT3.x - 2; lon<= (int) pointT1.x + 1; lon++) {
            for (int lat = (int) pointT3.y - 2; lat <= (int) pointT1.y + 1; lat++) {
                String area = lon + "-" + lat;
                Vector<Region> rE = ReadFile.Poligons.get(area);
                if(rE == null) continue;
                for(int k =0;  k< rE.size() ; k++){
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
                    canvas.drawPath(pathRegion, cusPaint);
                }
            }
        }

        for(int lon = (int) pointT3.x - 2; lon<= (int) pointT1.x + 1; lon++) {
            for (int lat = (int) pointT3.y - 2; lat <= (int) pointT1.y + 1; lat++) {
                String area = lon + "-" + lat;
                //draw polyline
                Vector<Polyline> PL = ReadFile.PLines.get(area);
                if (PL == null) continue;
                for (int k = 0; k < PL.size(); k++) {
                    Polyline pl = PL.get(k);
                    if (PL.get(k) != null) {
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
                        canvas.drawLines(pointis, depthLinePaint);

                    }
                }
            }
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
}
