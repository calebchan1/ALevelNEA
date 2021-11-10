package com.example.exercisetracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

public class RunningActivity extends AppCompatActivity implements LocationListener, ActivityCompat.OnRequestPermissionsResultCallback {
    //Sensors
    private SensorManager sensorManager;
    private SensorEventListener listener;
    private LocationManager locationManager;

    //TextViews
    private TextView timerText;
    private TextView stepText;
    private TextView calorieText;
    private TextView distText;
    private TextView paceText;
    //Buttons
    private MaterialButton finishBtn;
    private MaterialButton startStopBtn;


    //Specialised running variables
    private float MET = 7.0F;
    private float distance;
    private Filter filter;
    private Detector detector;
    private Boolean isRunning;
    private Integer seconds;
    private Integer steps;
    private Integer calories;
    private Float[] filtered_data;
    private Boolean hasProcessed;
    private Route route;

    //Permissions
    private String[] PERMISSIONS;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_running);

        //handling permissions
        PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
        };
        if(checkPermissions(RunningActivity.this,PERMISSIONS) == Boolean.FALSE) {
            requestRunningPermissions(PERMISSIONS);
        }
        else{
            startRunning();
        }
    }

    private void startRunning(){
        //instantiating all private variables
        isRunning = true;
        seconds = 0;
        timerText = findViewById(R.id.timerText);
        stepText = findViewById(R.id.stepText);
        distText = findViewById(R.id.distText);
        paceText = findViewById(R.id.paceText);
        calorieText = findViewById(R.id.calText);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //CUSTOM JAVA CLASSES
        filter = new Filter((float) -10, (float) 10);
        detector = new Detector((float) 1.5, 2);
        ArrayList<Double[]> currentRoute = new ArrayList<Double[]>();
        route = new Route(currentRoute);

        //sensor managers
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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
            @SuppressLint("DefaultLocale")
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
                calories = Math.round(MET*User.getWeight()*(seconds.floatValue()/3600));
                calorieText.setText(String.format("Calories:\n%d",calories));
                DecimalFormat df = new DecimalFormat("#.##");
                paceText.setText("Pace:\n"+df.format(distance/seconds.floatValue()));
                //allowing preprocessing to happen at the instance of a 5 second interval
                if ((seconds % 5)==0){
                    hasProcessed = Boolean.FALSE;
                }


            }
        });


        //2d arrays to store a variable amount of samples, each sample consisting of the x y z values
        ArrayList<Float[]> accel = new ArrayList<Float[]>();
        ArrayList<Float[]> grav = new ArrayList<Float[]>();
        ArrayList<Float[]> geo = new ArrayList<Float[]>();

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
                        if (result<0){
                            result = (float) Math.sqrt(0-result);
                            result = 0-result;
                        }
                        else{
                            result = (float) Math.sqrt(result);
                        }
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

                    distance=0;
                }

            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        sensorManager.registerListener(listener,sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listener,sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),SensorManager.SENSOR_DELAY_NORMAL);
    }


    //HANDLING GPS TRACKING
    @Override
    public void onLocationChanged(@NonNull Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Double[] entry = {latitude,longitude};
        System.out.println(String.format("Latitude: %b Longitude:%b",latitude,longitude));
        route.addRoute(entry);
    }



    //PERMISSIONS
    private boolean checkPermissions(Context context, String[] PERMISSIONS) {
        //CHECKING FOR EXISTING PERMISSIONS
        if (context!=null && PERMISSIONS!=null){
            for (String permission: PERMISSIONS){
                if (ActivityCompat.checkSelfPermission(context,permission)!=PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }
        return true;
    }
    private void requestRunningPermissions(String[] PERMISSIONS){
        ActivityCompat.requestPermissions(this,PERMISSIONS,1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //handling how app responds to permissions being denied/accepted
        if (requestCode == 1){
            for (int i=0;i<permissions.length;i++){
                if (grantResults[i]==PackageManager.PERMISSION_GRANTED){
                }
                else{
                    //when permission is denied, running activity stops and alert is shown
                    Toast.makeText(this, "Permissions denied", Toast.LENGTH_LONG).show();
                    isRunning = false;
                    this.finish();

                }
            }
        }
    }
}
