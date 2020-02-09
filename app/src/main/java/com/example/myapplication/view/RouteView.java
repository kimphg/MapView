package com.example.myapplication.view;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import com.example.myapplication.object.Text;

import java.util.ArrayList;

public class RouteView extends View {
    private int mScale;
    public ArrayList<Text> listPlace = new ArrayList<>();

    public RouteView(Context context) {
        super(context);
    }



    @Override
    protected void onDraw(Canvas canvas) {


        super.onDraw(canvas);
    }
}
