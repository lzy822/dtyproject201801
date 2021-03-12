package com.geopdfviewer.android;

import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * 地图数据缓存类
 * 用于缓存geopdf的基础数据内容
 *
 * 该数据取自geopdf中的内容
 *
 * @author  李正洋
 *
 * @since   1.1
 */

public class Map_test implements Comparable<Map_test>{
    private String m_name;
    private int m_num;
    private String m_GPTS;
    private String m_BBox;
    private String m_WKT;
    private String m_uri;
    private String m_imguri;
    private String m_MediaBox;
    private String m_CropBox;
    private String m_ic;
    private String m_center_latlong;
    private int MapType;
    private int page;

    @Override
    public int compareTo(@NonNull Map_test map_test) {
        return this.page - map_test.getPage();
    }

    public int getMapType() {
        return MapType;
    }

    public void setMapType(int mapType) {
        MapType = mapType;
    }

    public Map_test(String m_name, int m_num, String m_GPTS, String m_BBox, String m_WKT, String m_uri, String m_imguri, String m_MediaBox, String m_CropBox, String m_ic, String m_center_latlong, int MapType) {
        this.m_name = m_name;
        this.m_num = m_num;
        this.m_GPTS = m_GPTS;
        this.m_BBox = m_BBox;
        this.m_WKT = m_WKT;
        this.m_uri = m_uri;
        this.m_imguri = m_imguri;
        this.m_MediaBox = m_MediaBox;
        this.m_CropBox = m_CropBox;
        this.m_ic = m_ic;
        this.m_center_latlong = m_center_latlong;
        this.MapType = MapType;
    }

    public Map_test(String m_name, int m_num, String m_GPTS, String m_BBox, String m_WKT, String m_uri, String m_imguri, String m_MediaBox, String m_CropBox, String m_ic, String m_center_latlong, int MapType, int page) {
        this.m_name = m_name;
        this.m_num = m_num;
        this.m_GPTS = m_GPTS;
        this.m_BBox = m_BBox;
        this.m_WKT = m_WKT;
        this.m_uri = m_uri;
        this.m_imguri = m_imguri;
        this.m_MediaBox = m_MediaBox;
        this.m_CropBox = m_CropBox;
        this.m_ic = m_ic;
        this.m_center_latlong = m_center_latlong;
        this.MapType = MapType;
        this.page = page;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getM_center_latlong() {
        return m_center_latlong;
    }

    public void setM_center_latlong(String m_center_latlong) {
        this.m_center_latlong = m_center_latlong;
    }

    public String getM_ic() {
        return m_ic;
    }

    public Map_test(String m_name, int mapType) {
        this.m_name = m_name;
        MapType = mapType;
    }

    public void setM_ic(String m_ic) {
        this.m_ic = m_ic;
    }

    public Map_test(int num, String name, String WKT, String uri, String GPTS, String BBox, String imguri) {
        m_BBox = BBox;
        m_num = num;
        m_name = name;
        m_WKT = WKT;
        m_uri = uri;
        m_GPTS = GPTS;
        m_imguri = imguri;
    }
    public Map_test(int num, String name, String WKT, String uri, String GPTS, String BBox, String imguri, String MediaBox, String CropBox) {
        m_BBox = BBox;
        m_num = num;
        m_name = name;
        m_WKT = WKT;
        m_uri = uri;
        m_GPTS = GPTS;
        m_imguri = imguri;
        m_MediaBox = MediaBox;
        m_CropBox = CropBox;
    }

    public Map_test(int num, String name, String WKT, String uri, String GPTS, String BBox, String imguri, String MediaBox, String CropBox, String ic) {
        m_BBox = BBox;
        m_num = num;
        m_name = name;
        m_WKT = WKT;
        m_uri = uri;
        m_GPTS = GPTS;
        m_imguri = imguri;
        m_MediaBox = MediaBox;
        m_CropBox = CropBox;
        m_ic = ic;
    }

    public String getM_MediaBox() {
        return m_MediaBox;
    }

    public void setM_MediaBox(String m_MediaBox) {
        this.m_MediaBox = m_MediaBox;
    }

    public String getM_CropBox() {
        return m_CropBox;
    }

    public void setM_CropBox(String m_CropBox) {
        this.m_CropBox = m_CropBox;
    }

    public String getM_imguri() {
        return m_imguri;
    }

    public void setM_imguri(String m_imguri) {
        this.m_imguri = m_imguri;
    }

    public Map_test(String name) {
        m_name = name;
    }
    public Map_test(String name, String uri) {
        m_name = name;
        m_uri = uri;
    }

    public String getM_name() {
        return m_name;
    }

    public void setM_name(String m_name) {
        this.m_name = m_name;
    }

    public int getM_num() {
        return m_num;
    }

    public void setM_num(int m_num) {
        this.m_num = m_num;
    }

    public String getM_GPTS() {
        return m_GPTS;
    }

    public void setM_GPTS(String m_GPTS) {
        this.m_GPTS = m_GPTS;
    }

    public String getM_BBox() {
        return m_BBox;
    }

    public void setM_BBox(String m_BBox) {
        this.m_BBox = m_BBox;
    }

    public String getM_WKT() {
        return m_WKT;
    }

    public void setM_WKT(String m_WKT) {
        this.m_WKT = m_WKT;
    }

    public String getM_uri() {
        return m_uri;
    }

    public void setM_uri(String m_uri) {
        this.m_uri = m_uri;
    }
}
