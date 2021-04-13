package com.geopdfviewer.android;

import android.support.annotation.NonNull;

import org.litepal.crud.LitePalSupport;

public class ElectronicAtlasMap extends LitePalSupport implements Comparable<ElectronicAtlasMap>{
    private String parentNode;
    private String name;
    private int mapType;
    private String path;
    private String imgPath;
    private PointF[] MapGeoInfo = new PointF[4];
    private String MapGeoStr;
    private int XZQNum;
    private int PageNum;

    public ElectronicAtlasMap() {
    }

    /*public ElectronicAtlasMap(String parentNode, String name, int mapType, String path, String imgPath, String mapGeoStr, int XZQNum) {
            this.parentNode = parentNode;
            this.name = name;
            this.mapType = mapType;
            this.path = path;
            this.imgPath = imgPath;
            MapGeoStr = mapGeoStr;
            this.XZQNum = XZQNum;
        }

        public ElectronicAtlasMap(String parentNode, String name, int mapType, String path, String mapGeoStr) {
            this.parentNode = parentNode;
            this.name = name;
            this.mapType = mapType;
            this.path = path;
            MapGeoStr = mapGeoStr;
        }*/
    public ElectronicAtlasMap(String parentNode, String name, int mapType, String path, String imgPath, String mapGeoStr, int XZQNum, int PageNum) {
        this.parentNode = parentNode;
        this.name = name;
        this.mapType = mapType;
        this.path = path;
        this.imgPath = imgPath;
        MapGeoStr = mapGeoStr;
        this.XZQNum = XZQNum;
        this.PageNum = PageNum;
    }

    public ElectronicAtlasMap(String parentNode, String name, int mapType, String path, String mapGeoStr, int PageNum) {
        this.parentNode = parentNode;
        this.name = name;
        this.mapType = mapType;
        this.path = path;
        MapGeoStr = mapGeoStr;
        this.PageNum = PageNum;
    }

    @Override
    public int compareTo(@NonNull ElectronicAtlasMap electronicAtlasMap) {
        return this.PageNum - electronicAtlasMap.getPageNum();
    }

    public int getPageNum() {
        return PageNum;
    }

    public void setPageNum(int pageNum) {
        PageNum = pageNum;
    }

    public int getXZQNum() {
        return XZQNum;
    }

    public void setXZQNum(int XZQNum) {
        this.XZQNum = XZQNum;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public String getMapGeoStr() {
        return MapGeoStr;
    }

    public void setMapGeoStr(String mapGeoStr) {
        MapGeoStr = mapGeoStr;
    }
//private List<Point2D> FramePts;

    /*public Map(String parentNode, String name, int mapType, String path) {
        this.parentNode = parentNode;
        this.name = name;
        this.mapType = mapType;
        this.path = path;
    }*/

    /*public List<Point2D> getFramePts() {
        return FramePts;
    }

    public void setFramePts(List<Point2D> framePts) {
        FramePts = framePts;
    }*/

    /*public Map(String parentNode, String name, int mapType, String path, List<Point2D> framePts) {
        this.parentNode = parentNode;
        this.name = name;
        this.mapType = mapType;
        this.path = path;
        FramePts = framePts;
    }*/

        /*public ElectronicAtlasMap(String parentNode, String name, int mapType, String path, PointF[] MapGeoInfo){
            this.parentNode = parentNode;
            this.name = name;
            this.mapType = mapType;
            this.path = path;
            this.MapGeoInfo = MapGeoInfo;
        }*/

    /*public ElectronicAtlasMap(String parentNode, String name, int mapType, String path, String MapGeoStr){
        this.parentNode = parentNode;
        this.name = name;
        this.mapType = mapType;
        this.path = path;
        this.MapGeoStr = MapGeoStr;
    }*/

        public PointF[] getMapGeoInfo() {
            return MapGeoInfo;
        }

        public void ShowMapRect(){
            float max_lat = 0;
            float max_long = 0;
            float min_lat = Float.MAX_VALUE;
            float min_long = Float.MAX_VALUE;
            for (int i = 0; i < MapGeoInfo.length; i++) {
                float lat = MapGeoInfo[i].getLat();
                float longi = MapGeoInfo[i].getLong();
                if (lat > max_lat)
                    max_lat = lat;
                if (longi > max_long)
                    max_long = longi;
                if (lat < min_lat)
                    min_lat = lat;
                if (longi < min_long)
                    min_long = longi;
            }
            System.out.println("最大经度： " + max_long + "\n" + "最大纬度： " + max_lat + "\n" + "最小经度： " + min_long + "\n" + "最小纬度： " + min_lat);
        }

        public void setMapGeoInfo(PointF[] mapGeoInfo) {
            MapGeoInfo = mapGeoInfo;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getParentNode() {
            return parentNode;
        }

        public void setParentNode(String parentNode) {
            this.parentNode = parentNode;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getMapType() {
            return mapType;
        }

        public void setMapType(int mapType) {
            this.mapType = mapType;
        }

}
