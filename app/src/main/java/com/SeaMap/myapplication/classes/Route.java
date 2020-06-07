package com.SeaMap.myapplication.classes;

import android.location.Location;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Route {
    private ArrayList<MapPoint> route;
    private float totalDistanceKm;
    private static final float SPEED_THRESHOLD = 0.5f;/*m/s*/

    public Route() {
        this.route = new ArrayList<>();
        this.totalDistanceKm = 0;
    }

    public List<MapPoint> getRoute() {
        return route;
    }

    public float getTotalDistance(Location curLocation) {
        return totalDistanceKm +getNextDestinationDistance(curLocation);
    }

    public void addNewDestination(MapPoint newDestination) {
        this.route.add(newDestination);
        updateTotalDist();
    }

    public void arrivedToDestination() {
        if (!this.route.isEmpty()) {
            if( route.size() >= 2 ){
                this.totalDistanceKm -= route.get(0).DistanceKmTo(route.get(1));

            }
            this.route.remove(0);
        }
    }

    //distance under 50m is considered arrived at the destination
    public boolean isArrived(Location curLocation) {
        return (route.get(0).DistanceKmTo((float)curLocation.getLatitude(),(float)curLocation.getLongitude())
        ) * 1000 < 50;
    }

    public float getNextDestinationDistance(Location curLocation) {
        if( !route.isEmpty() ){
            MapPoint head = route.get(0);
            return head.DistanceKmTo((float)curLocation.getLatitude(),(float)curLocation.getLongitude());
        }
        else {
            return 0;
        }
    }

    public double getRemainingDistance( Location curLocation ){
        return getNextDestinationDistance(curLocation) + this.totalDistanceKm;
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

    public void RemoveRoutePoint(int i) {
        route.remove(i);
        updateTotalDist();
    }
    private void updateTotalDist()
    {
        totalDistanceKm = 0;
        MapPoint pointOld = null;
        for(MapPoint point :route)
        {
            if(pointOld!=null)
            {
                totalDistanceKm +=pointOld.DistanceKmTo(point);
            }
            pointOld = point;
        }
    }

    public boolean isEmpty() {
        return route.isEmpty();
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

