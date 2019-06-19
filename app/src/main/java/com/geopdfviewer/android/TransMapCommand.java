package com.geopdfviewer.android;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class TransMapCommand implements BtCommand{
    private static final String TAG = "TransMapCommand";
    private volatile static TransMapCommand uniqueInstance;
    private boolean isON;
    private JZActivity jzActivity;
    private int map_num;

    public boolean isON() {
        return isON;
    }

    private TransMapCommand(JZActivity activity) {
        jzActivity = activity;
        isON = false;
        //map_num = i;
    }

    public int getMap_num() {
        return map_num;
    }

    public void setMap_num(int map_num) {
        this.map_num = map_num;
        jzActivity.num_map1 = jzActivity.num_map;
        jzActivity.num_map = map_num;
    }

    @Override
    public void on() {
        isON = true;
    }

    @Override
    public void process() {
        if (isON){
            jzActivity.geometry_whiteBlanks.clear();
            jzActivity.num_whiteBlankPt = 0;
            jzActivity.isWhiteBlank = false;
            jzActivity.whiteBlankPt = "";
            jzActivity.getInfo(map_num);
            jzActivity.updateMapInfo(map_num);
            Map map = jzActivity.currentMap;
            jzActivity.toolbar.setTitle(map.getName());
            jzActivity.getNormalBitmap();
            //manageInfo();
            jzActivity.pdfView.recycle();
            Log.w(TAG, "process: " + map_num);
            jzActivity.displayFromFile(map.getUri());
            off();
            jzActivity.autoTrans_imgbt.setBackgroundResource(R.drawable.ic_close_black_24dp);
            jzActivity.getWhiteBlankData();
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
