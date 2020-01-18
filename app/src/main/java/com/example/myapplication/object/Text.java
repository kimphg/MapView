package com.example.myapplication.object;

import android.graphics.PointF;

public class Text {

    public String name;
    public PointF point1, point2;
    public String font;
    public int pen[];
    public float angle ;
    public String location;
    public int type;

    public Text (){
        name = "";
        point1 = new PointF();
        point2 = new PointF();
        font = "";
        pen = new int [3];
        angle = 0;
        location = "";
        type = 0;
    }

    public Text(String iname,PointF ipoint1, PointF ipoint2, String ifont, float iangle, String ilocation){
        name = iname;
        point1 = ipoint1;
        point2 = ipoint2;
        font = ifont;
        angle = iangle;
        location = ilocation;
    }
}
