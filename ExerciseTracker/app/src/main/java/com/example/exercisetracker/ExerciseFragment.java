package com.example.exercisetracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

public class ExerciseFragment extends Fragment implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback{
    private String[] PERMISSIONS;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_exercise,container,false);
        Button runBtn = (Button) view.findViewById(R.id.runBtn);
        Button walkBtn = (Button) view.findViewById(R.id.walkBtn);
        Button treadmillBtn = (Button) view.findViewById(R.id.treadmillBtn);
        Button pushUpBtn = (Button) view.findViewById(R.id.pushUpBtn);
        treadmillBtn.setOnClickListener(this);
        pushUpBtn.setOnClickListener(this);
        runBtn.setOnClickListener(this);
        walkBtn.setOnClickListener(this);
        return view;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.runBtn:
                Intent intent1 = new Intent(getContext(), RunningActivity.class);
                startActivity(intent1);
                break;
            case R.id.walkBtn:
                Toast.makeText(getContext(), "Walking Tracking", Toast.LENGTH_SHORT).show();
                break;
            case R.id.treadmillBtn:
                Intent intent2 = new Intent(getContext(), TreadmillActivity.class);
                startActivity(intent2);
                break;
            case R.id.pushUpBtn:
                Intent intent3 = new Intent(getContext(), PushUpActivity.class);
                startActivity(intent3);
                break;
        }
    }


}
