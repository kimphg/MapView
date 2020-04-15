package com.SeaMap.myapplication.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Build;


import androidx.annotation.RequiresApi;

import com.SeaMap.myapplication.classes.ReadFile;
import com.SeaMap.myapplication.object.Density;

import java.util.Vector;

public class DensityMap extends PolygonsView {

    public DensityMap(Context context) {
        super(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        PointF pointT1 = ConvScrPointToWGS(scrCtX * 2,0);
        PointF pointT3 = ConvScrPointToWGS(0, scrCtY * 2);

        Paint pointDensity = new Paint();
        pointDensity.setColor(Color.rgb(255, 30, 30));
//        if(mScale < 15) pointDensity.setStrokeWidth(1f);
//        else pointDensity.setStrokeWidth(1.3f);
        for(int lon = (int) pointT3.x ; lon<= (int) pointT1.x ; lon++) {
            for (int lat = (int) pointT3.y ; lat <= (int) pointT1.y ; lat++) {
                String area = lon + "-" + lat;
                Vector<Density> vtDensity = ReadFile.listDensity.get(area);
                if (vtDensity == null) continue;
                int size = vtDensity.size() ;
//                float []pointf = new float[size * 2];
                for(int i =0; i < size * 2; i+= 2){
                    Density density = vtDensity.get(i / 2);
                    if(mScale < 10 && density.getCountMove() < 4) continue;
                    Point p1 = ConvWGSToScrPoint(density.getLongitude(), density.getLatitude());
//                    pointf[i] = p1.x;
//                    pointf[i + 1] = p1.y;

                    double brightness =  (density.getCountMove()/60.0);
                    if(brightness>1)brightness=1;
                    if(brightness<0.2)brightness=0.2;
                    pointDensity.setColor(Color.argb((int)(brightness*255),30, 230, 30));
                    pointDensity.setStrokeWidth((int) (  mScale /10.0));
                    canvas.drawPoint(p1.x, p1.y, pointDensity);
                }

//                canvas.drawPoints(pointf, pointDensity);
            }
        }
    }
}