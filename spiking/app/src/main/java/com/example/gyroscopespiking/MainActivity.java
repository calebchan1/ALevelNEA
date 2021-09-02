package com.example.gyroscopespiking;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private Sensor gyroscope;
    private SensorEventListener gyroscopeEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        if (gyroscope == null) {
            Toast.makeText(this, "This device has no gyroscope", Toast.LENGTH_SHORT).show();
            finish();
        }
        gyroscopeEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.values[2]>0.5f){
                    getWindow().getDecorView().setBackgroundColor(Color.BLUE);
                }
                else if (event.values[2]<-0.5f){
                    getWindow().getDecorView().setBackgroundColor(Color.YELLOW);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(gyroscopeEventListener,gyroscope,SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(gyroscopeEventListener);
    }
}