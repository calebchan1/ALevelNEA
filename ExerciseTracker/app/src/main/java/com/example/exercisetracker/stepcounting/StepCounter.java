package com.example.exercisetracker.stepcounting;

import android.content.Context;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * StepCounter class encapsulates all methods and classes involving step detection.
 * Is used in activities requiring step detection from accelerometer and gravimeter
 * Currently, these activities include: RunningActivity, TreadmillActivity and WalkingActivity
 *
 * Is composed of Detector and Filter classes
 */
public class StepCounter {
    private Context context;
    private Detector detector;
    private Filter filter;
    private Integer steps;
    //2d arrays to store a variable amount of samples, each sample consisting of the x y z values
    private ArrayList<Float[]> grav;
    private ArrayList<Float[]> accel;

    private final DecimalFormat df;
    private Boolean hasProcessed;


    public Boolean getHasProcessed() {
        return hasProcessed;
    }

    public void setHasProcessed(Boolean hasProcessed) {
        this.hasProcessed = hasProcessed;
    }

    public Integer getSteps() {
        return steps;
    }

    public StepCounter(Context context, Integer stepDuration, Float DetectThresh, Float MinFilterThresh, Float MaxFilterThresh, DecimalFormat df){
        this.filter = new Filter(MinFilterThresh,MaxFilterThresh);
        this.detector = new Detector(DetectThresh, stepDuration);
        this.context = context;
        this.df = df;
        this.hasProcessed = Boolean.FALSE;
        this.accel = new ArrayList<>();
        this.grav = new ArrayList<>();
        this.steps = 0;
    }

    public void addEntry(Integer SensorType,Float x, Float y, Float z){
        switch(SensorType){
            case 0:
                // when sensorType is 0, from accelerometer
                Float[] entry = convertToEntry(x,y,z);
                System.out.println("acceleration:" + String.format("%f, %f, %f", entry[0], entry[1], entry[2]));
                accel.add(entry);
                this.accel.add(entry);
            case 1:
                // when sensorType is 1, from gravimeter
                Float[] entry1 = convertToEntry(x,y,z);
                System.out.println("gravity:" + String.format("%f, %f, %f", entry1[0], entry1[1], entry1[2]));
                grav.add(entry1);
                this.grav.add(entry1);
        }
    }

    public boolean isEmpty(){
        //if grav and accel arraylists are empty
        if (grav.isEmpty() && accel.isEmpty()){
            return true;
        }
        return false;
    }

    public void countSteps(){
        ArrayList<Float> results= processData();
        grav.clear();
        accel.clear();
        //hasProcessed to true to prevent small chunks of data being processed
        hasProcessed = Boolean.TRUE;

        //FILTERING DATA
        filter.filter(results);

        //detecting steps
        this.steps += detector.detect(filter.getFiltered_data());
    }

    private Float[] convertToEntry(Float raw_x, Float raw_y, Float raw_z) {
        Float x = Float.parseFloat(df.format(raw_x));
        Float y = Float.parseFloat(df.format(raw_y));
        Float z = Float.parseFloat(df.format(raw_z));
        Float[] entry = new Float[3];
        entry[0] = x;
        entry[1] = y;
        entry[2] = z;
        return entry;
    }

    private ArrayList<Float> processData(){
        ArrayList<Float> results = new ArrayList<Float>();
        //PRE-PROCESSING DATA
        //handling when grav array and accel array are unequal:
        while (accel.size() != grav.size()) {
            if (accel.size() > grav.size()) {
                accel.remove(accel.size() - 1);

            } else {
                grav.remove(grav.size() - 1);
            }
        }

        //PERFORM DOT PRODUCT
        for (int j = 0; j < grav.size(); j++) {
            Float[] accelValues = accel.get(j);
            Float[] gravValues = grav.get(j);
            Float result = Float.parseFloat(df.format(gravValues[0] * accelValues[0] + gravValues[1] * accelValues[1] + gravValues[2] * accelValues[2]));
            result = result / 9.81f;
            results.add(result);
            System.out.println("result: " + j + " " + result.toString());
        }
        return results;
    }
}
