package com.Seamap.app.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.view.View;

import com.Seamap.app.R;

import java.util.ArrayList;

public class DistancePTPView extends View {
    private ArrayList<PointF> listCoor = new ArrayList<PointF>();
    private Paint linePaint = new Paint();

    private int mDistance;

    public DistancePTPView(Context context) {
        super(context);
        mDistance = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int size = listCoor.size();
        for(int i = 0; i< size; i++){
            Point p = SeaMap.ConvWGSToScrPoint(listCoor.get(i).x, listCoor.get(i).y);
            Bitmap mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pin);
            Paint locationPaint = new Paint();
            canvas.drawBitmap(mbitmap, p.x, p.y, locationPaint);
        }

        linePaint.setColor(Color.RED);

        if(size > 1){
            for(int i =0; i < size -1; i++){
                Point p1 = SeaMap.ConvWGSToScrPoint(listCoor.get(i).x, listCoor.get(i).y);
                Point p2 = SeaMap.ConvWGSToScrPoint(listCoor.get(i + 1).x, listCoor.get(i + 1).y);

                canvas.drawLine(p1.x, p1.y, p2.x, p2.y, linePaint);
            }
        }
    }

    public ArrayList<PointF> getListCoor() {
        return listCoor;
    }

    public void setListCoor(ArrayList<PointF> listCoor) {
        this.listCoor = listCoor;
    }

    public int getmDistance() {
        return mDistance;
    }
}
