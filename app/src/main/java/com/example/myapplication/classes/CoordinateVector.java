package com.example.myapplication.classes;

import java.util.Vector;

public class CoordinateVector extends MapCoordinate{
    private Vector< MapCoordinate > coordinateVector;
    //kilometer in default
    private double vectorLength;

    public double getVectorLength(){
        vectorLength = 0;
        for( int i = 0; i < this.coordinateVector.size(); i++ ){
            vectorLength += this.coordinateVector.elementAt( i ).distanceToOtherCoord( coordinateVector.elementAt( i + 1 ));

        }
        return vectorLength;
    }
}
