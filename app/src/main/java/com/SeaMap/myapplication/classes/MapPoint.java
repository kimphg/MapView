package com.SeaMap.myapplication.classes;

public class MapPoint {
    public int type = 0;
    public static String separator = ",";
    public float mlat,mlon;
    public String mName,mlatlonString;
    MapPoint(String name,String latlon)
    {
        type = 0;
        if(name==null)mName = latlon;
        else mName = name;
        mlatlonString = latlon;
        String[] lat_lon = latlon.split(separator);
        if(lat_lon.length==2)
        {
            mlat = Float.parseFloat(lat_lon[0]);
            mlon = Float.parseFloat(lat_lon[1]);
        }

    }
    public MapPoint(float plat,float plon,String name)
    {
        type = 0;
        mlat = plat;
        mlon = plon;
        mName = name;
        mlatlonString=String.valueOf( mlat) + separator + String.valueOf( mlon);
        if(mName==null)mName=new String(mlatlonString);
    }
    public void SetPoint(MapPoint newPoint)
    {
        type = newPoint.type;
        mlat = newPoint.mlat;
        mlon = newPoint.mlon;
        mName = newPoint.mName;
        if(mName==null)mName=new String(mlatlonString);
        mlatlonString=newPoint.mlatlonString;
    }
}
