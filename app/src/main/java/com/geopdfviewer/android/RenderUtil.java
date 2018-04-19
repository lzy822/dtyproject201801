package com.geopdfviewer.android;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class RenderUtil {

    //设置当前地图切换查询的容许误差值
    public static double getDeltaKforTrans(float page_width, double max_long, double min_long, Activity activity){
        WindowManager wm = activity.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        int deviceWidth = outMetrics.widthPixels;
        return (max_long - min_long) / page_width * deviceWidth * 1;
    }
}
