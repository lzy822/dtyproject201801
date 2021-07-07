package com.geopdfviewer.android;

import org.litepal.crud.LitePalSupport;

public class Folkway_StandardInfo  extends LitePalSupport {
    private String objectID;
    private String VillageName;
    private String TownName;
    private String DistrictName;
    private String Nation;
    private int NationHouseHold;
    private int NationNumber;
    private int TotalHouseHold;
    private int TotalNumber;
    private float Longi;
    private float Lat;
    private String Thought;
    private String Festival;
    private String Ceremony;
    private String Activity;

    public Folkway_StandardInfo(String objectID, String villageName, String townName, String districtName, String nation, int nationHouseHold, int nationNumber, int totalHouseHold, int totalNumber, float longi, float lat, String thought, String festival, String ceremony, String activity) {
        this.objectID = objectID;
        VillageName = villageName;
        TownName = townName;
        DistrictName = districtName;
        Nation = nation;
        NationHouseHold = nationHouseHold;
        NationNumber = nationNumber;
        TotalHouseHold = totalHouseHold;
        TotalNumber = totalNumber;
        Longi = longi;
        Lat = lat;
        Thought = thought;
        Festival = festival;
        Ceremony = ceremony;
        Activity = activity;
    }

    public String getThought() {
        return Thought;
    }

    public void setThought(String thought) {
        Thought = thought;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public String getFestival() {
        return Festival;
    }

    public void setFestival(String festival) {
        Festival = festival;
    }

    public String getCeremony() {
        return Ceremony;
    }

    public void setCeremony(String ceremony) {
        Ceremony = ceremony;
    }

    public String getActivity() {
        return Activity;
    }

    public void setActivity(String activity) {
        Activity = activity;
    }

    public float getLongi() {
        return Longi;
    }

    public void setLongi(float longi) {
        Longi = longi;
    }

    public float getLat() {
        return Lat;
    }

    public void setLat(float lat) {
        Lat = lat;
    }

    public String getVillageName() {
        return VillageName;
    }

    public void setVillageName(String villageName) {
        VillageName = villageName;
    }

    public String getTownName() {
        return TownName;
    }

    public void setTownName(String townName) {
        TownName = townName;
    }

    public String getDistrictName() {
        return DistrictName;
    }

    public void setDistrictName(String districtName) {
        DistrictName = districtName;
    }

    public String getNation() {
        return Nation;
    }

    public void setNation(String nation) {
        Nation = nation;
    }

    public int getNationHouseHold() {
        return NationHouseHold;
    }

    public void setNationHouseHold(int nationHouseHold) {
        NationHouseHold = nationHouseHold;
    }

    public int getNationNumber() {
        return NationNumber;
    }

    public void setNationNumber(int nationNumber) {
        NationNumber = nationNumber;
    }

    public int getTotalHouseHold() {
        return TotalHouseHold;
    }

    public void setTotalHouseHold(int totalHouseHold) {
        TotalHouseHold = totalHouseHold;
    }

    public int getTotalNumber() {
        return TotalNumber;
    }

    public void setTotalNumber(int totalNumber) {
        TotalNumber = totalNumber;
    }
}
