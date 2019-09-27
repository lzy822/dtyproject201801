package com.geopdfviewer.android;

import java.util.Iterator;

/**
 * 地图组件抽象类
 * 用于被继承，MapFrame和MapItem的基类
 *
 * @author  李正洋
 *
 * @since   1.6
 *
 */
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
