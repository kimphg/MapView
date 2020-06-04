package com.SeaMap.myapplication.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.SeaMap.myapplication.Activity.HistoryTimeInput;
import com.SeaMap.myapplication.Activity.MapPointEditor;
import com.SeaMap.myapplication.classes.GlobalDataManager;
import com.SeaMap.myapplication.classes.MapPoint;

import java.util.List;

public class HistoryMapView extends MapView {

    private List<MapPoint> pointList;
    public HistoryMapView(Context context) {
        super(context);
        pointList = GlobalDataManager.getLocationHistory();
        objectPaint.setStrokeWidth(Math.max(2, pointSize ));
        objectPaint.setColor(Color.argb(150, 50, 10, 0));
        objectPaint.setTextSize(pointSize*6);
        objectPaint.setStyle(Paint.Style.FILL);
    }
    public void SetTimePeriod(){
        Intent intent = new Intent(mCtx.getApplicationContext(), HistoryTimeInput.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mCtx.getApplicationContext().startActivity(intent);

        //pointList = GlobalDataManager.getLocationHistory();
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        DrawHistory(canvas);
    }
    private void DrawHistory(Canvas canvas)
    {
        Long timeStart = Long.parseLong(GlobalDataManager.GetConfig("history_start"));
        PointF tempPoint=null;
        objectPaint.setColor(Color.argb(100, 50, 10, 0));
        for (MapPoint point:pointList
        ) {
            //kiểm tra thời gian
            if(point.mTime<timeStart)continue;
            //vẽ
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
