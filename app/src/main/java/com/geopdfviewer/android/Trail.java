package com.geopdfviewer.android;

import org.litepal.crud.LitePalSupport;

import java.util.List;

/**
 * Created by 54286 on 2018/3/15.
 */

public class Trail extends LitePalSupport {
    private int id;
    private String ic;
    private String name;
    private String path;
    private String starttime;
    private String endtime;

    public String getStarttime() {
        return starttime;
    }

    public void setStarttime(String starttime) {
        this.starttime = starttime;
    }

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;
    }

    public String getIc() {
        return ic;
    }

    public void setIc(String ic) {
        this.ic = ic;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
