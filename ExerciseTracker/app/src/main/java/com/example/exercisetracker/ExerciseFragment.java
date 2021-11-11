package com.example.exercisetracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
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
        runBtn.setOnClickListener(this);
        walkBtn.setOnClickListener(this);
        return view;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.runBtn:
                Intent intent = new Intent(getContext(), RunningActivity.class);
                startActivity(intent);
                break;
            case R.id.walkBtn:
                Toast.makeText(getContext(), "Walking Tracking", Toast.LENGTH_SHORT).show();
                break;
        }
    }


}
