package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class Location extends View {

    private float longitiudeScr, latitudeScr;
    Paint circlePain;

    public Location(Context context, AttributeSet attrs) {
        super(context, attrs);
        circlePain = new Paint();
    }

    @Override
    public void onDraw(Canvas canvas){
        circlePain.setColor(Color.BLUE);
        canvas.drawCircle(latitudeScr, latitudeScr,10, circlePain);
    }

    public void setLonLat(float lonScr, float latScr){
        longitiudeScr = lonScr;
        latitudeScr =latScr;
    }


}
