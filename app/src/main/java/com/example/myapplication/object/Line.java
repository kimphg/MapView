package com.example.myapplication.object;

import android.graphics.PointF;

public class Line {
    public PointF point1, point2;
    public int pen[];
    public Line(PointF ipoint1,  PointF ipoint2){
        point1 = ipoint1;
        point2 = ipoint2;
    }
    public Line (){
        point1 = new PointF();
        point2 = new PointF();
        pen = new int [3];
    }
}
