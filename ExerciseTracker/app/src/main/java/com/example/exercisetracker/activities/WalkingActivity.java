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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.exercisetracker.stepcounting.Detector;
import com.example.exercisetracker.stepcounting.Filter;
import com.example.exercisetracker.R;
import com.example.exercisetracker.other.Route;
import com.example.exercisetracker.stepcounting.StepCounter;
import com.example.exercisetracker.other.User;
import com.example.exercisetracker.other.dbhelper;
import com.google.android.material.button.MaterialButton;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

public class WalkingActivity extends AppCompatActivity{
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


    //Specialised walking variables
    private float MET;
    private double distance;
    private Boolean isWalking;
    private Integer seconds;
    private Integer steps;
    private Integer calories;
    private Route route;
    private String timeStarted;
    private Date date;
    private StepCounter stepCounter;

    //audio
    private TextToSpeech tts;
    private int currquote;
    private String[] quotes;

    //Permissions
    private String[] PERMISSIONS;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //visuals
        getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        }
        getWindow().setNavigationBarColor(getResources().getColor(R.color.main_colour));
        getWindow().setStatusBarColor(getResources().getColor(R.color.main_colour));
        setContentView(R.layout.activity_walking);

        init();

        if (checkPermissions(this, PERMISSIONS) == Boolean.FALSE) {
            //dealt with overriding onRequestPermissionsResult method
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(PERMISSIONS,0);
            }
        } else {
            startWalking();
        }
    }

    private void init(){
        //instantiating all private variables
        long millis=System.currentTimeMillis();
        Timestamp timestamp = new Timestamp(millis);
        timeStarted = timestamp.toString().substring(11,16);
        date = new Date(millis);
        isWalking = true;
        seconds = 0;
        steps = 0;
        distance = 0f;
        timerText = findViewById(R.id.timerText);
        stepText = findViewById(R.id.stepText);
        distText = findViewById(R.id.distText);
        paceText = findViewById(R.id.paceText);
        calorieText = findViewById(R.id.calText);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        MET = Float.parseFloat(getString(R.string.met_walking));

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

        //CUSTOM JAVA CLASSES
        stepCounter = new StepCounter(this, 2,0.5f,-10f,10f,new DecimalFormat("#.##"));
        ArrayList<Double[]> currentRoute = new ArrayList<Double[]>();
        route = new Route(currentRoute);
        //NOTIFICATION MANAGER
        notificationManagerCompat = NotificationManagerCompat.from(this);

        //handling when start and stop button clicked
        startStopBtn = findViewById(R.id.startStopBtn);
        finishBtn = findViewById(R.id.finishBtn);

        //HANDLING PERMISSIONS
        PERMISSIONS = new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    @SuppressLint("MissingPermission")
    private void startWalking() {
        //click listeners
        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishWalking();
            }
        });
        startStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isWalking) {
                    startStopBtn.setText("Resume");
                    isWalking = false;

                } else {
                    startStopBtn.setText("Pause");
                    isWalking = true;
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            //requesting background permission for android q+
            //Android forces you to request this separately
            isWalking =false;
            requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},1);
        }

        //handling location changes
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if (isWalking) {
                    //location in form of latitude and longitude
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    Double[] entry = {latitude, longitude};
                    System.out.println(entry);
                    route.addRoute(entry);
                }
            }

        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 3, locationListener);

        createTimer();
        //2d arrays to store a variable amount of samples, each sample consisting of the x y z values
        listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (isWalking) {
                    Sensor sensor = event.sensor;
                    if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                        //getting values from accelerometer
                        stepCounter.addEntry(0, event.values[0], event.values[1], event.values[2]);
                    } else if (sensor.getType() == Sensor.TYPE_GRAVITY & isWalking) {
                        //getting values from gravimeter
                        stepCounter.addEntry(1, event.values[0], event.values[1], event.values[2]);
                    }
                    //PROCESSING DATA
                    if ((seconds % 5) == 0 && (!stepCounter.isEmpty())) {
                        stepCounter.countSteps();
                        steps = stepCounter.getSteps();
                    }
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
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 0:
                if (grantResults.length>0) {
                    //checking if all permissions are granted on UI dialog
                    boolean granted = true;
                    for (int result : grantResults) {
                        if (result == PackageManager.PERMISSION_DENIED) {
                            granted = false;
                        }
                    }
                    if (granted) {
                        startWalking();
                    } else {
                        Toast.makeText(this, "Permissions Denied\nPlease allow permissions in settings", Toast.LENGTH_SHORT).show();
                        finishWalking();
                    }
                    return;
                }

            case 1:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    isWalking = true;
                    return;
                }
                else{
                    Toast.makeText(this, "Permissions Denied\nPlease allow permissions in settings", Toast.LENGTH_SHORT).show();
                    finishWalking();
                }
        }

    }

    private void finishWalking(){
        isWalking = false;
        //audio text to speech to congratulate user
        tts.speak(String.format("Congratulations, you burnt %d calories and walked %d steps. See you next time!", calories, steps), TextToSpeech.QUEUE_FLUSH, null);

        sensorManager.unregisterListener(listener);
        if (locationManager!=null && timeStarted!=null) {
            locationManager.removeUpdates(locationListener);
            //exiting the walking activity and saving data to database
            //will only save activities which last longer than 60s
            if (seconds>60){
                dbhelper helper = new dbhelper(WalkingActivity.this);
                if (helper.saveActivity("walking",date.toString(),timeStarted,seconds.toString(),calories.toString(),steps.toString(), String.valueOf(distance),null)) {
                    Toast.makeText(WalkingActivity.this, "Save successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(WalkingActivity.this, "Save unsuccessful", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                //saves space and resources on database
                Toast.makeText(WalkingActivity.this, "Activity too short, save unsuccessful", Toast.LENGTH_SHORT).show();
            }

        }
        this.finish();

    }
    private void createTimer() {
        //creating handler to run simultaneously to track duration in seconds
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                handler.postDelayed(this, 1000);
                if (isWalking) {
                    seconds++;
                    //calculating distances between location updates and updating text views
                    DecimalFormat df = new DecimalFormat("#.##");
                    if (route.getRouteSize() >= 2) {
                        route.calculateDistance();
                        distance = route.getDistance();
                    }
                    //allowing preprocessing to happen at the instance of a 5 second interval
                    if ((seconds % 5) == 0) {
                        stepCounter.setHasProcessed(Boolean.FALSE);
                    }
                    updateViews(df);
                    //updating notification every second

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

    //handling live notification bar
    public void sendOnChannel1(View v){
        Notification notification = new NotificationCompat.Builder(this,CHANNEL_1_ID)
                .setSmallIcon(R.id.icon)
                .setContentTitle("Walking Tracking")
                .setContentText(String.valueOf(steps))
                .setCategory(NotificationCompat.CATEGORY_WORKOUT)
                .build();
        notificationManagerCompat.notify(1,notification);
    }
}
