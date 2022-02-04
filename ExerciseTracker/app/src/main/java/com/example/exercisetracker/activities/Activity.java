package com.example.exercisetracker.activities;

import java.sql.Date;


/**
 * Class used to store data about Activity
 */
public class Activity {


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

        String[] temp = desc.split(" ");

        // temp formatted as (date, time, duration, calories, steps, distance,reps)
        if (name.equals("Running") || name.equals("Treadmill") || name.equals("Walking")) {
            //null values for number of reps
            this.date = Date.valueOf(temp[0]);
            this.timeStarted = temp[1].substring(0,5);//slicing string to just fit hr:min
            this.duration = Integer.parseInt(temp[2]);
            this.calories = Integer.parseInt(temp[3]);
            this.steps = Integer.parseInt(temp[4]);
            this.distance = Integer.parseInt(temp[5]);
        } else if (name.equals("Push Up") || name.equals("Squats")) {
            //null values for steps and distance
            this.date = Date.valueOf(temp[0]);
            this.timeStarted = temp[1].substring(0,5);//slicing string to just fit hr:min
            this.duration = Integer.parseInt(temp[2]);
            this.calories = Integer.parseInt(temp[3]);
            this.reps = Integer.parseInt(temp[6]);
        }

    }

    public String getTimeStarted() {
        return timeStarted;
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

    public int getImg() {
        return img;
    }
}
