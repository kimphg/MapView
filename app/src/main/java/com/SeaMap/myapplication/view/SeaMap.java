package com.SeaMap.myapplication.view;

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

import androidx.annotation.RequiresApi;

import com.SeaMap.myapplication.R;
import com.SeaMap.myapplication.classes.ReadFile;
import com.SeaMap.myapplication.object.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static java.lang.Math.abs;

public class SeaMap  extends PolygonsView {

    List<Text> ListPlace = new ArrayList<>();

    Paint textPaint = new Paint();
    Path mPath= new Path();
    Paint cusPaint = new Paint();

    public SeaMap(Context context) {
        super(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);

        PointF pointT1 = ConvScrPointToWGS(scrCtX * 2,0);
        PointF pointT3 = ConvScrPointToWGS(0, scrCtY * 2);

        cusPaint.setAntiAlias(true);
        cusPaint.setStyle(Paint.Style.STROKE);
        cusPaint.setColor(Color.rgb(255, 239, 213));
        cusPaint.setStyle(Paint.Style.FILL);

        for(int lon = (int) pointT3.x ; lon<= (int) pointT1.x  ; lon++) {
            for (int lat = (int) pointT3.y; lat <= (int) pointT1.y; lat++) {
                String area = lon + "-" + lat;
//                //draw text
                Vector<Text> tT = ReadFile.tTexts.get(area);
                if (tT == null) continue;
                for (int k = 0; k < tT.size(); k++) {
                    Text text = tT.get(k);
                    if(text.getName().length() == 0) continue;
                    if (mScale < 5 && text.getType() != 3) continue;
                    Point p1 = ConvWGSToScrPoint(text.getCoordinate()[0], text.getCoordinate()[1]);
                    Point p2 = ConvWGSToScrPoint(text.getCoordinate()[2], text.getCoordinate()[3]);
                    int distance = Distance(p1, p2);
                    int color = text.getPen()[2];
                    int red = (int) color / 65536;
                    int green = (int) (color - red * 65536) / 256;
                    int blue = (int) (color - red * 65536 - green * 256);
                    textPaint.setColor(Color.rgb(red, green, blue));
                    mPath = new Path();
                    mPath.moveTo(p1.x, p1.y);
                    mPath.lineTo(p2.x, p2.y);
                    int textSize = (int) distance * 2 / text.getName().length();
                    if(textSize > scrCtX )continue;
                    if(textSize < scrCtX * 0.05)continue;

                    if (mScale <= 4f && text.getType() == 3) {
                        textPaint.setTextSize((int) distance * 2 / text.getName().length());
                        canvas.drawText(text.getName(), p1.x, p1.y, textPaint);
                    } else if (mScale > 2 && mScale <= 10 && text.getType() == 0) {
                        textPaint.setTextSize((int) distance * 2 / text.getName().length());
                        canvas.drawTextOnPath(text.getName(), mPath, 0, 0, textPaint);
                    } else if (mScale > 6 && mScale <= 12 && text.getType() == 4) {
                        textPaint.setTextSize(distance / text.getName().length());
                        canvas.drawTextOnPath(text.getName(), mPath, 0, 0, textPaint);
                    } else if (mScale > 8 && text.getType() == 1) {
                        if (text.getName().length() != 0)
                            textPaint.setTextSize(distance / text.getName().length());
                        canvas.drawTextOnPath(text.getName(), mPath, 0, 0, textPaint);
                    }
                }
            }
        }
    }

    private int Distance(Point p1, Point p2){
        return (int) Math.sqrt(Math.pow((p1.x -p2.x),2) + Math.pow((p1.y - p2.y),2));
    }
}
