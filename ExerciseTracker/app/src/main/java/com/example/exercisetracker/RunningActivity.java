package com.example.exercisetracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

public class RunningActivity extends AppCompatActivity {
    //Sensors
    private SensorManager sensorManager;
    private SensorEventListener listener;
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
    private Boolean hasProcessed;
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

        //CUSTOM JAVA CLASSES
        filter = new Filter((float) -10, (float) 10);
        detector = new Detector((float)0.09);

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
                isRunning=false;
                sensorManager.unregisterListener(listener);
                //exiting the running activity and sending data back to main program
                finish();
            }
        });

        //creating handler to run simultaneously to track duration in seconds
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this,1000);
                //changing the timerText every second that handler is delayed
                if (isRunning){
                    seconds ++;
                }
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;
                String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
                timerText.setText(time);

                //allowing preprocessing to happen at the instance of a 5 second interval
                if ((seconds % 5)==0){
                    hasProcessed = Boolean.FALSE;
                }


            }
        });

        //handling accelerometer and gravimeter
        checkPermissions();
        //2d arrays to store a variable amount of samples, each sample consisting of the x y z values
        ArrayList<Float[]> accel = new ArrayList<Float[]>();
        ArrayList<Float[]> grav = new ArrayList<Float[]>();
        listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                Sensor sensor = event.sensor;
                //formatting by rounding to 2 decimal places
                DecimalFormat df = new DecimalFormat("#.##");
                if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION & isRunning) {
                    //handling the linear acceleration
                    Float x = Float.parseFloat(df.format(event.values[0]));
                    Float y = Float.parseFloat(df.format(event.values[1]));
                    Float z = Float.parseFloat(df.format(event.values[2]));
                    Float[] entry = new Float[3];
                    entry[0] = x; entry[1] = y; entry[2] = z;
                    System.out.println("acceleration:" + String.format("%f, %f, %f",entry[0],entry[1],entry[2]));
                    accel.add(entry);


                }
                if (sensor.getType() == Sensor.TYPE_GRAVITY & isRunning){
                    //handling gravimeter
                    Float x = Float.parseFloat(df.format(event.values[0]));
                    Float y = Float.parseFloat(df.format(event.values[1]));
                    Float z = Float.parseFloat(df.format(event.values[2]));
                    Float[] entry = new Float[3];
                    entry[0] = x; entry[1] = y; entry[2] = z;
                    grav.add(entry);
                    System.out.println("gravity: " + String.format("%f, %f, %f",entry[0],entry[1],entry[2]));
                }

                //PROCESSING DATA
                if (((seconds%5)==0 && (grav.size()>0)) && (accel.size()>0) && (hasProcessed==Boolean.FALSE)){
                    ArrayList<Float> results = new ArrayList<Float>();
                    //PRE-PROCESSING DATA
                    //handling when grav array and accel array are unequal:
                    while (accel.size()!=grav.size()){
                        if (accel.size()>grav.size()){
                            accel.remove(accel.size()-1);

                        }
                        else{
                            grav.remove(grav.size()-1);
                        }
                    }

                    System.out.println("Seconds: " +seconds);
                    //PERFORM DOT PRODUCT
                    System.out.println(String.format("gravsize: %d accelsize: %d",grav.size(),accel.size()));
                    for (int j=0;j<grav.size();j++){
                        Float[] accelValues = accel.get(j);
                        Float[] gravValues = grav.get(j);
                        Float result = Float.parseFloat(df.format(gravValues[0]*accelValues[0]+gravValues[1]*accelValues[1]+gravValues[2]*accelValues[2]));
                        results.add(result);
                        System.out.println("result: "+j+" "+result.toString());
                    }
                    grav.clear();
                    accel.clear();
                    //hasProcessed to true to prevent small chunks of data being processed
                    hasProcessed = Boolean.TRUE;

                    //FILTERING DATA
                    filter.filter(results);
                    filtered_data = filter.getFiltered_data();
                    for (int i =0;i<filtered_data.length;i++){
                        String entry = filtered_data[i].toString() + "\n";
                            System.out.print(entry);
                            try {
                                File storage = Environment.getExternalStorageDirectory();
                                File dir = new File(storage.getAbsolutePath() + "/documents");
                                File file = new File(dir, "output.csv");
                                FileOutputStream f = new FileOutputStream(file, true);
                                try {
                                    f.write(entry.getBytes());
                                    f.flush();
                                    f.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                    }

                    //detecting steps
                    detector.detect(filtered_data);
                    steps = detector.getStepCount();
                    stepText.setText(String.format("Steps:\n%d",steps));
                }

                //for every 5 seconds, filter the data and pass through detector
//                    if ((seconds % 5) == 0) {
//                        filter.filter(temp);
//                        filtered_data = filter.getFiltered_data();
//                        for (int i = 0; i < filtered_data.length; i++) {
//                            String entry = filtered_data[i].toString() + "\n";
//                            System.out.print(entry);
//                            try {
//                                File storage = Environment.getExternalStorageDirectory();
//                                File dir = new File(storage.getAbsolutePath() + "/documents");
//                                File file = new File(dir, "output.csv");
//                                FileOutputStream f = new FileOutputStream(file, true);
//                                try {
//                                    f.write(entry.getBytes());
//                                    f.flush();
//                                    f.close();
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            } catch (FileNotFoundException e) {
//                                e.printStackTrace();
//                            }
//
//                        }
//                        //insert code here to handle detection of steps
//                        detector.detect(filtered_data);
//                        //clearing temp for next sequences of values.
//                        temp.clear();
//                    } else {
//                        temp.add(mag);
//                    }


            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        sensorManager.registerListener(listener,sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listener,sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),SensorManager.SENSOR_DELAY_NORMAL);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermissions() {
        int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            return;
        }
    }

}
