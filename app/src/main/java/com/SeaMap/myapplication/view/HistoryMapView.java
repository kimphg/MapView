package com.SeaMap.myapplication.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.SeaMap.myapplication.classes.GlobalDataManager;
import com.SeaMap.myapplication.classes.MapPoint;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

public class HistoryMapView extends MapView {

    ArrayList<MapPoint> pointList = new ArrayList<>();
    public HistoryMapView(Context context) {
        super(context);
        pointList = GlobalDataManager.getLocationHistory();
        objectPaint.setStrokeWidth(Math.max(2, pointSize ));
        objectPaint.setColor(Color.argb(150, 50, 10, 0));
        objectPaint.setTextSize(pointSize*6);
        objectPaint.setStyle(Paint.Style.FILL);
    }
    public void SetTimePeriod(long begin,long end){
        pointList = GlobalDataManager.getLocationHistory();
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        PointF tempPoint=null;
        objectPaint.setColor(Color.argb(150, 50, 10, 0));
        for (MapPoint point:pointList
             ) {
            if(tempPoint==null)tempPoint = ConvWGSToScrPoint(point.mlon,point.mlat);
            else
            {
                PointF p1 = ConvWGSToScrPoint(point.mlon,point.mlat);
                canvas.drawLine(p1.x,p1.y,tempPoint.x,tempPoint.y,objectPaint);
                tempPoint=p1;
            }
        }

    }



}
