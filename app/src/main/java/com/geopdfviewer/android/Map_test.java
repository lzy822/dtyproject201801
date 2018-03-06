package com.geopdfviewer.android;

import android.net.Uri;

/**
 * Created by 54286 on 2018/3/6.
 */

public class Map_test {
    private String name;
    private String WKT;
    private Uri uri;
    public Map_test(String name, String WKT, Uri uri) {
        this.name = name;
        this.WKT = WKT;
        this.uri = uri;
    }
    public Map_test(String name) {
        this.name = name;
        //this.WKT = WKT;
        //this.uri = uri;
    }
    public Map_test(String name, Uri uri) {
        this.name = name;
        //this.WKT = WKT;
        this.uri = uri;
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

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
