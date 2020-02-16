package com.example.myapplication.object;

import android.graphics.PointF;

import java.io.Serializable;

public class Line implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     *
     */
    private float coordinates[];
    private int pen[];

    public Line(float icoordinates[]) {
        coordinates = icoordinates;
    }

    public Line() {
        coordinates = new float [4];
        pen = new int[3];
    }

    public float[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(float[] coordinates) {
        this.coordinates = coordinates;
    }

    public int[] getPen() {
        return pen;
    }

    public void setPen(int[] pen) {
        this.pen = pen;
    }
}
