package com.example.myapplication.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.view.View;

import com.example.myapplication.classes.ReadFile;
import com.example.myapplication.object.Region;

import java.util.Vector;

import static java.lang.Math.cos;

public class PolygonsView extends View {

    private Paint cusPaint = new Paint();
    public PolygonsView(Context context) {
        super(context);
    }
    private int scrCtY,scrCtX;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        scrCtY = getHeight() / 2;
        scrCtX = getWidth() / 2;

        PointF pointT1 = ConvScrPointToWGS(scrCtX * 2,0);
        PointF pointT3 = ConvScrPointToWGS(0, scrCtY * 2);

        cusPaint.setAntiAlias(true);
        cusPaint.setStyle(Paint.Style.FILL);
        cusPaint.setColor(Color.rgb(255, 239, 213));

        for(int lon = (int) pointT3.x ; lon<= (int) pointT1.x  ; lon++) {
            for (int lat = (int) pointT3.y; lat <= (int) pointT1.y; lat++) {
                String area = lon + "-" + lat;
                Vector<Region> rE = ReadFile.Poligons.get(area);
                if(rE == null) continue;
                for(int k =0;  k< rE.size() ; k++){
                    Region polygon = rE.get(k);
                    if(polygon == null) continue;
                    Path pathRegion = new Path();
                    for (int i = 0; i < polygon.getCoordinate().length; i = i + 2) {
                        if (i == 0) {
                            Point point1 = SeaMap.ConvWGSToScrPoint(polygon.getCoordinate()[i], polygon.getCoordinate()[i + 1]);
                            pathRegion.moveTo(point1.x, point1.y);
                        } else {
                            Point point1 = SeaMap.ConvWGSToScrPoint(polygon.getCoordinate()[i], polygon.getCoordinate()[i + 1]);
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
            }
        }
    }

    public PointF ConvScrPointToWGS(int x,int y)
    {
        float olat  = SeaMap.mlat -  (float)(((y-scrCtY)/SeaMap.mScale)/(111.132954f));
        float refLat = (SeaMap.mlat +(olat))*0.00872664625997f;//3.14159265358979324/180.0/2;
        float olon = (x-scrCtX)/SeaMap.mScale/(111.31949079327357f*(float)cos(refLat))+ SeaMap.mlon;
        return new PointF(olon,olat);
    }
}
