package com.example.exercisetracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import org.w3c.dom.Text;

import java.io.Console;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;

public class RunningActivity extends AppCompatActivity {
    //Sensors
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener accelerometerEventListener;
    //TextViews
    private TextView timerText;
    private TextView stepText;
    private TextView calorieText;
    private TextView distText;
    //Buttons
    private MaterialButton finishBtn;
    private MaterialButton startStopBtn;
    //Specialised running variables
    private Filter filter;
    private Detector detector;
    private Boolean isRunning;
    private Integer seconds;
    private Integer steps;
    private Integer calories;
    private Float[] filtered_data;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_running);
        //instantiating all private variables
        isRunning = true;
        seconds = 0;
        timerText = findViewById(R.id.timerText);
        stepText = findViewById(R.id.stepText);
        distText = findViewById(R.id.distText);
        calorieText = findViewById(R.id.calText);
        sensorManager =(SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        filter = new Filter((float) 0.6, (float) 10);
        detector = new Detector((float)0.09);
        //creating handler to run simultaneously to track duration in seconds
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                //changing the timerText every second that handler is delayed
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;
                String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
                timerText.setText(time);
                if (isRunning){
                    seconds ++;
                }
                handler.postDelayed(this,1000);
            }
        });

        //handling when start and stop button clicked
        startStopBtn = findViewById(R.id.startStopBtn);
        startStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRunning){
                    isRunning = false;
                }
                else{
                    isRunning = true;
                }
            }
        });



        finishBtn = findViewById(R.id.finishBtn);
        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //exiting the running activity and sending data back to main program
                finish();
            }
        });

        //handling accelerometer permissions
        checkPermissions();
        if (accelerometer==null){
            Toast.makeText(this, "No accelerometer detected", Toast.LENGTH_SHORT).show();
            finish();
        }

        //handling accelerometer
        ArrayList<Float> temp = new ArrayList<>();
        accelerometerEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                DecimalFormat df = new DecimalFormat("#.####");
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                float mag = (float) Math.sqrt(x*x + y*y + z*z);
                //for every 5 seconds, filter the data and pass through detector
                if ((seconds % 5) == 0) {
                    filter.filter(temp);
                    filtered_data = filter.getFiltered_data();
                    for (int i=0;i<filtered_data.length;i++){
                        System.out.println(filtered_data[i].toString());
                    }
                    //insert code here to handle detection of steps
                    //clearing temp for next sequences of values.
                    temp.clear();
                } else {
                    temp.add(mag);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        sensorManager.registerListener(accelerometerEventListener,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermissions() {
        int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            return;
        }
        Toast.makeText(getBaseContext(), "Permission is already granted", Toast.LENGTH_LONG).show();
    }




}
