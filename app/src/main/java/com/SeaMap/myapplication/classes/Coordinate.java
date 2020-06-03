package com.SeaMap.myapplication.classes;

import android.location.Location;

public class Coordinate {

    public double longitude;
    public double latitude;
    public Coordinate(){};
    public Coordinate(double longitude, double latitude){
        this.longitude = longitude;
        this.latitude = latitude;
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
