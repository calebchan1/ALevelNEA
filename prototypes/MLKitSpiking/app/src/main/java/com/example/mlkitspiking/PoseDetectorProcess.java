package com.example.mlkitspiking;

import android.content.Context;

import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PoseDetectorProcess {
    private final Context context;
    private final PoseDetectorOptionsBase options;
    private final Executor classificationExecutor;
    private final PoseDetector detector;
    public PoseDetectorProcess(Context context, PoseDetectorOptionsBase options){
        this.context = context;
        this.options = options;
        classificationExecutor = Executors.newSingleThreadExecutor();
        detector = PoseDetection.getClient(options);
    }

    public void stopTracking(){
        detector.close();
    }


}


