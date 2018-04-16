package com.geopdfviewer.android;

import org.litepal.crud.DataSupport;

public class Lines_WhiteBlank extends DataSupport {
    private String m_ic;
    private String m_lines;
    private int m_color;

    public String getM_ic() {
        return m_ic;
    }

    public void setM_ic(String m_ic) {
        this.m_ic = m_ic;
    }

    public String getM_lines() {
        return m_lines;
    }

    public void setM_lines(String m_lines) {
        this.m_lines = m_lines;
    }

    public int getM_color() {
        return m_color;
    }

    public void setM_color(int m_color) {
        this.m_color = m_color;
    }
}
