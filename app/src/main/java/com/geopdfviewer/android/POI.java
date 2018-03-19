package com.geopdfviewer.android;

import org.litepal.crud.DataSupport;

/**
 * Created by 54286 on 2018/3/15.
 */

public class POI  extends DataSupport {
    private int id;
    private String ic;
    private String name;
    private String path;
    private float path_x;
    private float path_y;
    private float x;
    private float y;
    private long time;

    public String getIc() {
        return ic;
    }

    public void setIc(String ic) {
        this.ic = ic;
    }

    public float getPath_x() {
        return path_x;
    }

    public void setPath_x(float path_x) {
        this.path_x = path_x;
    }

    public float getPath_y() {
        return path_y;
    }

    public void setPath_y(float path_y) {
        this.path_y = path_y;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
