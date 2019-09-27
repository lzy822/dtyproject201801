package com.geopdfviewer.android;

import android.app.Activity;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * 渲染工具类
 * 用于存储渲染方法
 */
public class RenderUtil {
    //设置当前地图切换查询的容许误差值
    public static double getDeltaKforTrans(float page_width, double max_long, double min_long, Activity activity, TuzhiEnum type){
        WindowManager wm = activity.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        int deviceWidth = outMetrics.widthPixels;
        if (type == TuzhiEnum.ZOOM_IN) return (max_long - min_long) / page_width * deviceWidth * 1;
        else return (max_long - min_long) * 24;
    }

    //获取pdf阅读器和pdf页面的拉伸比例
    public static float[] getK(float width, float height, float viewer_width, float viewer_height){
        float k_w = 0;
        float k_h = 0;
        if (viewer_height > height){
            k_h = (viewer_height - height) / 2;
        } else k_h = 0;
        if (viewer_width > width){
            k_w = (viewer_width - width) / 2;
        } else k_w = 0;
        return new float[]{k_w, k_h};
        //locError(Float.toString(k_w) + "see" + Float.toString(k_h));
    }

    //经纬度到屏幕坐标位置转化
    public static PointF getPixLocFromGeoL(PointF pt, float pageWidth, float pageHeight, double deltaLong, double deltaLat, double min_long, double min_lat){
        double y_ratio = ((pt.x - min_lat) / deltaLat);
        double x_ratio = ((pt.y - min_long) / deltaLong);
        pt.x = (float) ( x_ratio * pageWidth);
        pt.y = (float) ( (1 - y_ratio) * pageHeight);
        return pt;
    }
}
