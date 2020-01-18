package com.example.myapplication.object;

import android.graphics.Point;
import android.graphics.PointF;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Polylines {
    @SerializedName("coordinates")
    private List<Float> coordinates;
    @SerializedName("pen")
    private int pen[];
    private int type;

    public Polylines() {
        pen = new int[3];
        coordinates = new ArrayList<>();
    }

    public List<Float> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Float> coordinates) {
        this.coordinates = coordinates;
    }

    public int[] getPen() {
        return pen;
    }

    public void setPen(int[] pen) {
        this.pen = pen;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
