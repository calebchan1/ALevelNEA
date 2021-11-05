package com.example.exercisetracker;

import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Detector {

    private float Threshold;
    private Integer stepCount;
    private Boolean isStep;

    public Detector(float Threshold) {
        this.Threshold = Threshold;
        stepCount = 0;
    }

    public void detect(Float[] filtered_data) {
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
