package com.SeaMap.myapplication.object;

import java.io.Serializable;

public class Polyline implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public float[] getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(float[] coordinate) {
        this.coordinate = coordinate;
    }

    /**
     *
     */
    private float coordinate[];
    private int type;
    private int pen[];

    // public Polyline() {
    //     pen = new int[3];
    //     type = 0;
    // }

    public Polyline(int number) {
        coordinate = new float[number];
        pen = new int[3];
        type = 0;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int[] getPen() {
        return pen;
    }

    public void setPen(int[] pen) {
        this.pen = pen;
    }
}