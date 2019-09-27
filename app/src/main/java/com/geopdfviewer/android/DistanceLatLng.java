package com.geopdfviewer.android;

/**
 * 距离类
 * 用于存储当前子路径的距离
 */
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
