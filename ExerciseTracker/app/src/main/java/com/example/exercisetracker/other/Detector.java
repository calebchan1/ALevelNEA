package com.example.exercisetracker.other;

import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Detector class used to detect number of steps by considering how long
 * the user acceleration exceeds a certain amount
 *
 */

public class Detector {

    private float Threshold;
    private Integer stepCount;
    private Integer stepduration;

    public Detector(float Threshold, Integer stepduration) {
        this.Threshold = Threshold;
        this.stepduration = stepduration;
        stepCount = 0;
    }

    public void detect(Float[] filtered_data) {
        int i = 0;
        while (i<filtered_data.length-1){
            Float data = filtered_data[i];
            if (data>=Threshold){
                int duration = 0;
                while ((filtered_data[i]>Threshold)&&(i!=filtered_data.length-1)){
                    i++;
                    duration = duration+1;
                }
                if (duration>stepduration){
                    stepCount++;
                }
            }
            else {
                i++;
            }
        }
    }


    public Integer getStepCount() {
        return stepCount;
    }

    public float getThreshold() {
        return Threshold;
    }

    public void setThreshold(float threshold) {
        Threshold = threshold;
    }
}
