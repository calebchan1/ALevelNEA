package com.example.exercisetracker;

public class Filter {
    private float[] filtered_data;
    private float minThreshold;
    private float maxThreshold;

    public Filter(float minThreshold, float maxThreshold) {
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;
    }

    public void filter(float[] data){
        filtered_data = new float[data.length];
        for (int i=0 ;i<data.length;i++){
            if (data[i]<minThreshold){
                filtered_data[i] = (float) 0;
            }
            if (data[i]>maxThreshold){
                filtered_data[i] = maxThreshold;
            }
            if ((data[i]>=minThreshold) & (data[i]<=maxThreshold)){
                filtered_data[i] = data[i];
            }
        }
        setFiltered_data(filtered_data);
    }


    public float[] getFiltered_data() {
        return filtered_data;
    }

    public void setFiltered_data(float[] filtered_data) {
        this.filtered_data = filtered_data;
    }

    public float getMinThreshold() {
        return minThreshold;
    }

    public void setMinThreshold(float minThreshold) {
        this.minThreshold = minThreshold;
    }
}
