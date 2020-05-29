package com.SeaMap.myapplication.classes;

import java.io.Serializable;

public class MapPoint implements Serializable {
    //time in seconds
    public Long mTime;
    public int mType = 0;
    public static String separator = ",";
    public float mlat,mlon;
    public String mName;//, mDataString;
    MapPoint(String name,String data)
    {
        mName = name;
        String[] lat_lon_type = data.split(separator);
        if(lat_lon_type.length>=3)
        {
            mlat = Float.parseFloat(lat_lon_type[0]);
            mlon = Float.parseFloat(lat_lon_type[1]);
            mType = Integer.parseInt(lat_lon_type[2]);

        }

    }

    public MapPoint(float plat,float plon,String name,int type,Long time)
    {
        if(time==0L)mTime=System.currentTimeMillis()/1000;
        else mTime = time;
        mType = type;
        mlat = plat;
        mlon = plon;
        mName = name;
        if(mName==null)mName=new String(String.valueOf( mlat) + separator + String.valueOf( mlon)+separator+String.valueOf( mType));
    }
    public void SetPoint(MapPoint newPoint)
    {
        mTime = newPoint.mTime;
        mType = newPoint.mType;
        mlat = newPoint.mlat;
        mlon = newPoint.mlon;
        mName = newPoint.mName;
        if(mName==null)mName=DataString();
    }
    public float DistanceTo(MapPoint point)
    {
        return DistanceTo(point.mlat,point.mlon);
    }
    public float DistanceTo(float lat,float lon)
    {
        double f1 = mlat * 0.01745329252;
        double f2 = mlon * 0.01745329252;
        double dlat = (lat-mlat) * 0.01745329252;
        double dlon = (lon-mlon) * 0.01745329252;
        double a = Math.sin(dlat/2) * Math.sin(dlat/2) + Math.cos(f1) * Math.cos(f2) * Math.sin(dlon/2) * Math.sin(dlon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return 6371000f * (float)c; // in metres
    }

    public String DataString() {
        return String.valueOf( mlat) + separator + String.valueOf( mlon)+separator+String.valueOf( mType);
    }
}
