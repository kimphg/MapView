package com.Seamap.app.object;

import java.io.Serializable;

public class Buoy implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     *
     */
    private float coordinates[];
    private String name;
    private String idFloats;

    public Buoy(float coordinates[], String name, String idFloats) {
        this.coordinates = coordinates;
        this.name = name;
        this.idFloats = idFloats;
    }

    public Buoy() {
        coordinates = new float[2];
        name = idFloats = "";
    }

    public float[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(float[] coordinates) {
        this.coordinates = coordinates;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdFloats() {
        return idFloats;
    }

    public void setIdFloats(String idFloats) {
        this.idFloats = idFloats;
    }
}
