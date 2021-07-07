package com.geopdfviewer.android;

import org.litepal.crud.LitePalSupport;

public class Folkway_Taboo  extends LitePalSupport {
    private String objectID;
    private String name;

    public Folkway_Taboo(String objectID, String name) {
        this.objectID = objectID;
        this.name = name;
    }

    public String getObjectID() {
        return objectID;
    }

    public void setObjectID(String objectID) {
        this.objectID = objectID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
