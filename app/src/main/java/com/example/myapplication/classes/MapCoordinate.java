package com.example.myapplication.classes;

import android.graphics.PointF;
import android.icu.text.Edits;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Vector;

public class MapCoordinate {
//    public PointF getCoordinate() {
//        return coordinate;
//    }
//
//    public void setCoordinate(PointF coordinate) {
//        this.coordinate = coordinate;
//    }

//    private PointF coordinate;

    private static double Haversine ( double angle ){
        return ( 1 - Math.cos( Math.toRadians( angle ) ) ) / 2;
    }

    //use this to calculate a route distance
    public static double distanceRoute( PointF point1, PointF point2 ){
        final double R = 6371;

        double latDiff = point1.y - point2.y;
        double longDiff = point1.x - point2.y;

        double centerAngleHaversine = Haversine( Math.toRadians( - latDiff ) )
                + Math.cos( Math.toRadians( point1.y ) ) * Math.cos( Math.toRadians( point2.y ) )
                * Haversine( Math.toRadians( - longDiff ) );

        return 2 * R * Math.asin( Math.sqrt( centerAngleHaversine ) );
    }

    private static double distanceSmall(PointF point1 , PointF point2){
        final double R = 6371;

        double dlat = R * Math.abs( point1.y - point2.y );
        double dlong = R * Math.cos( Math.toRadians( point2.y )) * Math.abs( point1.x - point2.x );

        double distance = Math.sqrt( Math.pow( dlat, 2 ) + Math.pow( dlong, 2 ));

        return distance;
    }

    //use this to calculate the distance of a vector
    public static double distanceOfVector(Vector< PointF > vector ){
        double distanceInTotal = 0;

        for( int i = 0; i < vector.size(); i++ ){
            distanceInTotal += distanceSmall( vector.elementAt( i ), vector.elementAt( i + 1 ));
        }

        return distanceInTotal;
    }

    //convert from kilometers to nautical miles
    public static double convertKmToNm( double value ){
        return value / 1.852d;
    }

    //convert from nautical miles to kilometers
    public static double convertNmToKm( double value ){
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
//    protected double distanceSmall(@org.jetbrains.annotations.NotNull MapCoordinate otherCoord ){
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
