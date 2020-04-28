package com.SeaMap.myapplication.object;


public class Density {
    public float latitude, longitude;
    public int countMove;
    public boolean reducceRes;
    public Density(float latitude, float longitude, int countMove) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.countMove = countMove;
        this.reducceRes = (((int)(latitude*1000+longitude*1000))%4)==0;
    }
}
