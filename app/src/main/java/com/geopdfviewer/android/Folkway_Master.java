package com.geopdfviewer.android;

import org.litepal.crud.LitePalSupport;

public class Folkway_Master extends LitePalSupport {
    private String objectID;
    private String identity;
    private String name;
    private String level;
    private String duty;
    private String inherited;
    private String story;
    private String otherinfo;

    public Folkway_Master(String objectID, String identity, String name, String level, String duty, String inherited, String story, String otherinfo) {
        this.objectID = objectID;
        this.identity = identity;
        this.name = name;
        this.level = level;
        this.duty = duty;
        this.inherited = inherited;
        this.story = story;
        this.otherinfo = otherinfo;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getDuty() {
        return duty;
    }

    public void setDuty(String duty) {
        this.duty = duty;
    }

    public String getInherited() {
        return inherited;
    }

    public void setInherited(String inherited) {
        this.inherited = inherited;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public String getOtherinfo() {
        return otherinfo;
    }

    public void setOtherinfo(String otherinfo) {
        this.otherinfo = otherinfo;
    }
}
