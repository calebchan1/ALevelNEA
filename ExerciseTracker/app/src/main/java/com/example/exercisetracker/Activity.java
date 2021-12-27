package com.example.exercisetracker;

public class Activity {
    //class used to store data as an object to produce dynamic cards relating to
    //previous Activity history
    //not to be confused with Android Activities

    private int id;
    private String name;
    private String desc;
    private int img;

    public Activity(String name, String desc, int img, int id){
        this.name = name;
        this.desc = desc;
        this.img = img;
        this.id = id;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }
}
