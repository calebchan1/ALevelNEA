package com.example.exercisetracker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PushUpActivity extends AppCompatActivity {
    //Text Views
    private TextView timerText,repText,calText;
    //buttons
    private Button startBtn, finishBtn;

    //pushup custom variables
    private Boolean isTracking;
    private Integer seconds;
    private Date timeStarted;
    private Float MET;

    //MLK variables
    private PoseDetectorOptions options;
    private PoseDetector poseDetector;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Preview preview;
    private CameraSelector cameraSelector;


    //permissions
    private String[] PERMISSIONS;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //visuals
        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.main_colour));// set status background white
        setContentView(R.layout.activity_pushup);
        //instantiating all variables
        isTracking = Boolean.TRUE;
        startBtn = findViewById(R.id.startStopBtn);
        finishBtn = findViewById(R.id.finishBtn);
        timerText = findViewById(R.id.timerText);
        repText = findViewById(R.id.repText);
        calText = findViewById(R.id.calText);
        MET = Float.parseFloat(getString(R.string.met_pushup));
        //handling button clicks
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when start stop button is clicked
                if (isTracking) {
                    startBtn.setText("Resume");
                    isTracking = false;

                } else {
                    startBtn.setText("Pause");
                    isTracking = true;
                }
            }
        });
        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when finish button is clicked
                isTracking = false;
                finish();
            }
        });

        //handling camera and write permissions
        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        isTracking = Boolean.TRUE;
                    } else {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        isTracking = Boolean.FALSE;
                        this.finish();
                    }
                });
        PERMISSIONS = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        if (checkPermissions(this,PERMISSIONS) == Boolean.FALSE){
            for (String permission: PERMISSIONS){
                requestPermissionLauncher.launch(permission);
            }
        }
        else{
            startTracking();
        }

    }
    private void startTracking(){
        cameraProviderFuture = ProcessCameraProvider.getInstance(PushUpActivity.this);

        seconds = 0;
        timeStarted = Calendar.getInstance().getTime();
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this,1000);
                if (isTracking == Boolean.TRUE){
                    seconds++;
                    //updating timer view
                    int hours = seconds / 3600;
                    int minutes = (seconds % 3600) / 60;
                    int secs = seconds % 60;
                    String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
                    timerText.setText(time);


                }
            }
        });

        //MLK tracking
        options = new PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build();
        poseDetector = PoseDetection.getClient(options);

    }

    //PERMISSIONS
    private boolean checkPermissions(Context context, String[] PERMISSIONS) {
        //CHECKING FOR EXISTING PERMISSIONS
        if (context!=null && PERMISSIONS!=null){
            for (String permission: PERMISSIONS){
                if (ActivityCompat.checkSelfPermission(context,permission)!= PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }
        return true;
    }
}
