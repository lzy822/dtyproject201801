package com.geopdfviewer.android;

import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

public class lineUtil {
    public static float[] getLineNormalEquation(float x1, float y1, float x2, float y2){
        if (x1 == x2 & y1 != y2){
            float[] parameters = {1, 0, -1 * x1};
            return parameters;
        }else if (x1 != x2 & y1 == y2){
            float[] parameters = {0, 1, -1 * y1};
            return parameters;
        }else if (x1 == x2 & y1 == y2){
            return null;
        }else {
            float[] parameters = {y2 - y1, x1 - x2, x2 * y1 - x1 * y2};
            return parameters;
        }
    }

    public static PointF getPoint1(float[] line1, float[] line2){
        if (line1[0] == line2[0] & line1[1] == line2[1]) return null;
        else return new PointF((line2[2] * line1[1] - line1[2] * line2[1]) / (line1[0] * line2[1] - line1[1] * line2[0]), (line2[0] * line1[2] - line1[0] * line2[2]) / (line1[0] * line2[1] - line1[1] * line2[0]));
    }

    public static List<float[]> getParallelLineEquation(float[] line, float delta){
        List<float[]> parameterList = new ArrayList<>();
        float[] parameters2 = {line[0], line[1], (float)(Math.sqrt(line[0] * line[0] + line[1] * line[1])) * delta + line[2]};
        parameterList.add(parameters2);
        float[] parameters1 = {line[0], line[1], (float)(line[2] - Math.sqrt(line[0] * line[0] + line[1] * line[1])) * delta};
        parameterList.add(parameters1);
        return parameterList;
    }

    public static float[] getVerticalLineEquation(float[] line, PointF pt){
        if (line[0] == 0 & line[1] != 0){
            float[] parameters = {1, 0, -1 * pt.x};
            return parameters;
        }else if (line[0] != 0 & line[1] == 0){
            float[] parameters = {0, 1, -1 * pt.y};
            return parameters;
        }else if (line[0] == 0 & line[1] == 0){
            return null;
        }else {
            float[] parameters = {line[1] / line[0], -1, pt.y - line[1] / line[0] * pt.x};
            return parameters;
        }
    }

    public static float[] getExactVerticalLine(List<float[]> lines, float[] mline, PointF p1, PointF p2){
        for (int i = 0; i < lines.size(); i++){
            PointF pt = getPoint1(lines.get(i), mline);
            if (pt.x >= ( p1.x >= p2.x ? p2.x : p1.x) & pt.x <= ( p1.x >= p2.x ? p1.x : p2.x) & pt.y >= ( p1.y >= p2.y ? p2.y : p1.y) & pt.y <= ( p1.y >= p2.y ? p1.y : p2.y)) return lines.get(i);
            else return null;
        }
        return null;
    }

    public static String getExternalPolygon(String line, float delta){
        String[] lines = line.trim().split(" ");
        int size = lines.length;
        List<PointF> pointFS1 = new ArrayList<>();
        for (int i = 0; i < size; i++){
            String[] pt = lines[i].split(",");
            pointFS1.add(new PointF(Float.valueOf(pt[1]), Float.valueOf(pt[0])));
        }
        List<PointF> pointFS = new ArrayList<>();
        float[] mline1 = new float[2];
        for (int i = 0; i < pointFS1.size(); i++){
            float[] mline = getLineNormalEquation(pointFS1.get(i).x, pointFS1.get(i).y, pointFS1.get(i + 1).x, pointFS1.get(i + 1).y);
            List<float[]> floats = getParallelLineEquation(mline, delta);
            if (i == 0 || i == pointFS1.size() - 2){
                float[] mvline = getExactVerticalLine(getParallelLineEquation(getVerticalLineEquation(mline, pointFS1.get(i)), delta), mline, pointFS1.get(i), pointFS1.get(i + 1));
                for (int j = 0; j < floats.size(); j++){
                    pointFS.add(getPoint1(floats.get(j), mvline));
                }
            }else {
                List<float[]> floats1 = getParallelLineEquation(mline1, delta);
                pointFS.add(getPoint1(floats.get(0), floats1.get(0)));
                pointFS.add(getPoint1(floats.get(1), floats1.get(1)));
            }
            mline1 = mline;
        }
        line = "";
        for (int i = 0; i < pointFS.size(); i = i + 2){
            line = line + pointFS.get(i).x + "," + pointFS.get(i).y + " ";
        }
        for (int i = 1; i < pointFS.size(); i = i + 2){
            line = line + pointFS.get(i).x + "," + pointFS.get(i).y + " ";
        }
        return line;
    }
}
