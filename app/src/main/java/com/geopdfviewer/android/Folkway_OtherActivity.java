package com.geopdfviewer.android;

import org.litepal.crud.LitePalSupport;

public class Folkway_OtherActivity  extends LitePalSupport {
    private String objectID;
    private String ActivityName;
    private String Abstract;
    private String Time;
    private String Location;
    private String Object;
    private String master;
    private String Participants;
    private String ActivityContent;
    private String taboo;
    private String story;

    public Folkway_OtherActivity(String objectID, String activityName, String anAbstract, String time, String location, String object, String master, String participants, String activityContent, String taboo, String story) {
        this.objectID = objectID;
        ActivityName = activityName;
        Abstract = anAbstract;
        Time = time;
        Location = location;
        Object = object;
        this.master = master;
        Participants = participants;
        ActivityContent = activityContent;
        this.taboo = taboo;
        this.story = story;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getTaboo() {
        return taboo;
    }

    public void setTaboo(String taboo) {
        this.taboo = taboo;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public String getActivityName() {
        return ActivityName;
    }

    public void setActivityName(String activityName) {
        ActivityName = activityName;
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

    public String getObject() {
        return Object;
    }

    public void setObject(String object) {
        Object = object;
    }

    public String getParticipants() {
        return Participants;
    }

    public void setParticipants(String participants) {
        Participants = participants;
    }

    public String getActivityContent() {
        return ActivityContent;
    }

    public void setActivityContent(String activityContent) {
        ActivityContent = activityContent;
    }
}
