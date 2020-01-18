package com.example.myapplication.object;

import android.graphics.Point;
import android.graphics.PointF;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Texts {
    @SerializedName("coordinates")
    private List<Float> coordinates;

    private String name;
    private String font;
    private int pen[];
    private float angle;
    private String location;

    //type 0: ten cac thanh pho
    //type 1: do sau
    //type 2: hon dao nho
    //type 3: quan dao
    private int type;

    public Texts() {
        pen = new int[3];
        coordinates = new ArrayList<>();
        type = 0;
    }

    public void setCoordinates(Vector<Float> icoordinates){
        this.coordinates = icoordinates;
    }

    public void setPen(int ipen[]){
        this.pen = ipen;
    }

    public List<Float> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Float> coordinates) {
        this.coordinates = coordinates;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public int[] getPen() {
        return pen;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int hashCode(){
        return this.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        Texts t = (Texts) obj;
        return this.name.equals(t);
    }
}
