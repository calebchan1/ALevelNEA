package com.example.exercisetracker.repDetection;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

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
    private final Float minDistance;
    private boolean enteredPose;
    private final TextView indicator;
    private final TextView debugTV;
    private int reps;
    private final Context context;
    //calculations
    private int duration; //duration of n samples lapsed since entered pose
    private boolean countedRep;
    private boolean pushedDown;
    private boolean returnedToPosition;
    private Map<Integer, PointF3D> startPoint;
    private final int type; //type refers to type of exercise
    //0 for push up and 1 for squats

    public RepCounter(Context context, Integer type, TextView indicator, TextView debug, Float minDistance) {
        this.context = context;
        this.minDistance = minDistance;
        this.debugTV = debug;
        this.indicator = indicator;
        this.type = type;
        reps = 0;
        enteredPose = false;
        duration = 0;
        pushedDown = false;
        returnedToPosition = false;
        countedRep = false;

    }

    public int getReps() {
        return reps;
    }

    private void reset() {
        countedRep = false;
        pushedDown = false;
        returnedToPosition = false;
    }

    public void addEntry(List<PoseLandmark> landmarks) {
        if (type == 0) {
            //rep counting for push up
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
                if (enteredPose) {
                    detectPushUpReps(relevantLandmarks);
                    //debugTV.setText(relevantLandmarks.get(PoseLandmark.NOSE).getY()+"\n Start pos:"+startPoint.get(PoseLandmark.NOSE).getY() + "\nDuration: "+duration);
                } else {
                    reset();
                    duration = 0;
                    //debugTV.setText("0\n0\n0");
                }
                updateIndicator();
            }
        } else if (type == 1) {
            //rep counting for squats
            //rep counting for push up
            if (!landmarks.isEmpty()) {
                Map<Integer, PointF3D> relevantLandmarks = getRelevantLandmarks(landmarks);
                //Z value decreases as you move close to the phone
                //Using Z value from ML KIT, determining if body in a squat position
                //i.e. hips LARGER z value than knees and upper body by a minimum z distance away
                enteredPose =
                        relevantLandmarks.get(PoseLandmark.LEFT_HIP).getZ() > relevantLandmarks.get(PoseLandmark.LEFT_KNEE).getZ()
                                && relevantLandmarks.get(PoseLandmark.RIGHT_HIP).getZ() > relevantLandmarks.get(PoseLandmark.RIGHT_KNEE).getZ();
                if (enteredPose) {
                    detectSquatReps(relevantLandmarks);
                    debugTV.setText(relevantLandmarks.get(PoseLandmark.NOSE).getY() + "\n Start pos:" + startPoint.get(PoseLandmark.NOSE).getY() + "\nDuration: " + duration);
                } else {
                    reset();
                    duration = 0;
                    debugTV.setText("0\n0\n0");
                }
                updateIndicator();
            }
        }
    }

    private void detectPushUpReps(Map<Integer, PointF3D> relevantLandmarks) {
        //detecting push up reps
        if (duration == 0) {
            //first time entering pose
            startPoint = relevantLandmarks;
        }
        //calculation of reps is done in a process:
        //when user first initially enters pose, the position is recorded
        //user has to then push down by at least x amount and return to original position for a rep to be counted
        if (countedRep) {
            //instance of the rep was already counted previously, thus all boolean variables must reset
            reset();
        } else {
            if (!pushedDown) {
                //checking to see if user has pushed down by tracking movement of nose
                pushedDown = relevantLandmarks.get(PoseLandmark.NOSE).getY() >= startPoint.get(PoseLandmark.NOSE).getY() + minDistance;
            } else {
                //user has pushed down, thus checking to see if they have returned to position by tracking movement of nose
                if (!returnedToPosition) {
                    returnedToPosition = relevantLandmarks.get(PoseLandmark.NOSE).getY() < startPoint.get(PoseLandmark.NOSE).getY();
                }
            }
            if (pushedDown && returnedToPosition && !countedRep) {
                //if that instance of rep was not counted yet
                Toast.makeText(this.context, "Rep Counted", Toast.LENGTH_SHORT).show();
                reps++;
                countedRep = true;
            } else {
                duration++;
            }
        }
    }

    private void detectSquatReps(Map<Integer, PointF3D> relevantLandmarks) {
        //detecting squat reps
        //calculation of reps is done in a process:
        //when user first initially enters pose, the position is recorded
        //user has to then move in the z direction by a min amount and return to original position for a rep to be counted
        if (duration == 0) {
            //first time entering pose
            startPoint = relevantLandmarks;
        }
        if (countedRep) {
            reset();
        } else {
            if (!pushedDown) {
                //checking to see if user has squatted down by tracking movement of difference of z distance
                //between the knees and the hips
                pushedDown = relevantLandmarks.get(PoseLandmark.LEFT_HIP).getZ() > relevantLandmarks.get(PoseLandmark.LEFT_KNEE).getZ() + minDistance
                        && relevantLandmarks.get(PoseLandmark.RIGHT_HIP).getZ() > relevantLandmarks.get(PoseLandmark.RIGHT_KNEE).getZ() + minDistance
                        && relevantLandmarks.get(PoseLandmark.NOSE).getY() > startPoint.get(PoseLandmark.NOSE).getY() + minDistance;
            } else {
                //user has pushed down, thus checking to see if they have returned to position by tracking movement of nose
                if (!returnedToPosition) {
                    returnedToPosition = relevantLandmarks.get(PoseLandmark.NOSE).getY() < startPoint.get(PoseLandmark.NOSE).getY() + minDistance;
                }
            }
            if (pushedDown & returnedToPosition & !countedRep) {
                //if that instance of rep was not counted yet
                Toast.makeText(this.context, "Rep Counted", Toast.LENGTH_SHORT).show();
                reps++;
                countedRep = true;
            }
            else{
                duration ++;
            }

        }
    }


    private void updateIndicator() {
        //method to update text view indicator on screen to show if user has entered a push up or not
        if (type == 0) {
            if (enteredPose && pushedDown && returnedToPosition) {
                indicator.setTextColor(ContextCompat.getColor(context, R.color.green));
                indicator.setText("Push Up Entered, Pushed Down, Returned");
            } else if (enteredPose && pushedDown) {
                indicator.setTextColor(ContextCompat.getColor(context, R.color.green));
                indicator.setText("Push Up Entered, Pushed Down");
            } else if (enteredPose) {
                indicator.setTextColor(ContextCompat.getColor(context, R.color.green));
                indicator.setText("Push Up Entered");
            } else {
                indicator.setTextColor(ContextCompat.getColor(context, R.color.red));
                indicator.setText("Push Up Not Entered");
            }
        } else if (type == 1) {
            if (enteredPose && pushedDown && returnedToPosition) {
                indicator.setTextColor(ContextCompat.getColor(context, R.color.green));
                indicator.setText("Squats Entered, Squatted Down, Returned");
            } else if (enteredPose && pushedDown) {
                indicator.setTextColor(ContextCompat.getColor(context, R.color.green));
                indicator.setText("Squats Entered, Squatted Down");
            } else if (enteredPose) {
                indicator.setTextColor(ContextCompat.getColor(context, R.color.green));
                indicator.setText("Squats Entered");
            } else {
                indicator.setTextColor(ContextCompat.getColor(context, R.color.red));
                indicator.setText("Squats Not Entered");
            }
        }
    }


    private Map<Integer, PointF3D> getRelevantLandmarks(List<PoseLandmark> landmarks) {
        //filtering through all landmarks given by ML kit to only relevant ones for push up and squats
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
