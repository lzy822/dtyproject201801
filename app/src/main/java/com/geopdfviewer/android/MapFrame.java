package com.geopdfviewer.android;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 非叶地图类，类似于子菜单类
 * 用于存储有下一级地图的地图数据
 *
 * @author  李正洋
 *
 * @since   1.6
 */
public class MapFrame extends MapComponent {
    private static final String TAG = "MapFrame";
    List<MapComponent> mapComponentList = new ArrayList<>();
    private String name;
    private String position;

    public MapFrame(String name, String position) {
        this.name = name;
        this.position = position;
    }

    @Override
    public void add(MapComponent component) {
        mapComponentList.add(component);
    }

    @Override
    public void remove(MapComponent component) {
        mapComponentList.remove(component);
    }

    @Override
    public MapComponent getChild(int i) {
        return mapComponentList.get(i);
    }

    @Override
    public Iterator createIterator() {
        return new CompositeIterator(mapComponentList.iterator());
    }

    @Override
    public void print() {
        Log.w(TAG, "MapFrame: name \n");
        Iterator iterator = mapComponentList.iterator();
        while (iterator.hasNext()){
            MapComponent mapComponent = (MapComponent) iterator.next();
            mapComponent.print();
        }
    }

    @Override
    public String getPosition() {
        return position;
    }
}
