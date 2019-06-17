package com.geopdfviewer.android;

import android.app.Activity;

public class TransMapCommand implements BtCommand {
    private volatile static TransMapCommand uniqueInstance;
    private boolean isON;
    private JZActivity jzActivity;
    private int map_num;

    private TransMapCommand(JZActivity activity) {
        jzActivity = activity;
        //map_num = i;
    }

    public int getMap_num() {
        return map_num;
    }

    public void setMap_num(int map_num) {
        this.map_num = map_num;
    }

    @Override
    public void on() {
        isON = true;
    }

    @Override
    public void process() {
        if (isON){
            jzActivity.updateMapInfo(map_num);
            jzActivity.manageInfo(jzActivity.mGpts);
            jzActivity.displayFromFile(jzActivity.mUri);
        }else {

        }
    }

    @Override
    public void off() {
        isON = false;
    }

    public static TransMapCommand getInstance(JZActivity activity){
        if (uniqueInstance == null){
            synchronized (TransMapCommand.class){
                if (uniqueInstance == null){
                    uniqueInstance = new TransMapCommand(activity);
                }
            }
        }
        return uniqueInstance;
    }
}
