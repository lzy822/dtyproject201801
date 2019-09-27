package com.geopdfviewer.android;

import android.support.v7.app.AppCompatActivity;

/**
 * 定位当前位置命令类
 * 用于以命令类的形式存储定位到当前位置的命令
 *
 * @author  李正洋
 *
 * @since   1.6
 */
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
