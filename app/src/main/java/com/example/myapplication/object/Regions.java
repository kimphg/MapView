package com.example.myapplication.object;

import android.graphics.Point;
import android.graphics.PointF;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Regions {
    @SerializedName("coordinates")
    private List<Float> coordinates;
    private int brush[],pen[];
    private float  location[];


    public Regions() {
        pen = new int[3];
        brush = new int[3];
        location = new float[2];
        coordinates = new ArrayList<>();
    }

    public List<Float> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Float> coordinates) {
        this.coordinates = coordinates;
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

    public float[] getLocation() {
        return location;
    }

    public void setLocation(float[] location) {
        this.location = location;
    }
}
