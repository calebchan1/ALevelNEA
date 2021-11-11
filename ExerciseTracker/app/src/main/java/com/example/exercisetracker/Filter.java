package com.example.exercisetracker;

import java.util.ArrayList;

public class Filter {
    private Float[] filtered_data;
    private Float minThreshold;
    private Float maxThreshold;

    public Filter(float minThreshold, float maxThreshold) {
        //setting max and min threshold on create
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;
    }

    public void filter(ArrayList<Float> data){
        //filter the data to remove bumpiness and outliers using thresholds given
        filtered_data = new Float[data.size()];
        for (int i=0 ;i<data.size();i++){
            if (data.get(i)==0.0){
                filtered_data[i] = 0f;
            }
            else if (data.get(i)>maxThreshold){
                filtered_data[i] = maxThreshold;
            }
            else if (data.get(i)<minThreshold){
                filtered_data[i] = minThreshold;
            }
            else{
                filtered_data[i] = data.get(i);
            }
        }
        setFiltered_data(filtered_data);
    }

    public float getMaxThreshold() {
        return maxThreshold;
    }

    public void setMaxThreshold(float maxThreshold) {
        this.maxThreshold = maxThreshold;
    }

    public Float[] getFiltered_data() {
        return filtered_data;
    }

    public void setFiltered_data(Float[] filtered_data) {
        this.filtered_data = filtered_data;
    }

    public float getMinThreshold() {
        return minThreshold;
    }

    public void setMinThreshold(float minThreshold) {
        this.minThreshold = minThreshold;
    }
}
