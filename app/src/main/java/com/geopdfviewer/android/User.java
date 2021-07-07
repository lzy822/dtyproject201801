package com.geopdfviewer.android;

public class User {
    private String name;
    private int age;
    private long time;
    private String portrait;
    private String className;

    public User(String name, int age, long time, String portrait, String className) {
        this.name = name;
        this.age = age;
        this.time = time;
        this.portrait = portrait;
        this.className = className;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
