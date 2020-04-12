package com.SeaMap.myapplication.classes;

import android.location.Location;

import com.SeaMap.myapplication.services.GpsService;

import java.util.ArrayList;

public class Route{
    public ArrayList<Coordinate> route;
    private float totalDistance;
    private static final float SPEED_THRESHOLD = 1;/*m/s*/

    public Route() {
        this.totalDistance = 0;
    }

    public ArrayList<Coordinate> getRoute() {
        return route;
    }

    public void setRoute(ArrayList<Coordinate> route) {
        this.route = route;
    }

    public float getTotalDistance() {
        return totalDistance;
    }

    public void addNewDestination( Coordinate newDestination ){
        Coordinate last = route.get( route.size() - 1 );
        this.totalDistance += GpsService.distance(
                last.longitude,
                last.latitude,
                newDestination.longitude,
                newDestination.latitude
        );

        this.route.add( newDestination );
    }

    //distance under 50m is considered arrived at the destination
    public boolean isArrived( Location curLocation, Coordinate destination ){
        return GpsService.distance(
                curLocation.getLongitude(),
                curLocation.getLatitude(),
                destination.longitude,
                destination.latitude
        ) / 1000 < 50;
    }

    public int getNextDestinationEta( Location curLocation ){
        if ( route.isEmpty() ){
            return 0;
        }
        else {
            Coordinate head = route.get( 0 );
            if( curLocation.getSpeed() < SPEED_THRESHOLD){
                return -1;
            }
            else{
                return (int)(GpsService.distance(
                        curLocation.getLongitude(),
                        curLocation.getLatitude(),
                        head.longitude,
                        head.latitude
                ) * 1000 / curLocation.getSpeed());
            }
        }
    }

    public void getTotalEta(Location curLocation){
    }
    //    private Date timeStart, timeEnd;
//    private int distanceEstimated = 0;
//    private Date timeEstimated;
//
//    public Route() {
//    }
//
//    //Lay ten cua tat ca dia diem
//    public ArrayList<String> namePlacesOnRoute(){
//        ArrayList<String> namePlace = new ArrayList<>();
//        for(int i =0; i< size(); i++){
//            namePlace.add(get(i).getName());
//        }
//        return namePlace;
//    }
//
//    public void swapPlaceOfRoute(int pos1, int pos2){
//        Text t1 = get(pos1);
//        Text t2 = get(pos2);
//        remove(pos1);
//        add(pos1,t2);
//        remove(pos2);
//        add(pos2,t1);
//    }
//
//
//    //tổng khoảng cách lộ trình ra km;
//    public float totaDistanceRuote_Km(){
//        return 1;
//    }
//
//    public float totaDistanceRuote_Nm(){
//        return 1;
//    }
//
//    public Date getTimeStart() {
//        return timeStart;
//    }
//
//    public Date getTimeEnd() {
//        return timeEnd;
//    }
//
//    public void setTimeEnd(Date timeEnd) {
//        this.timeEnd = timeEnd;
//    }
//
//    public void setTimeStart(Date timeStart) {
//        this.timeStart = timeStart;
//    }
}

