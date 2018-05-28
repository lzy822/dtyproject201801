package com.geopdfviewer.android;

public class DistanceLatLng extends LatLng {
    private float distance;

    public DistanceLatLng(float latitude, float longitude, float distance) {
        super(latitude, longitude);
        this.distance = distance;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
