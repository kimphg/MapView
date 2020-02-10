package com.example.myapplication.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.view.View;

import com.example.myapplication.R;
import com.example.myapplication.classes.ReadFile;
import com.example.myapplication.object.Buoy;

import java.util.Vector;

import static java.lang.Math.cos;

public class BuoyView extends View {

    private int scrCtX, scrCtY;
    public BuoyView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        scrCtY = getHeight() / 2;
        scrCtX = getWidth() / 2;

        PointF pointT1 = ConvScrPointToWGS(scrCtX * 2,0);
        PointF pointT3 = ConvScrPointToWGS(0, scrCtY * 2);

        for(int lon = (int) pointT3.x ; lon<= (int) pointT1.x  ; lon++) {
            for (int lat = (int) pointT3.y; lat <= (int) pointT1.y; lat++) {
                String area = lon + "-" + lat;
                Vector<Buoy> buoys = ReadFile.vtBuoys.get(area);
                if(buoys == null) continue;
                for(int k =0;  k< buoys.size() ; k++){
                    Point p = SeaMap.ConvWGSToScrPoint(buoys.get(k).getCoordinates()[0], buoys.get(k).getCoordinates()[1]);
                    Bitmap mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_buoy);

                    Paint locationPaint = new Paint();
                    canvas.drawBitmap(mbitmap, p.x, p.y, locationPaint);
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
