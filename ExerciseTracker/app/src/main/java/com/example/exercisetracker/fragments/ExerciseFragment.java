package com.example.exercisetracker.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.example.exercisetracker.R;
import com.example.exercisetracker.activities.PushUpActivity;
import com.example.exercisetracker.activities.RunningActivity;
import com.example.exercisetracker.activities.SquatsActivity;
import com.example.exercisetracker.activities.TreadmillActivity;
import com.example.exercisetracker.activities.WalkingActivity;

public class ExerciseFragment extends Fragment implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private String[] PERMISSIONS;
    //notification
    private NotificationManagerCompat notificationManagerCompat;

    @Override
    public void onResume() {
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_exercise, container, false);
        Button runBtn = (Button) view.findViewById(R.id.runBtn);
        Button walkBtn = (Button) view.findViewById(R.id.walkBtn);
        Button treadmillBtn = (Button) view.findViewById(R.id.treadmillBtn);
        Button pushUpBtn = (Button) view.findViewById(R.id.pushUpBtn);
        Button squatBtn = (Button) view.findViewById(R.id.squatBtn);
        treadmillBtn.setOnClickListener(this);
        pushUpBtn.setOnClickListener(this);
        runBtn.setOnClickListener(this);
        walkBtn.setOnClickListener(this);
        squatBtn.setOnClickListener(this);
        return view;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.runBtn:
                //starting running activity
                Intent intent1 = new Intent(getContext(), RunningActivity.class);
                startActivity(intent1);

                break;
            case R.id.walkBtn:
                //starting walking activity
                Intent intent2 = new Intent(getContext(), WalkingActivity.class);
                startActivity(intent2);
                break;
            case R.id.treadmillBtn:
                //starting treadmill activity
                Intent intent3 = new Intent(getContext(), TreadmillActivity.class);
                startActivity(intent3);
                break;
            case R.id.pushUpBtn:
                //starting push up activity
                Intent intent4 = new Intent(getContext(), PushUpActivity.class);
                startActivity(intent4);
                break;
            case R.id.squatBtn:
                //starting squat activity
                Intent intent5 = new Intent(getContext(), SquatsActivity.class);
                startActivity(intent5);
        }
    }


}
