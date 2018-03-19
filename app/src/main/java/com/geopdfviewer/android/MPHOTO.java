package com.geopdfviewer.android;

import org.litepal.crud.DataSupport;

/**
 * Created by 54286 on 2018/3/19.
 */

public class MPHOTO extends DataSupport {
    private int id;
    private String pdfic;
    private String POIC;
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

    public String getPOIC() {
        return POIC;
    }

    public void setPOIC(String POIC) {
        this.POIC = POIC;
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
