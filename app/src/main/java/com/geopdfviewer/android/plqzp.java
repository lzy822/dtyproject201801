package com.geopdfviewer.android;

import org.litepal.crud.LitePalSupport;

/**
 * 盘龙区照片类
 * 用于记录盘龙区照片数据，与kmltext表通过xh字段相连接
 * 该内容有对应的数据表
 *
 * @author  李正洋
 *
 * @since   1.4
 */
public class plqzp extends LitePalSupport {
    private String xh;
    private String zp1;
    private String zp2;
    private String zp3;
    private String zp4;
    private String zp5;

    public String getZp4() {
        return zp4;
    }

    public void setZp4(String zp4) {
        this.zp4 = zp4;
    }

    public String getZp5() {
        return zp5;
    }

    public void setZp5(String zp5) {
        this.zp5 = zp5;
    }

    public String getZp3() {
        return zp3;
    }

    public void setZp3(String zp3) {
        this.zp3 = zp3;
    }

    public String getXh() {
        return xh;
    }

    public void setXh(String xh) {
        this.xh = xh;
    }

    public String getZp1() {
        return zp1;
    }

    public void setZp1(String zp1) {
        this.zp1 = zp1;
    }

    public String getZp2() {
        return zp2;
    }

    public void setZp2(String zp2) {
        this.zp2 = zp2;
    }
}
