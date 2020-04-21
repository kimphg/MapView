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

    public static String[] decimalToDMS(double lon, double lat ){
        String[] result = new String[2];
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

        result[0] = latDeg + "°" + latMin + "'" + latSec + "\"" + latCardinal;
        result[1] = lonDeg + "°" + lonMin + "'" + lonSec + "\"" + lonCardinal;

        return result;
    }


}
