package com.SeaMap.myapplication.classes;

import android.location.Location;

import com.SeaMap.myapplication.services.GpsService;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Route {
    public List<Coordinate> route;
    private float totalDistance;
    private static final float SPEED_THRESHOLD = 1;/*m/s*/

    public Route() {
        this.route = new LinkedList<>();
        this.totalDistance = 0;
    }

    public List<Coordinate> getRoute() {
        return route;
    }

    public void setRoute(ArrayList<Coordinate> route) {
        this.route = route;
    }

    public float getTotalDistance() {
        return totalDistance;
    }

    public void addNewDestination(Coordinate newDestination) {
        if( !route.isEmpty() ){
            Coordinate last = route.get(route.size() - 1);
            this.totalDistance += GpsService.distance(
                    last.longitude,
                    last.latitude,
                    newDestination.longitude,
                    newDestination.latitude
            );
        }


        this.route.add(newDestination);
    }

    public void arrivedToDestination() {
        if (!this.route.isEmpty()) {
            if( route.size() >= 2 ){
                this.totalDistance -= GpsService.distance(
                        route.get(0).longitude,
                        route.get(0).latitude,
                        route.get(1).longitude,
                        route.get(1).latitude
                );
            }
            this.route.remove(0);
        }
    }

    //distance under 50m is considered arrived at the destination
    public boolean isArrived(Location curLocation) {
        return GpsService.distance(
                curLocation.getLongitude(),
                curLocation.getLatitude(),
                route.get(0).longitude,
                route.get(0).latitude
        ) * 1000 < 50;
    }

    public double getNextDestinationDistance(Location curLocation) {
        Coordinate head = route.get(0);
        return GpsService.distance(
                curLocation.getLongitude(),
                curLocation.getLatitude(),
                head.longitude,
                head.latitude
        );
    }

    public double getRemainingDistance( Location curLocation ){
        return getNextDestinationDistance(curLocation) + this.totalDistance;
    }

    public int getNextDestinationEta(Location curLocation) {
        if (route.isEmpty()) {
            return -2;
        } else if (curLocation.getSpeed() < SPEED_THRESHOLD) {
            return -1;
        } else {
            return (int) (getNextDestinationDistance(curLocation) * 1000 / curLocation.getSpeed());
        }
    }

    public void getTotalEta(Location curLocation) {
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

