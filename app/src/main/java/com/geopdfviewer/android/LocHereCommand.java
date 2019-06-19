package com.geopdfviewer.android;

import android.support.v7.app.AppCompatActivity;

public class LocHereCommand implements BtCommand {
    private volatile static LocHereCommand uniqueInstance;
    private boolean isON;
    private JZActivity jzActivity;

    private LocHereCommand(JZActivity activity) {
        jzActivity = activity;
    }

    @Override
    public void on() {
        isON = true;
    }

    @Override
    public void process() {
        if (isON){

        }else {

        }
    }

    @Override
    public void off() {
        isON = false;
    }

    public static LocHereCommand getInstance(JZActivity activity){
        if (uniqueInstance == null){
            synchronized (TransMapCommand.class){
                if (uniqueInstance == null){
                    uniqueInstance = new LocHereCommand(activity);
                }
            }
        }
        return uniqueInstance;
    }
}
