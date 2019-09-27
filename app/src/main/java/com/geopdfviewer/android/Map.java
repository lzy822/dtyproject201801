package com.geopdfviewer.android;

import org.litepal.crud.LitePalSupport;

/**
 * 地图数据存储类
 * 用于存储geopdf的基础数据内容
 * 该内容有对应的数据表
 *
 * 该数据取自geopdf中的内容
 *
 * @author  李正洋
 *
 * @since   1.1
 */
public class Map extends LitePalSupport {
    private String name;
    private String wkt;
    private String uri;
    private String gpts;
    private String bbox;
    private String mediabox;
    private String cropbox;
    private String imguri;
    private String ic;
    private String position;
    private int maptype;

    public Map(String name, String wkt, String uri, String gpts, String bbox, String mediabox, String cropbox, String imguri, String ic, String position, int maptype) {
        this.name = name;
        this.wkt = wkt;
        this.uri = uri;
        this.gpts = gpts;
        this.bbox = bbox;
        this.mediabox = mediabox;
        this.cropbox = cropbox;
        this.imguri = imguri;
        this.ic = ic;
        this.position = position;
        this.maptype = maptype;
    }

    public int getMaptype() {
        return maptype;
    }

    public void setMaptype(int maptype) {
        this.maptype = maptype;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWkt() {
        return wkt;
    }

    public void setWkt(String wkt) {
        this.wkt = wkt;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getGpts() {
        return gpts;
    }

    public void setGpts(String gpts) {
        this.gpts = gpts;
    }

    public String getBbox() {
        return bbox;
    }

    public void setBbox(String bbox) {
        this.bbox = bbox;
    }

    public String getMediabox() {
        return mediabox;
    }

    public void setMediabox(String mediabox) {
        this.mediabox = mediabox;
    }

    public String getCropbox() {
        return cropbox;
    }

    public void setCropbox(String cropbox) {
        this.cropbox = cropbox;
    }

    public String getImguri() {
        return imguri;
    }

    public void setImguri(String imguri) {
        this.imguri = imguri;
    }

    public String getIc() {
        return ic;
    }

    public void setIc(String ic) {
        this.ic = ic;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}