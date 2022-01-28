package com.example.exercisetracker.other;

import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.List;

public class RepCounter {
    private int reps;
    private int enterThreshold;
    private int exitThreshold;
    private boolean isEntered;
    private List<PoseLandmark> currLandmarks;

    public RepCounter(int enterThreshold, int exitThreshold) {
        this.enterThreshold = enterThreshold;
        this.exitThreshold = exitThreshold;
    }

    public void handleLandmarks(String type, List<PoseLandmark> allLandmarks) {
        if (type.equals("pushup")) {

        }
    }

    public void countReps() {

    }
}
