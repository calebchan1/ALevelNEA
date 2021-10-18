package com.example.accelerometerspiking;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import java.lang.Math;
import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
        accelerometer =sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        textView = findViewById(R.id.textView);
        checkPermissions();
        if (accelerometer==null){
            Toast.makeText(this, "No accelerometer detected", Toast.LENGTH_SHORT).show();
            finish();
        }
        accelerometerEventListener = new SensorEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onSensorChanged(SensorEvent event) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                String x1 = Float.toString(x);
                String y1 = Float.toString(y);
                String z1 = Float.toString(z);
                float mag = (float) Math.sqrt(x*x + y*y + z*z);
                textView.setText(x1 + "," + y1 + "," + z1 + ","+mag);
                //recording each datapoint
                String entry = x + ","+ y +","+z+","+mag+"\n";
                try {
                    File storage = Environment.getExternalStorageDirectory();
                    File dir = new File(storage.getAbsolutePath() + "/documents");
                    File file = new File(dir, "output.csv");
                    FileOutputStream f = new FileOutputStream(file, true);
                    try {
                        f.write(entry.getBytes());
                        f.flush();
                        f.close();
                        Toast.makeText(getBaseContext(), "Data saved", Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                } catch (FileNotFoundException e) {
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
        sensorManager.registerListener(accelerometerEventListener,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(accelerometerEventListener);
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