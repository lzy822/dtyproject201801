package com.geopdfviewer.android;

import java.util.Iterator;

public abstract class MapComponent {


    public void add(MapComponent component){
        throw new UnsupportedOperationException();
    }

    public void remove(MapComponent component){
        throw new UnsupportedOperationException();
    }

    public MapComponent getChild(int i){
        throw new UnsupportedOperationException();
    }

    public abstract Iterator createIterator();

    public void print(){
        throw new UnsupportedOperationException();
    }

    public String getPosition(){
        throw new UnsupportedOperationException();
    }

    public String getUri(){
        throw new UnsupportedOperationException();
    }

    public String getName(){
        throw new UnsupportedOperationException();
    }

    public String getImgUri(){
        throw new UnsupportedOperationException();
    }

    public int getMapType(){
        throw new UnsupportedOperationException();
    }

    public String getMapTypeForString(int maptype){
        throw new UnsupportedOperationException();
    }
}
