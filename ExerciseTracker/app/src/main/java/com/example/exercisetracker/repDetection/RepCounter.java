package com.example.exercisetracker.repDetection;

import android.content.Context;
import android.graphics.Point;
import android.widget.Toast;

import com.google.mlkit.vision.common.PointF3D;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RepCounter class used to detect number of reps from results of mlk tracking
 * Landmarks added as entries to class
 */

public class RepCounter {
    private boolean enteredPose;
    private int reps;
    private Context context;

    public RepCounter(Context context){
        this.context = context;
        enteredPose = false;
        reps = 0;
    }

    public void addEntry(List<PoseLandmark> landmarks){
        if (!landmarks.isEmpty()) {
            Map<Integer, PointF3D> relevantLandmarks = getRelevantLandmarks(landmarks);
            if (enteredPose) {
                //from previous landmarks, user was already in a push-up pose
            } else {
                //if user previously was not in a push up pose
                //Using Z value from ML KIT, determining if body is laying down horizontally
                //i.e. Lower body has a smaller z value than upper body
                boolean condition =
                        relevantLandmarks.get(PoseLandmark.LEFT_HIP).getZ()<relevantLandmarks.get(PoseLandmark.NOSE).getZ()
                        && relevantLandmarks.get(PoseLandmark.LEFT_KNEE).getZ()<relevantLandmarks.get(PoseLandmark.LEFT_HIP).getZ();

                if (condition){
                    Toast.makeText(context, "Entered Pose", Toast.LENGTH_SHORT).show();
                    enteredPose = true;
                }
                else{
                    enteredPose=false;
                }

            }
        }
    }


    private Map<Integer, PointF3D> getRelevantLandmarks(List<PoseLandmark> landmarks){
        Map<Integer, PointF3D> relevantLandmarks = new HashMap<>();
        for (PoseLandmark landmark : landmarks){
            switch (landmark.getLandmarkType()){
                case PoseLandmark.NOSE:
                    relevantLandmarks.put(PoseLandmark.NOSE,landmark.getPosition3D());
                    break;
                case PoseLandmark.LEFT_HIP:
                    relevantLandmarks.put(PoseLandmark.LEFT_HIP,landmark.getPosition3D());
                    break;
                case PoseLandmark.RIGHT_HIP:
                    relevantLandmarks.put(PoseLandmark.RIGHT_HIP,landmark.getPosition3D());
                    break;
                case PoseLandmark.LEFT_KNEE:
                    relevantLandmarks.put(PoseLandmark.LEFT_KNEE,landmark.getPosition3D());
                    break;
                case PoseLandmark.RIGHT_KNEE:
                    relevantLandmarks.put(PoseLandmark.RIGHT_KNEE,landmark.getPosition3D());
                    break;
            }
        }
        return relevantLandmarks;
    }
}
