package com.SeaMap.myapplication.classes;

import java.nio.ByteBuffer;

public class MapPointUser extends MapPoint {
    //time in seconds
    public Long mTimeSec;
    //Point type 0: safe 1: danger 2: fishing 3: location 4: empty 5: location history
    public int mType = 0;
    public static String separator = ",";
    public static int bytesSize = 32;//8 + 4 + 4 + 4 + 4 + 4 ;//time8 type4 lat4 lon4 pres4 presVar4
    public float mAirPressure=0,mAirPressureStd=0;
    public String mName;//, mDataString;
    MapPointUser(String name, String data)
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
    public MapPointUser(byte[] data)
    {
        if(data.length!= MapPointUser.bytesSize)return;
        ByteBuffer buffer = ByteBuffer.wrap(data);
        mTimeSec = buffer.getLong(0);
        mType = buffer.getInt(8);
        mlat = buffer.getFloat(12);
        mlon = buffer.getFloat(16);
        mAirPressure = buffer.getFloat(20);
        mAirPressureStd = buffer.getFloat(24);
    }
    public MapPointUser(MapPointUser newPoint) {
        mTimeSec = newPoint.mTimeSec;
        mType = newPoint.mType;
        mlat = newPoint.mlat;
        mlon = newPoint.mlon;
        mName = newPoint.mName;
        if(mName==null)mName=DataString();
    }
    public MapPointUser(float plat, float plon, String name, int type)
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
    public void copyData(MapPointUser newPoint)
    {
        mTimeSec = newPoint.mTimeSec;
        mType = newPoint.mType;
        mlat = newPoint.mlat;
        mlon = newPoint.mlon;
        mName = newPoint.mName;
        if(mName==null)mName=DataString();
    }


    public String DataString() {
        return String.valueOf( mlat) + separator + String.valueOf( mlon)+separator+String.valueOf( mType);
    }
}
