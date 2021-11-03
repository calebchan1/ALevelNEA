package com.example.exercisetracker;

public class Detector {

    private float Threshold;
    private Integer stepCount;
    private Boolean isStep;

    public Detector(float Threshold) {
        this.Threshold = Threshold;
        stepCount = 0;
    }

    public void detect(Float[] filtered_data){
        int counter = 1;
        while (counter < filtered_data.length){
            if ((filtered_data[counter-1]==0)&(filtered_data[counter]>0)){
                isStep=true;
                while (isStep){
                    if (filtered_data[counter]<Threshold){
                        isStep=false;
                        stepCount++;
                    }
                    counter++;
                }
            }
            else{
                counter++;
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
