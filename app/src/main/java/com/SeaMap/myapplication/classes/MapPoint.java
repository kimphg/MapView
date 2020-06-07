package com.SeaMap.myapplication.classes;

public class MapPoint {
    public float mlat,mlon;

    public MapPoint() {

    }

    public MapPoint(float lat,float lon)
    {
        mlat = lat;
        mlon = lon;
    }
    public float DistanceKmTo(MapPoint point)
    {
        return DistanceKmTo(point.mlat,point.mlon);
    }
    public float DistanceKmTo(float lat, float lon)
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
//        double deltaLat = Math.toRadians( lat2 - lat1 );
//        double deltaLon = Math.toRadians( lon2 - lon1 );
//
//        double haversine = Math.pow(Math.sin( deltaLat / 2 ), 2) +
//                Math.cos( Math.toRadians( lat1 ) ) * Math.cos( Math.toRadians( lat2 )) *
//                        Math.pow( Math.sin(deltaLon/2), 2);
//
//        return 2 * 6371 * Math.atan2( Math.sqrt( haversine ), Math.sqrt(1 - haversine));
    }
    public String getDMSString()
    {
        return decimalToDMS(mlon,mlat);
    }
    public static String decimalToDMS(double lon, double lat ){

        double convertLat = Math.abs( lat );
        int latDeg = (int) Math.floor( convertLat );
        int latMin = (int) Math.floor( (convertLat - latDeg ) * 60 );
        int latSec = (int) Math.floor( ( (convertLat - latDeg) * 60.0 - latMin)*60.0);
        String latCardinal = ( (lat > 0) ? "N" : "S");

        double convertLon = Math.abs( lon );
        int lonDeg = (int) Math.floor( convertLon );
        int lonMin = (int) Math.floor( (convertLon - lonDeg ) * 60 );
        int lonSec = (int)Math.floor( ( (convertLon - lonDeg ) * 60 - lonMin) *60.0);
        String lonCardinal = ( (lon > 0) ? "E" : "W");

        String result  = latDeg + "°" + latMin + "'" + latSec + "\"" + latCardinal+"/"+ lonDeg + "°" + lonMin + "'" + lonSec + "\"" + lonCardinal;

        return result;
    }
    public static String decimalToDMS(float value ) {
        int latDeg = (int) Math.floor( value );
        float latMin = (value - (float)latDeg ) * 60.0f ;
        return latDeg + "°" + latMin + "'";
    }
}
