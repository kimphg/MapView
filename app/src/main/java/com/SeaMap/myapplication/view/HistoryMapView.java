package com.SeaMap.myapplication.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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
    }
    public void SetTimePeriod(long begin,long end){
        pointList = GlobalDataManager.getLocationHistory();
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        for (MapPoint point:pointList
             ) {
            DrawData(canvas,point);
        }

    }
    private void DrawData(Canvas canvas,MapPoint point)
    {
        PointF p1 = ConvWGSToScrPoint(point.mlon,point.mlat);
        p1.offset((float)buf_x, (float)buf_y);
        objectPaint.setColor(Color.argb(160, 20,200 , 20));
        drawCross(p1,objectPaint,canvas);
        canvas.drawText(point.mName,p1.x+pointSize*5,p1.y,objectPaint);
    }


}
