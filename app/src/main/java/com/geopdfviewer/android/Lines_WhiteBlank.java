package com.geopdfviewer.android;

import org.litepal.crud.LitePalSupport;

public class Lines_WhiteBlank extends LitePalSupport {
    private String ic;
    private String lines;
    private int color;
    private int mmid;

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

    public int getMmid() {
        return mmid;
    }

    public void setMmid(int mmid) {
        this.mmid = mmid;
    }
}
