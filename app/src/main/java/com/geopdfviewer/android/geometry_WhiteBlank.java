package com.geopdfviewer.android;

/**
 * 白板要素类
 * 用于缓存白板的空间信息，序号，颜色和空间索引
 *
 * @author  李正洋
 */
public class geometry_WhiteBlank {
    private String ic;
    private String m_lines;
    private int m_color;
    private float maxlat;
    private float maxlng;
    private float minlat;
    private float minlng;

    public geometry_WhiteBlank(String ic, String m_lines, int m_color, float maxlat, float maxlng, float minlat, float minlng) {
        this.ic = ic;
        this.m_lines = m_lines;
        this.m_color = m_color;
        this.maxlat = maxlat;
        this.maxlng = maxlng;
        this.minlat = minlat;
        this.minlng = minlng;
    }

    public float getMaxlat() {
        return maxlat;
    }

    public void setMaxlat(float maxlat) {
        this.maxlat = maxlat;
    }

    public float getMaxlng() {
        return maxlng;
    }

    public void setMaxlng(float maxlng) {
        this.maxlng = maxlng;
    }

    public float getMinlat() {
        return minlat;
    }

    public void setMinlat(float minlat) {
        this.minlat = minlat;
    }

    public float getMinlng() {
        return minlng;
    }

    public void setMinlng(float minlng) {
        this.minlng = minlng;
    }

    public String getIc() {
        return ic;
    }

    public void setIc(String ic) {
        this.ic = ic;
    }

    public geometry_WhiteBlank(String m_lines, int m_color) {
        this.m_lines = m_lines;
        this.m_color = m_color;
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
