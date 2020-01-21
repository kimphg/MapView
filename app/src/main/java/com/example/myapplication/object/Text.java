package com.example.myapplication.object;


import java.io.Serializable;
import java.util.Comparator;

public class Text implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String name;
    private float coordinate [];
    private String font;
    private int pen[];
    private float angle;
    private String location;
    //type 0: ten cac thanh pho
    //type 1: do sau
    //type 2: hon dao nho
    //type 3: quan dao
    private int type;

    public Text() {
        name = "";
        coordinate = new float[4];
        font = "";
        pen = new int[3];
        angle = 0;
        location = "";
    }

    public Text(String iname, float icoordinates [], String ifont, float iangle, String ilocation) {
        name = iname;
        coordinate = icoordinates;
        font = ifont;
        angle = iangle;
        location = ilocation;
        type = 0;
    }

    @Override
    public int hashCode(){
        return this.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        Text t = (Text) obj;
        return this.name.equals(t);
    }

    public static class PlaceComparator implements Comparator<Text>{

        @Override
        public int compare(Text o1, Text o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float[] getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(float[] coordinate) {
        this.coordinate = coordinate;
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

    public void setPen(int[] pen) {
        this.pen = pen;
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
}


