package com.geopdfviewer.android;

import java.util.List;

public class MLOI extends FOI {
    private List<String> multiline;
    private int linenum;
    private float maxlat;
    private float maxlng;
    private float minlat;
    private float minlng;

    public List<String> getMultiline() {
        return multiline;
    }

    public void setMultiline(List<String> multiline) {
        this.multiline = multiline;
    }

    public int getLinenum() {
        return linenum;
    }

    public void setLinenum(int linenum) {
        this.linenum = linenum;
    }

    public float getMaxlat() {
        return maxlat;
    }

    public void setMaxlat(float maxlat) {
        this.maxlat = maxlat;
    }

    public float getMaxlng() {
        return maxlng;
    }

    public void setMaxlng(float maxlng) {
        this.maxlng = maxlng;
    }

    public float getMinlat() {
        return minlat;
    }

    public void setMinlat(float minlat) {
        this.minlat = minlat;
    }

    public float getMinlng() {
        return minlng;
    }

    public void setMinlng(float minlng) {
        this.minlng = minlng;
    }
}
