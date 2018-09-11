package com.geopdfviewer.android;

import org.litepal.crud.LitePalSupport;

public class IconDataset extends LitePalSupport{
    private String path;
    private String name;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
