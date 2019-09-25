package com.geopdfviewer.android;

import org.litepal.crud.LitePalSupport;
/*
 *  用来存储武警DEMO中的图例样式
 *  访问根目录下的./原图 文件夹
 *
 *  @author 李正洋
 *
 */
public class IconDataset extends LitePalSupport{
    private String path;
    private String name;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
