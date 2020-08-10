package com.geopdfviewer.android;

import org.litepal.crud.LitePalSupport;

/**
 * 房屋信息表
 *
 * 李正洋
 *
 * 2020/7/20
 */
public class FWXXSheet extends LitePalSupport {
    private String ParcelID;//宗地id
    private String Time;//建房时间
    private String HouseStructure;//房屋结构
    private String FloorNumber;//层数
    private String BuildingArea;//建筑面积

    public String getParcelID() {
        return ParcelID;
    }

    public void setParcelID(String parcelID) {
        ParcelID = parcelID;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getHouseStructure() {
        return HouseStructure;
    }

    public void setHouseStructure(String houseStructure) {
        HouseStructure = houseStructure;
    }

    public String getFloorNumber() {
        return FloorNumber;
    }

    public void setFloorNumber(String floorNumber) {
        FloorNumber = floorNumber;
    }

    public String getBuildingArea() {
        return BuildingArea;
    }

    public void setBuildingArea(String buildingArea) {
        BuildingArea = buildingArea;
    }
}
