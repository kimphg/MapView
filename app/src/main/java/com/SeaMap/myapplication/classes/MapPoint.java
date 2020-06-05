package com.SeaMap.myapplication.classes;

import java.nio.ByteBuffer;

public class MapPoint  {
    //time in seconds
    public Long mTimeSec;
    //Point type 0: safe 1: danger 2: fishing 3: location 4: empty 5: location history
    public int mType = 0;
    public static String separator = ",";
    public static int bytesSize = 32;//8 + 4 + 4 + 4 + 4 + 4 ;//time8 type4 lat4 lon4 pres4 presVar4
    public float mlat,mlon;
    public float mAirPressure=0,mAirPressureStd=0;
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
    public MapPoint(byte[] data)
    {
        if(data.length!=MapPoint.bytesSize)return;
        ByteBuffer buffer = ByteBuffer.wrap(data);
        mTimeSec = buffer.getLong(0);
        mType = buffer.getInt(8);
        mlat = buffer.getFloat(12);
        mlon = buffer.getFloat(16);
        mAirPressure = buffer.getFloat(20);
        mAirPressureStd = buffer.getFloat(24);
    }
    public MapPoint(MapPoint newPoint) {
        mTimeSec = newPoint.mTimeSec;
        mType = newPoint.mType;
        mlat = newPoint.mlat;
        mlon = newPoint.mlon;
        mName = newPoint.mName;
        if(mName==null)mName=DataString();
    }
    public MapPoint(float plat,float plon,String name,int type)
    {
        mTimeSec =System.currentTimeMillis()/1000;
        mType = type;
        mlat = plat;
        mlon = plon;
        mAirPressure = Float.parseFloat(GlobalDataManager.GetConfig("air_pressure"));//Float.parseFloat(GetConfig("air_pressure")
        mAirPressureStd = Float.parseFloat(GlobalDataManager.GetConfig("air_pressure_std"));
        mName = name;
        if(mName==null)mName=new String(String.valueOf( mlat) + separator + String.valueOf( mlon)+separator+String.valueOf( mType));
    }
    public byte[] toBytes()
    {
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytesSize);
        byteBuffer.putLong(mTimeSec);
        byteBuffer.putInt(mType);
        byteBuffer.putFloat(mlat);
        byteBuffer.putFloat(mlon);
        byteBuffer.putFloat(mAirPressure);
        byteBuffer.putFloat(mAirPressureStd);
        return byteBuffer.array();
    }
    public void copyData(MapPoint newPoint)
    {
        mTimeSec = newPoint.mTimeSec;
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
//        double f1 = mlat * 0.01745329252;
//        double f2 = mlon * 0.01745329252;
//        double dlat = (lat-mlat) * 0.01745329252;
//        double dlon = (lon-mlon) * 0.01745329252;
//        double a = Math.sin(dlat/2) * Math.sin(dlat/2) + Math.cos(f1) * Math.cos(f2) * Math.sin(dlon/2) * Math.sin(dlon/2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
//        return 6371000f * (float)c; // in metres
        float refLat = (lat + (mlat))*0.00872664625997f;//pi/360
        float x	= (float) ((((mlon) - lon) * 111.31949079327357f)*Math.cos(refLat));// 3.14159265358979324/180.0*6378.137);//deg*pi/180*rEarth
        float y	= ((mlat- lat ) * 111.132954f);
        return (float)Math.sqrt(x*x+y*y);
    }

    public String DataString() {
        return String.valueOf( mlat) + separator + String.valueOf( mlon)+separator+String.valueOf( mType);
    }
}
