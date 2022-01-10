package com.example.exercisetracker.repDetection;

import android.content.Context;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.exercisetracker.R;
import com.google.mlkit.vision.common.PointF3D;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RepCounter class used to detect number of reps from results of mlk tracking
 * Landmarks added as entries to class
 */

public class RepCounter {
    private boolean enteredPose;
    private TextView indicator;
    private int reps;
    private Context context;

    public RepCounter(Context context, TextView indicator) {
        this.context = context;
        enteredPose = false;
        reps = 0;
        this.indicator = indicator;
    }

    public void addEntry(List<PoseLandmark> landmarks) {
        if (!landmarks.isEmpty()) {
            Map<Integer, PointF3D> relevantLandmarks = getRelevantLandmarks(landmarks);
            //Z value decreases as you move close to the phone
            //Using Z value from ML KIT, determining if body is laying down horizontally
            //i.e. Lower body has a LARGER z value than upper body
            enteredPose =
                    relevantLandmarks.get(PoseLandmark.LEFT_HIP).getZ() > relevantLandmarks.get(PoseLandmark.NOSE).getZ()
                            && relevantLandmarks.get(PoseLandmark.LEFT_KNEE).getZ() > relevantLandmarks.get(PoseLandmark.LEFT_HIP).getZ()
                            && relevantLandmarks.get(PoseLandmark.RIGHT_HIP).getZ() > relevantLandmarks.get(PoseLandmark.NOSE).getZ()
                            && relevantLandmarks.get(PoseLandmark.RIGHT_KNEE).getZ() > relevantLandmarks.get(PoseLandmark.RIGHT_HIP).getZ();
            updateIndicator();
        }
    }

    private void updateIndicator() {
        //method to update text view indicator on screen to show if user has entered a push up or not
        if (enteredPose) {
            indicator.setTextColor(ContextCompat.getColor(context, R.color.green));
            indicator.setText("Push Up Entered");
        } else {
            indicator.setTextColor(ContextCompat.getColor(context, R.color.red));
            indicator.setText("Push Up Not Entered");
        }
    }


    private Map<Integer, PointF3D> getRelevantLandmarks(List<PoseLandmark> landmarks) {
        //filtering through all landmarks given by ML kit to only relevant ones for push up
        //these are the nose, hip and knee landmarks representing the face, lower and upper body
        Map<Integer, PointF3D> relevantLandmarks = new HashMap<>();
        for (PoseLandmark landmark : landmarks) {
            switch (landmark.getLandmarkType()) {
                case PoseLandmark.NOSE:
                    relevantLandmarks.put(PoseLandmark.NOSE, landmark.getPosition3D());
                    break;
                case PoseLandmark.LEFT_HIP:
                    relevantLandmarks.put(PoseLandmark.LEFT_HIP, landmark.getPosition3D());
                    break;
                case PoseLandmark.RIGHT_HIP:
                    relevantLandmarks.put(PoseLandmark.RIGHT_HIP, landmark.getPosition3D());
                    break;
                case PoseLandmark.LEFT_KNEE:
                    relevantLandmarks.put(PoseLandmark.LEFT_KNEE, landmark.getPosition3D());
                    break;
                case PoseLandmark.RIGHT_KNEE:
                    relevantLandmarks.put(PoseLandmark.RIGHT_KNEE, landmark.getPosition3D());
                    break;
            }
        }
        return relevantLandmarks;
    }
}
