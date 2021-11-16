package com.example.exercisetracker;

import static com.example.exercisetracker.BaseApp.CHANNEL_1_ID;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
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
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RunningActivity extends AppCompatActivity  {
    //Sensors
    private SensorManager sensorManager;
    private SensorEventListener listener;
    private LocationManager locationManager;
    private LocationListener locationListener;
    //TextViews
    private TextView timerText;
    private TextView stepText;
    private TextView calorieText;
    private TextView distText;
    private TextView paceText;
    //Buttons
    private MaterialButton finishBtn;
    private MaterialButton startStopBtn;
    //notification
    private NotificationManagerCompat notificationManagerCompat;


    //Specialised running variables
    private float MET = 7.0F;
    private double distance;
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
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    isRunning = Boolean.FALSE;
                    this.finish();
                }
            });

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //visuals
        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.main_colour));// set status background white
        setContentView(R.layout.activity_running);

        //instantiating all private variables
        Date timestarted = Calendar.getInstance().getTime();
        isRunning = true;
        seconds = 0;
        steps = 0;
        distance = 0f;
        timerText = findViewById(R.id.timerText);
        stepText = findViewById(R.id.stepText);
        distText = findViewById(R.id.distText);
        paceText = findViewById(R.id.paceText);
        calorieText = findViewById(R.id.calText);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //CUSTOM JAVA CLASSES
        filter = new Filter(-10f, 10f);
        detector = new Detector(1f, 2);
        ArrayList<Double[]> currentRoute = new ArrayList<Double[]>();
        route = new Route(currentRoute);
        //NOTIFICATION MANAGER
        notificationManagerCompat = NotificationManagerCompat.from(this);

        //handling when start and stop button clicked
        startStopBtn = findViewById(R.id.startStopBtn);
        startStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRunning) {
                    startStopBtn.setText("Resume");
                    isRunning = false;

                } else {
                    startStopBtn.setText("Pause");
                    isRunning = true;
                }
            }
        });
        finishBtn = findViewById(R.id.finishBtn);
        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRunning = false;
                sensorManager.unregisterListener(listener);
                locationManager.removeUpdates(locationListener);
                //exiting the running activity and sending data back to main program
                Activity activity = new Activity(timestarted,seconds,"running");
                if (activity.saveActivity(getFilesDir().toString()) == Boolean.TRUE){
                    Toast.makeText(RunningActivity.this, "Save successful", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(RunningActivity.this, "Save unsuccessful", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });

        //HANDLING PERMISSIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            PERMISSIONS = new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            };
        }
        else{
            PERMISSIONS = new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            };
        }
        if (checkPermissions(this,PERMISSIONS) == Boolean.FALSE){
            for (String permission: PERMISSIONS){
                requestPermissionLauncher.launch(permission);
            }
        }
        else{
            startRunning();
        }
    }

    @SuppressLint("MissingPermission")
    private void startRunning() {
        //handling location changes
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if (isRunning) {
                    //location in form of latitude and longitude
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    Double[] entry = {latitude, longitude};
                    route.addRoute(entry);
                }
            }

        };
        //sensor managers
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,2,locationListener);

        //creating handler to run simultaneously to track duration in seconds
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                handler.postDelayed(this, 1000);
                if (isRunning) {
                    seconds++;
                    //calculating distances between location updates and updating text views
                    if (route.getRouteSize()>=2) {
                        DecimalFormat df = new DecimalFormat("#.##");
                        route.calculateDistance();
                        distance = route.getDistance();
                        distText.setText(String.format("Distance:\n%sm", df.format(distance)));
                        //changing pace text view
                        paceText.setText(Html.fromHtml("Pace:\n"+String.valueOf(df.format(distance/seconds.floatValue()))+"ms<sup>-1</sup"));

                    }
                    //changing timer text view
                    int hours = seconds / 3600;
                    int minutes = (seconds % 3600) / 60;
                    int secs = seconds % 60;
                    String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
                    timerText.setText(time);

                    //changing calorie text view
                    calories = Math.round(MET * User.getWeight() * (seconds.floatValue() / 3600));
                    calorieText.setText(String.format("Calories:\n%d", calories));

                    //changing step text view
                    stepText.setText(String.format("Steps:\n%d", steps));
                    //allowing preprocessing to happen at the instance of a 5 second interval
                    if ((seconds % 5) == 0) {
                        hasProcessed = Boolean.FALSE;
                    }

                }

            }
        });


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
                    entry[0] = x;
                    entry[1] = y;
                    entry[2] = z;
                    System.out.println("acceleration:" + String.format("%f, %f, %f", entry[0], entry[1], entry[2]));
                    accel.add(entry);


                }
                if (sensor.getType() == Sensor.TYPE_GRAVITY & isRunning) {
                    //handling gravimeter
                    Float x = Float.parseFloat(df.format(event.values[0]));
                    Float y = Float.parseFloat(df.format(event.values[1]));
                    Float z = Float.parseFloat(df.format(event.values[2]));
                    Float[] entry = new Float[3];
                    entry[0] = x;
                    entry[1] = y;
                    entry[2] = z;
                    grav.add(entry);
                    System.out.println("gravity: " + String.format("%f, %f, %f", entry[0], entry[1], entry[2]));
                }


                //PROCESSING DATA
                if (((seconds % 5) == 0 && (grav.size() > 0)) && (accel.size() > 0) && (hasProcessed == Boolean.FALSE)) {
                    ArrayList<Float> results = new ArrayList<Float>();
                    //PRE-PROCESSING DATA
                    //handling when grav array and accel array are unequal:
                    while (accel.size() != grav.size()) {
                        if (accel.size() > grav.size()) {
                            accel.remove(accel.size() - 1);

                        } else {
                            grav.remove(grav.size() - 1);
                        }
                    }

                    System.out.println("Seconds: " + seconds);
                    //PERFORM DOT PRODUCT
                    System.out.println(String.format("gravsize: %d accelsize: %d", grav.size(), accel.size()));
                    for (int j = 0; j < grav.size(); j++) {
                        Float[] accelValues = accel.get(j);
                        Float[] gravValues = grav.get(j);
                        Float result = Float.parseFloat(df.format(gravValues[0] * accelValues[0] + gravValues[1] * accelValues[1] + gravValues[2] * accelValues[2]));
                        if (result < 0) {
                            result = (float) Math.sqrt(0 - result);
                            result = 0 - result;
                        } else {
                            result = (float) Math.sqrt(result);
                        }
                        results.add(result);
                        System.out.println("result: " + j + " " + result.toString());
                    }
                    grav.clear();
                    accel.clear();
                    //hasProcessed to true to prevent small chunks of data being processed
                    hasProcessed = Boolean.TRUE;

                    //FILTERING DATA
                    filter.filter(results);
                    filtered_data = filter.getFiltered_data();
                    //WRITING TO FILE FOR DEBUGGING
//                    for (int i = 0; i < filtered_data.length; i++) {
//                        String entry = filtered_data[i].toString() + "\n";
//                        System.out.print(entry);
//                        try {
//                            File storage = Environment.getExternalStorageDirectory();
//                            File dir = new File(storage.getAbsolutePath() + "/documents");
//                            File file = new File(dir, "output.csv");
//                            FileOutputStream f = new FileOutputStream(file, true);
//                            try {
//                                f.write(entry.getBytes());
//                                f.flush();
//                                f.close();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        }
//                    }

                    //detecting steps
                    detector.detect(filtered_data);
                    steps = detector.getStepCount();

                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listener, sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_NORMAL);
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

    //handling live notification bar
    public void sendOnChannel1(View v){
        Notification notification = new NotificationCompat.Builder(this,CHANNEL_1_ID)
                .setSmallIcon(R.id.icon)
                .setContentTitle("Running Tracking")
                .setContentText(String.valueOf(steps))
                .setCategory(NotificationCompat.CATEGORY_WORKOUT)
                .build();
        notificationManagerCompat.notify(1,notification);
    }
}
