package com.geopdfviewer.android;

import org.litepal.crud.LitePalSupport;

public class Folkway_Festival  extends LitePalSupport {
    private String objectID;
    private String Name;
    private String Abstract;
    private String Time;
    private String Location;
    private String procedure;
    private String taboo;
    private String story;
    private String OtherInfo;

    public Folkway_Festival(String objectID, String name, String anAbstract, String time, String location, String procedure, String taboo, String story, String otherInfo) {
        this.objectID = objectID;
        Name = name;
        Abstract = anAbstract;
        Time = time;
        Location = location;
        this.procedure = procedure;
        this.taboo = taboo;
        this.story = story;
        OtherInfo = otherInfo;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getAbstract() {
        return Abstract;
    }

    public void setAbstract(String anAbstract) {
        Abstract = anAbstract;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }

    public String getTaboo() {
        return taboo;
    }

    public void setTaboo(String taboo) {
        this.taboo = taboo;
    }

    public String getOtherInfo() {
        return OtherInfo;
    }

    public void setOtherInfo(String otherInfo) {
        OtherInfo = otherInfo;
    }
}
