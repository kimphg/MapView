package com.Seamap.app.classes;

import com.Seamap.app.object.Text;

import java.util.ArrayList;
import java.util.Date;

public class Route extends ArrayList<Text> {

    private Date timeStart, timeEnd;
    private int distanceEstimated = 0;
    private Date timeEstimated;

    public Route() {
    }

    //Lay ten cua tat ca dia diem
    public ArrayList<String> namePlacesOnRoute(){
        ArrayList<String> namePlace = new ArrayList<>();
        for(int i =0; i< size(); i++){
            namePlace.add(get(i).getName());
        }
        return namePlace;
    }

    public void swapPlaceOfRoute(int pos1, int pos2){
        Text t1 = get(pos1);
        Text t2 = get(pos2);
        remove(pos1);
        add(pos1,t2);
        remove(pos2);
        add(pos2,t1);
    }


    //tổng khoảng cách lộ trình ra km;
    public float totaDistanceRuote_Km(){
        return 1;
    }

    public float totaDistanceRuote_Nm(){
        return 1;
    }

    public Date getTimeStart() {
        return timeStart;
    }

    public Date getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(Date timeEnd) {
        this.timeEnd = timeEnd;
    }

    public void setTimeStart(Date timeStart) {
        this.timeStart = timeStart;
    }
}

