package com.geopdfviewer.android;

import org.litepal.crud.LitePalSupport;

/**
 * 盘龙区音频类
 * 用于记录盘龙区音频数据，与kmltext表通过xh字段相连接
 * 该内容有对应的数据表
 *
 * @author  李正洋
 *
 * @since   1.4
 */
public class plqyp extends LitePalSupport {
    private String xh;
    private String yp;

    public String getXh() {
        return xh;
    }

    public void setXh(String xh) {
        this.xh = xh;
    }

    public String getYp() {
        return yp;
    }

    public void setYp(String yp) {
        this.yp = yp;
    }
}
