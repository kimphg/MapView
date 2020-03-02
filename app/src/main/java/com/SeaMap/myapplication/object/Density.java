package com.Seamap.app.object;

import java.io.Serializable;

public class Density implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     *
     */
    private float latitude, longitude;
    private int countMove;

    public Density(float latitude, float longitude, int countMove) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.countMove = countMove;
    }

    public Density() {
        latitude = longitude = countMove = 0;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public int getCountMove() {
        return countMove;
    }

    public void setCountMove(int countMove) {
        this.countMove = countMove;
    }
}
