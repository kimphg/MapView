package com.Seamap.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Build;


import androidx.annotation.RequiresApi;

import com.Seamap.app.classes.ReadFile;
import com.Seamap.app.object.Density;

import java.util.Vector;

public class DensityView extends PolygonsView {

    public DensityView(Context context) {
        super(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        PointF pointT1 = ConvScrPointToWGS(scrCtX * 2,0);
        PointF pointT3 = ConvScrPointToWGS(0, scrCtY * 2);

        Paint pointDensity = new Paint();
        pointDensity.setColor(Color.RED);
        for(int lon = (int) pointT3.x ; lon<= (int) pointT1.x ; lon++) {
            for (int lat = (int) pointT3.y ; lat <= (int) pointT1.y ; lat++) {
                String area = lon + "-" + lat;
                Vector<Density> vtDensity = ReadFile.listDensity.get(area);
                if (vtDensity == null) continue;
                int size = vtDensity.size() ;
                float []pointf = new float[size * 2];
                for(int i =0; i < size * 2; i+= 2){
                    Point p1 = ConvWGSToScrPoint(vtDensity.get(i / 2).getLongitude(), vtDensity.get(i/2).getLatitude());
                    pointf[i] = p1.x;
                    pointf[i + 1] = p1.y;
                }
                canvas.drawPoints(pointf, pointDensity);
            }
        }
    }
}