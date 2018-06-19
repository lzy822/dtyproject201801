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
                    if (f.isDirectory()) num++;
                }
            }
            FileSubset = new String[num];
            for (int i = 0, j = 0; i < strings.length; i++){
                if (strings[i].contains(FileType)){
                    FileSubset[j] = strings[i];
                    j++;
                }else {
                    File f = new File(RootPath + "/" + strings[i]);
                    if (f.isDirectory()) {
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
                    if (f.isDirectory()) num++;
                }
            }
            FileSubset = new String[num];
            for (int i = 0, j = 0; i < strings.length; i++){
                if (strings[i].contains(FileType)){
                    FileSubset[j] = strings[i];
                    j++;
                }else {
                    File f = new File(RootPath + "/" + strings[i]);
                    if (f.isDirectory()) {
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
                        if (f.isDirectory()) num++;
                    }
                }
                FileSubset = new String[num];
                for (int i = 0, j = 0; i < strings.length; i++){
                    if (strings[i].contains(FileType)){
                        FileSubset[j] = strings[i];
                        j++;
                    }else {
                        File f = new File(RootPath + "/" + strings[i]);
                        if (f.isDirectory()) {
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
}
