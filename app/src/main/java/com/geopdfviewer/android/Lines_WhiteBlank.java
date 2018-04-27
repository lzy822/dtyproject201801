package com.geopdfviewer.android;

import org.litepal.crud.DataSupport;

public class Lines_WhiteBlank extends DataSupport {
    private String ic;
    private String lines;
    private int color;
    private int id;

    public String getIc() {
        return ic;
    }

    public void setIc(String ic) {
        this.ic = ic;
    }

    public String getLines() {
        return lines;
    }

    public void setLines(String lines) {
        this.lines = lines;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
