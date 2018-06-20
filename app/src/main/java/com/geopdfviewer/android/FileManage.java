package com.geopdfviewer.android;

import android.os.Environment;

import java.io.File;
import java.util.List;

public class FileManage {
    private String RootPath;
    private String LastPath;
    private String FileType;
    private String[] FileSubset;
    private File file;
    public static int BUBBLESORT = -1;

    public void setFileType(String fileType) {
        FileType = fileType;
    }

    public FileManage(String fileType) {
        RootPath = Environment.getExternalStorageDirectory().toString();
        LastPath = Environment.getExternalStorageDirectory().toString();
        FileType = fileType;
        file = new File(RootPath);
        if (FileType.equals("")) FileSubset = file.list();
        else {
            String[] strings = file.list();
            int num = 0;
            for (int i = 0; i < strings.length; i++){
                if (strings[i].contains(FileType)) {
                    num++;
                }else {
                    File f = new File(RootPath + "/" + strings[i]);
                    if (f.isDirectory() & !strings[i].substring(0, 1).equals(".")) num++;
                }
            }
            FileSubset = new String[num];
            for (int i = 0, j = 0; i < strings.length; i++){
                if (strings[i].contains(FileType)){
                    FileSubset[j] = strings[i];
                    j++;
                }else {
                    File f = new File(RootPath + "/" + strings[i]);
                    if (f.isDirectory() & !strings[i].substring(0, 1).equals(".")) {
                        FileSubset[j] = strings[i];
                        j++;
                    }
                }
            }
        }
    }

    public FileManage(String rootPath, String lastPath, String fileType) {
        RootPath = rootPath;
        LastPath = lastPath;
        FileType = fileType;
        file = new File(RootPath);
        if (FileType.equals("")) FileSubset = file.list();
        else {
            String[] strings = file.list();
            int num = 0;
            for (int i = 0; i < strings.length; i++){
                if (strings[i].contains(FileType)) {
                    num++;
                }else {
                    File f = new File(RootPath + "/" + strings[i]);
                    if (f.isDirectory() & !strings[i].substring(0, 1).equals(".")) num++;
                }
            }
            FileSubset = new String[num];
            for (int i = 0, j = 0; i < strings.length; i++){
                if (strings[i].contains(FileType)){
                    FileSubset[j] = strings[i];
                    j++;
                }else {
                    File f = new File(RootPath + "/" + strings[i]);
                    if (f.isDirectory() & !strings[i].substring(0, 1).equals(".")) {
                        FileSubset[j] = strings[i];
                        j++;
                    }
                }
            }
        }
    }

    public FileManage(String rootPath, String lastPath, int Type) {
        RootPath = rootPath;
        LastPath = lastPath;
    }

    public FileManage SelectLast(){
        if (!RootPath.equals(Environment.getExternalStorageDirectory().toString())){
            RootPath = LastPath;
            LastPath = LastPath.substring(0, LastPath.lastIndexOf("/"));
            file = new File(RootPath);
            FileSubset = file.list();
            if (FileType.equals("")) FileSubset = file.list();
            else {
                String[] strings = file.list();
                int num = 0;
                for (int i = 0; i < strings.length; i++){
                    if (strings[i].contains(FileType)) {
                        num++;
                    }else {
                        File f = new File(RootPath + "/" + strings[i]);
                        if (f.isDirectory() & !strings[i].substring(0, 1).equals(".")) num++;
                    }
                }
                FileSubset = new String[num];
                for (int i = 0, j = 0; i < strings.length; i++){
                    if (strings[i].contains(FileType)){
                        FileSubset[j] = strings[i];
                        j++;
                    }else {
                        File f = new File(RootPath + "/" + strings[i]);
                        if (f.isDirectory() & !strings[i].substring(0, 1).equals(".")) {
                            FileSubset[j] = strings[i];
                            j++;
                        }
                    }
                }
            }
        }
        return this;
    }

    public void SelectNext(FileManage fm){
        RootPath = fm.RootPath;
        LastPath = fm.LastPath;
        file = new File(RootPath);
        FileSubset = file.list();
    }

    public String getRootPath() {
        return RootPath;
    }

    public String[] getFileSubset() {
        return FileSubset;
    }

    public String[] getFileSubset(int type) {
        if (type == BUBBLESORT) return bubbleSort(FileSubset);
        else return FileSubset;
    }

    private String[] bubbleSort(String[] arr) {
        int len = arr.length;
        for (int i = 0; i < len - 1; i++) {
            for (int j = 0; j < len - 1 - i; j++) {
                if (arr[j].toUpperCase().charAt(0) > arr[j + 1].toUpperCase().charAt(0)) {        // 相邻元素两两对比
                    String temp = arr[j+1];        // 元素交换
                    arr[j+1] = arr[j];
                    arr[j] = temp;
                }
            }
        }
        return arr;
    }
}
