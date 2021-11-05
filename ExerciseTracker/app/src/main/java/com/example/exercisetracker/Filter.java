package com.example.exercisetracker;

import java.util.ArrayList;

public class Filter {
    private Float[] filtered_data;
    private Float minThreshold;
    private Float maxThreshold;

    public Filter(float minThreshold, float maxThreshold) {
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;
    }

    public void filter(ArrayList<Float> data){
        filtered_data = new Float[data.size()];
        for (int i=0 ;i<data.size();i++){
            if (data.get(i)<minThreshold){
                filtered_data[i] = (float) 0;
            }
            if (data.get(i)>maxThreshold){
                filtered_data[i] = maxThreshold;
            }
            if ((data.get(i)>=minThreshold) & (data.get(i)<=maxThreshold)){
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
