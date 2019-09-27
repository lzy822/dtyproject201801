package com.geopdfviewer.android;

import org.litepal.crud.LitePalSupport;

/**
 * 照片要素类
 * 用于记录与兴趣点绑定的照片数据
 * 该内容有对应的数据表，照片要素表与兴趣点表通过poic字段相连接
 *
 * @author  李正洋
 *
 * @since   1.1
 *
 * Created by 54286 on 2018/3/19.
 */
public class MPHOTO extends LitePalSupport {
    private int id;
    private String pdfic;
    private String poic;
    private String path;
    private String time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPdfic() {
        return pdfic;
    }

    public void setPdfic(String pdfic) {
        this.pdfic = pdfic;
    }

    public String getPoic() {
        return poic;
    }

    public void setPoic(String poic) {
        this.poic = poic;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
