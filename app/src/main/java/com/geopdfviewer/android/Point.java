package com.geopdfviewer.android;

/**
 * Pointf的扩展类
 * 用于更精确的存储x,y类型的坐标数据
 *
 * @author  李正洋
 *
 */
public class Point {
    private double x;
    private double y;

    public Point(Point point) {
        x = point.x;
        y = point.y;
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
}
