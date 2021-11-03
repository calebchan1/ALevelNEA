package com.example.exercisetracker;

public class Detector {

    private float Threshold;
    private int stepCount;

    public Detector(float Threshold) {
        this.Threshold = Threshold;
    }

    public void detect(Float[] filtered_data){
        for (int i=1;i<filtered_data.length;i++){
            if ((filtered_data[i]>=Threshold)&(filtered_data[i-1]<Threshold)){
                stepCount++;
            }
        }
    }

    public int getStepCount() {
        return stepCount;
    }

    public float getThreshold() {
        return Threshold;
    }

    public void setThreshold(float threshold) {
        Threshold = threshold;
    }
}
