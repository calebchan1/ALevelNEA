package com.example.accelerometerspiking;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener accelerometerEventListener;
    private TextView textView;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager =(SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer =sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        textView = findViewById(R.id.textView);
        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE}, 1);


        if (accelerometer==null){
            Toast.makeText(this, "No accelerometer detected", Toast.LENGTH_SHORT).show();
            finish();
        }
        accelerometerEventListener = new SensorEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onSensorChanged(SensorEvent event) {
                float value = event.values[2];
                String s = Float.toString(value);
                textView.setText(s);
                //recording each datapoint
                String record = s + ",";
                File storage = Environment.getStorageDirectory();
                File dir = new File(MainActivity.this.getFilesDir(), "mydir");
                if (!dir.exists()) {
                    //checking if the directory exists
                    dir.mkdir();
                }
                try{
                    //committing changes to the file
                    File accelFile = new File(dir,"accelFile.csv");
                    FileWriter writer = new FileWriter(accelFile);
                    writer.append(record);
                    writer.flush();
                    writer.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
    }

    @Override
    protected void onResume() {
        //when the user resumes the app
        super.onResume();
        sensorManager.registerListener(accelerometerEventListener,accelerometer,SensorManager.SENSOR_DELAY_FASTEST);
    }
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(accelerometerEventListener);
    }

}