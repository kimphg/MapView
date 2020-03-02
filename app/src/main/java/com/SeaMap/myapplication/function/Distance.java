package com.Seamap.app.function;


import com.Seamap.app.classes.Coordinate;

import java.util.List;


public class Distance {

    public Distance(){};

    //convert radian
    private double ConvertToRadian(double degrees){
        return degrees* (Math.PI /180);
    }


    //distance between two point
    public double DistanceHaversine(Coordinate point1, Coordinate point2){
        //earth radius
        double EarthRadiusKm = 6371;
        double dlat = ConvertToRadian(point1.x - point2.x);
        double dlon = ConvertToRadian(point1.y - point2.y);

        double latRadian1 = ConvertToRadian(point1.x);
        double latRadian2 = ConvertToRadian(point2.x);



        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) + Math.sin(dlon / 2) * Math.sin(dlon / 2) * Math.cos(latRadian1) * Math.cos(latRadian2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return c*EarthRadiusKm;

    }


    //distance between multi point use harversine : trouble : multi point have many odd
    //need to find a solution

    public double DistanceMultiPoint(List<Coordinate> pointList)
    {
        double Distance  = 0;
        for (int i = 0; i < pointList.size()-1;i++)
        {
            Distance += DistanceHaversine(pointList.get(i),pointList.get(i+1));

        }
        return Distance;

    }




}