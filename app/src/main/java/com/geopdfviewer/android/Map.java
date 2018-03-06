package com.geopdfviewer.android;

import android.net.Uri;

/**
 * Created by 54286 on 2018/3/5.
 */

public class Map {
    private String name;
    private Uri uri;
    private String WKT;

    public Map(String name, int Uri, String WKT) {
        this.name = name;
        this.uri = uri;
        this.WKT = WKT;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWKT() {
        return WKT;
    }

    public void setWKT(String WKT) {
        this.WKT = WKT;
    }

    public Uri getImageId() {
        return uri;
    }

    public void setImageId(Uri uri) {
        this.uri = uri;
    }
}
