package com.example.myapplication.object;

import android.graphics.Point;
import android.graphics.PointF;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Polyline {
    public int numberPoint;
    public Map<Point, Vector<PointF>> lines = new HashMap<Point, Vector<PointF>>();
    public int pen[];

    public Polyline(){}

    public Polyline(int inumberPoint){
        numberPoint = inumberPoint;
        pen = new int[3];
    }
}
