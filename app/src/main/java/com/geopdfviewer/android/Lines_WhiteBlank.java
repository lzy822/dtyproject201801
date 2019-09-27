package com.geopdfviewer.android;

import org.litepal.crud.LitePalSupport;

/**
 * 白板线类
 * 用于存储白板的空间信息，序号，颜色和空间索引
 * 该内容有对应的数据表
 *
 * @author  李正洋
 */
public class Lines_WhiteBlank extends LitePalSupport {
    private String ic;
    private String lines;
    private int color;
    private int mmid;
    private float maxlat;
    private float maxlng;
    private float minlat;
    private float minlng;

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
