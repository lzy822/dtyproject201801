package com.geopdfviewer.android;

import android.graphics.Bitmap;

public class bt {
    private Bitmap m_bm;
    private String m_path;

    public bt(Bitmap m_bm, String m_path) {
        this.m_bm = m_bm;
        this.m_path = m_path;
    }

    public Bitmap getM_bm() {
        return m_bm;
    }

    public void setM_bm(Bitmap m_bm) {
        this.m_bm = m_bm;
    }

    public String getM_path() {
        return m_path;
    }

    public void setM_path(String m_path) {
        this.m_path = m_path;
    }
}
