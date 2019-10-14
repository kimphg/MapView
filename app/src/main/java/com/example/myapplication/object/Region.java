package com.example.myapplication.object;

import android.graphics.Point;
import android.graphics.PointF;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Region {
    public int numberPoint;
    public Map<Point, Vector<PointF>> lines = new HashMap<Point, Vector<PointF>>();
    public float brush[], location[];
    public int pen[];

    public Region(){}
    public Region(int inumberPoint){
        numberPoint = inumberPoint;
        pen = new int [3];
        brush = new float[3];
        location = new float[2];
    }
}
