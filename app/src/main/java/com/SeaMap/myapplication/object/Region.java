package com.SeaMap.myapplication.object;

import java.io.Serializable;

public class Region implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     *
     */
    private float coordinate[];
    private float  location[];
    private int brush[],pen[];
    private int type;

    public Region(int number) {
        pen = new int[3];
        brush = new int[3];
        location = new float[2];
        coordinate = new float[number];
        type = 0;
    }

    public float[] getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(float[] coordinate) {
        this.coordinate = coordinate;
    }

    public float[] getLocation() {
        return location;
    }

    public void setLocation(float[] location) {
        this.location = location;
    }

    public int[] getBrush() {
        return brush;
    }

    public void setBrush(int[] brush) {
        this.brush = brush;
    }

    public int[] getPen() {
        return pen;
    }

    public void setPen(int[] pen) {
        this.pen = pen;
    }

    public int getType() { return type; }

    public void setType(int type) { this.type = type; }
}
