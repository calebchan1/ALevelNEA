package com.example.exercisetracker.activities;

import static com.example.exercisetracker.other.BaseApp.CHANNEL_1_ID;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.exercisetracker.R;
import com.example.exercisetracker.other.Detector;
import com.example.exercisetracker.other.Filter;
import com.example.exercisetracker.other.User;
import com.example.exercisetracker.other.dbhelper;
import com.google.android.material.button.MaterialButton;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

public class TreadmillActivity extends AppCompatActivity {
    //Sensors
    private SensorManager sensorManager;
    private SensorEventListener listener;
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
    private float MET;
    private double distance;
    private Filter filter;
    private Detector detector;
    private Boolean isRunning;
    private Integer seconds;
    private Integer steps;
    private Integer calories;
    private Float[] filtered_data;
    private Boolean hasProcessed;
    private Integer height;
    private String timeStarted;
    private Date date;

    //audio
    private TextToSpeech tts;

    //Permissions
    private String[] PERMISSIONS;
    private ActivityResultLauncher<String> requestPermissionLauncher;


    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        }
        getWindow().setNavigationBarColor(getResources().getColor(R.color.main_colour));
        getWindow().setStatusBarColor(getResources().getColor(R.color.main_colour));
        setContentView(R.layout.activity_treadmill);

        init();
        handlePermissions();
    }

    private void init(){
        //instantiating all private variables
        seconds = 0;
        steps = 0;
        distance = 0f;
        height = User.getHeight();
        MET = Float.parseFloat(getString(R.string.met_treadmill));

        long millis = System.currentTimeMillis();
        Timestamp timestamp = new Timestamp(millis);
        timeStarted = timestamp.toString().substring(11, 16);
        date = new java.sql.Date(millis);

        timerText = findViewById(R.id.timerText);
        stepText = findViewById(R.id.stepText);
        distText = findViewById(R.id.distText);
        paceText = findViewById(R.id.paceText);
        calorieText = findViewById(R.id.calText);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //CUSTOM JAVA CLASSES
        filter = new Filter(-10f, 10f);
        detector = new Detector(0.5f, 2);
        //NOTIFICATION MANAGER
        notificationManagerCompat = NotificationManagerCompat.from(this);

        //text to speech instantiation
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                // if No error is found then only it will run
                if (i != TextToSpeech.ERROR) {
                    // To Choose language of speech
                    tts.setLanguage(Locale.UK);
                }
            }
        });

        //handling when start and stop button clicked
        startStopBtn = findViewById(R.id.startStopBtn);
        finishBtn = findViewById(R.id.finishBtn);

    }

    @SuppressLint("MissingPermission")
    private void startRunning() {
        isRunning = true;
        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //exiting the running activity and sending data back to main program
                finishRunning();
            }
        });
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
        createTimer();

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
                    Float[] entry = convertToEntry(event.values[0], event.values[1], event.values[2], df);
                    System.out.println("acceleration:" + String.format("%f, %f, %f", entry[0], entry[1], entry[2]));
                    accel.add(entry);
                }
                if (sensor.getType() == Sensor.TYPE_GRAVITY & isRunning) {
                    //handling gravimeter
                    Float[] entry = convertToEntry(event.values[0], event.values[1], event.values[2], df);
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
                        result = result / 9.81f;
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

    private Float[] convertToEntry(Float raw_x, Float raw_y, Float raw_z, DecimalFormat df) {
        Float x = Float.parseFloat(df.format(raw_x));
        Float y = Float.parseFloat(df.format(raw_y));
        Float z = Float.parseFloat(df.format(raw_z));
        Float[] entry = new Float[3];
        entry[0] = x;
        entry[1] = y;
        entry[2] = z;
        return entry;
    }

    private void createTimer(){
        //creating handler to run simultaneously to track duration in seconds
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                handler.postDelayed(this, 1000);
                if (isRunning) {
                    //rather than using geolocation and routes, treadmill is in one location
                    //distance is calculated using the average stride based off their height*0.4 to a good approximation
                    //calculating distance and changing distance text view
                    seconds++;
                    distance = height.floatValue() * ((float) Math.floor(steps / 2)) * 0.004f; //0.004 as user height stored as cm
                    DecimalFormat df = new DecimalFormat("#.##");
                    updateViews(df);
                    //allowing preprocessing to happen at the instance of a 5 second interval
                    if ((seconds % 5) == 0) {
                        hasProcessed = Boolean.FALSE;
                    }

                }

            }
        });
    }

    private void updateViews(DecimalFormat df) {
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
        distText.setText(String.format("Distance:\n%sm", df.format(distance)));
        //changing pace text view
        paceText.setText(Html.fromHtml("Pace:\n" + df.format(distance / seconds.floatValue()) + "ms<sup>-1</sup"));
    }

    private void handlePermissions(){
        //HANDLING PERMISSIONS
        PERMISSIONS = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };

        if (checkPermissions(this, PERMISSIONS) == Boolean.FALSE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(PERMISSIONS, 0);
            }
        } else {
            startRunning();
        }
    }

    //PERMISSIONS
    private boolean checkPermissions(Context context, String[] PERMISSIONS) {
        //CHECKING FOR EXISTING PERMISSIONS
        if (context != null && PERMISSIONS != null) {
            for (String permission : PERMISSIONS) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void finishRunning() {
        isRunning = false;
        sensorManager.unregisterListener(listener);

        //audio text to speech to congratulate user
        tts.speak(String.format("Congratulations, you burnt %d calories. See you next time!", calories), TextToSpeech.QUEUE_FLUSH, null);

        //exiting the running activity and saving data to database
        if (seconds > 60) {
            dbhelper helper = new dbhelper(TreadmillActivity.this);
            if (helper.saveActivity("treadmill", date.toString(), timeStarted, seconds.toString(), calories.toString(), steps.toString(), String.valueOf(distance), null)) {
                Toast.makeText(TreadmillActivity.this, "Save successful", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(TreadmillActivity.this, "Save unsuccessful", Toast.LENGTH_SHORT).show();
            }
        } else {
            //saves space and resources on database
            Toast.makeText(TreadmillActivity.this, "Activity too short, save unsuccessful", Toast.LENGTH_SHORT).show();
        }
        this.finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0:
                if (grantResults.length > 0) {
                    //checking if all permissions are granted on UI dialog
                    boolean granted = true;
                    for (int result : grantResults) {
                        if (result == PackageManager.PERMISSION_DENIED) {
                            granted = false;
                        }
                    }
                    if (granted) {
                        startRunning();
                    } else {
                        Toast.makeText(this, "Permissions Denied\nPlease allow permissions in settings", Toast.LENGTH_SHORT).show();
                        finishRunning();
                    }
                    return;
                }

        }
    }


    //handling live notification bar
    public void sendOnChannel1(View v) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.id.icon)
                .setContentTitle("Treadmill Tracking")
                .setContentText(String.valueOf(steps))
                .setCategory(NotificationCompat.CATEGORY_WORKOUT)
                .build();
        notificationManagerCompat.notify(1, notification);
    }
}
