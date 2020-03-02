package com.SeaMap.myapplication.classes;

import android.graphics.PointF;

public class MapCoordinate {
    public PointF getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(PointF coordinate) {
        this.coordinate = coordinate;
    }

    private PointF coordinate;

    public double distanceToOtherCoord(PointF point1 , PointF point2){
        final double R = 6371;

        double dlat = R * Math.abs( point1.y - point2.y );
        double dlong = R * Math.abs( point1.x - point2.x );

        double distance = Math.sqrt( Math.pow( dlat, 2 ) + Math.pow( dlong, 2 ));

        return distance;
    }

    //convert from kilometers to nautical miles
    public double convertKmToNm( double value ){
        return value / 1.852d;
    }

    //convert from nautical miles to kilometers
    protected double convertNmToKm( double value ){
        return value * 1.852;
    }

}

//package com.example.myapplication.classes;
//
//        import android.graphics.PointF;
//
//public class MapCoordinate {
//    public PointF getCoordinate() {
//        return coordinate;
//    }
//
//    public void setCoordinate(PointF coordinate) {
//        this.coordinate = coordinate;
//    }
//
//    private PointF coordinate;
//
//    protected double distanceToOtherCoord(@org.jetbrains.annotations.NotNull MapCoordinate otherCoord ){
//        final double R = 6371;
//
//        double dlat = R * Math.abs( coordinate.y - otherCoord.coordinate.y );
//        double dlong = R * Math.abs( coordinate.x - otherCoord.coordinate.x );
//
//        double distance = Math.sqrt( Math.pow( dlat, 2 ) + Math.pow( dlong, 2 ));
//
//        return distance;
//    }
//
//    //convert from kilometers to nautical miles
//    protected double convertKmToNm( double value ){
//        return value / 1.852d;
//    }
//
//    //convert from nautical miles to kilometers
//    protected double convertNmToKm( double value ){
//        return value * 1.852;
//    }
//
//}
