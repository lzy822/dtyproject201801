package com.geopdfviewer.android;

import android.graphics.PointF;

import java.util.LinkedList;
import java.util.List;

public class Douglas {
    // 存储用于处理的点的List列表
    private List<PointF> list = null;

    // 给定的阈值(限差)threshold
    private static final double threshold = 0.00001;

    public Douglas(List<PointF> list) {
        this.list = list;
    }

    /**
     * 道格拉斯算法，处理List<PointF>序列
     * 先将首末两点加入points序列中，然后在list序列找出距离首末点连线距离的最大距离值dmax并与阈值进行比较，
     * 若大于阈值则将这个点加入points序列，重新遍历points序列。否则将两点间的所有点(list)移除。
     *
     * @return 返回经过道格拉斯算法后得到的点的序列
     */
    public List<PointF> douglasPeucker() {
        List<PointF> points = new LinkedList<>();
        // 将首末两点加入到序列中
        points.add(this.list.get(0));
        points.add(list.get(list.size() - 1));
        for (int i = 0; i < points.size() - 1; i++) {
            int start = this.list.indexOf(points.get(i));
            int end = this.list.indexOf(points.get(i + 1));
            // 比较是否是相邻点
            if (end - start == 1) {
                continue;
            }
            String value = getMaxDistance(start, end);
            double maxDist = Double.parseDouble(value.split(",")[0]);
            // 大于限差将点加入points序列
            if (maxDist >= threshold) {
                int position = Integer.parseInt(value.split(",")[1]);
                points.add(i + 1, list.get(position));
                // 将循环变量i初始化,即重新遍历points序列
                i = -1;
            } else {
                /**
                 * 将两点间的点全部删除
                 */
                int removePosition = start + 1;
                for (int j = 0; j < end - start - 1; j++) {
                    this.list.remove(removePosition);
                }
            }
        }
        return points;
    }

    /**
     * 根据给定的始末点，计算出距离始末点直线的最远距离和点在list列表中的位置
     * @param start 遍历list起始点
     * @param end 遍历list终点
     * @return maxDiatance + "," + position 返回最大距离+","+在list中位置
     */
    private String getMaxDistance(int start, int end) {
        double maxDiatance = -1;
        int position = -1;
        double distance = getDistance(this.list.get(start), this.list.get(end));
        for (int i = start; i < end; i++) {
            double firstSide = getDistance(this.list.get(start),
                    this.list.get(i));
            if (firstSide < threshold) {
                continue;
            }
            double lastSide = getDistance(this.list.get(end), this.list.get(i));
            if (lastSide < threshold) {
                continue;
            }
            // 使用海伦公式求距离
            double p = (distance + firstSide + lastSide) / 2;
            double dis = Math.sqrt(p * (p - distance) * (p - firstSide)
                    * (p - lastSide))
                    / distance;
            if (dis > maxDiatance) {
                maxDiatance = dis;
                position = i;
            }
        }
        return maxDiatance + "," + position;
    }

    // 两点间距离公式
    private double getDistance(PointF first, PointF last) {
        return Math.sqrt(Math.pow(first.x - last.y, 2)
                + Math.pow(first.y - last.x, 2));
    }
}
