package com.example.exercisetracker.activities;

import java.sql.Date;

public class Activity {
    //class used to store data as an object to produce dynamic cards relating to
    //previous Activity history
    //not to be confused with Android Activities

    private int img;
    private String name;

    private int id;
    private String desc;
    private String timeStarted;
    private Date date;
    private int duration;
    private int calories;
    private int steps;
    private int distance;
    private int reps;

    public Activity(String name, String desc, int img, int id) {
        this.name = name;
        this.desc = desc;
        this.img = img;
        this.id = id;

    }

    public String getTimeStarted() {
        return timeStarted;
    }

    public void setTimeStarted(String timeStarted) {
        this.timeStarted = timeStarted;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
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
