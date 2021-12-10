package com.example.exercisetracker;

import static android.content.Context.MODE_PRIVATE;

import android.os.Environment;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;

public class Activity{
    private Date timeStarted;
    private Integer duration;
    private String type;
    private Integer calories;
    private Route route;
    private Float distance;

    public Activity(Date timeStarted, Integer duration, String type, Integer calories) {
        //An activity must at it's minimum have the time started, duration and exercise type name
        this.timeStarted = timeStarted;
        this.duration = duration;
        this.type = type;
        this.calories = calories;
    }

    public String getType() {
        return type;
    }

    public Date getTimeStarted() {
        return timeStarted;
    }

    public Integer getDuration() {
        return duration;
    }

    public Integer getCalories() {
        return calories;
    }

    public void setCalories(Integer calories) {
        this.calories = calories;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

}
