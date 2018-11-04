package com.jellygom.mibandsdkdemo;

/**
 * Created by Tulio on 07-11-2017.
 */

public class LatLng {
    private Double lat;
    private Double lon;

    public LatLng(Double lat, Double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }
}
