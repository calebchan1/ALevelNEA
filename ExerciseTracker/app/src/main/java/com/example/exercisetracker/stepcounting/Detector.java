package com.example.exercisetracker.stepcounting;

/**
 * Detector class used to detect number of steps by considering how long
 * the user acceleration exceeds a certain amount
 */

public class Detector {

    private float Threshold;
    private Integer stepduration;

    public Detector(float Threshold, Integer stepduration) {
        this.Threshold = Threshold;
        this.stepduration = stepduration;
    }

    public Integer detect(Float[] filtered_data) {
        int i = 0;
        int stepCount = 0;
        while (i < filtered_data.length - 1) {
            Float data = filtered_data[i];
            if (data >= Threshold) {
                int duration = 0;
                while ((filtered_data[i] > Threshold) && (i != filtered_data.length - 1)) {
                    i++;
                    duration = duration + 1;
                }
                if (duration > stepduration) {
                    stepCount++;
                }
            } else {
                i++;
            }
        }
        return stepCount;
    }

    public float getThreshold() {
        return Threshold;
    }

    public void setThreshold(float threshold) {
        Threshold = threshold;
    }
}
