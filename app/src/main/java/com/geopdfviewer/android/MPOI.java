package com.geopdfviewer.android;

import org.litepal.crud.LitePalSupport;

/**
 * 兴趣点类
 * 用于记录兴趣点数据
 * 该内容有对应的数据表，兴趣点表与照片要素表，录音要素表通过poic字段相连接
 *
 * @author  李正洋
 *
 * @since   1.1
 *
 * Created by 54286 on 2018/3/19.
 */
public class MPOI extends LitePalSupport {
    private long num;
    private float lat;
    private float lng;
    private String ImgPath;
    private double width;
    private double height;

    public long getNum() {
        return num;
    }

    public void setNum(long num) {
        this.num = num;
    }

    public String getImgPath() {
        return ImgPath;
    }

    public void setImgPath(String imgPath) {
        ImgPath = imgPath;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLng() {
        return lng;
    }

    public void setLng(float lng) {
        this.lng = lng;
    }
}
