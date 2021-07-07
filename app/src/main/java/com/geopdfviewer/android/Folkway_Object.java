package com.geopdfviewer.android;

import org.litepal.crud.LitePalSupport;

public class Folkway_Object  extends LitePalSupport {
    private String objectID;
    private String Name;
    private String Abstract;
    private String Sacrifice;
    private String story;
    private String OtherInfo;

    public Folkway_Object(String objectID, String name, String anAbstract, String sacrifice, String story, String otherInfo) {
        this.objectID = objectID;
        Name = name;
        Abstract = anAbstract;
        Sacrifice = sacrifice;
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

    public String getSacrifice() {
        return Sacrifice;
    }

    public void setSacrifice(String sacrifice) {
        Sacrifice = sacrifice;
    }

    public String getOtherInfo() {
        return OtherInfo;
    }

    public void setOtherInfo(String otherInfo) {
        OtherInfo = otherInfo;
    }
}
