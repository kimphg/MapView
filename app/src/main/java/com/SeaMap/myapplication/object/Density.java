package com.SeaMap.myapplication.object;

public class Density {
    public float latitude, longitude;
    public short countMove;
    public boolean reduceRes;
    public Density(float latitude, float longitude, short countMove) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.countMove = countMove;
        this.reduceRes = (((int)(latitude*1000+longitude*1000))%4)==0;
    }
}
