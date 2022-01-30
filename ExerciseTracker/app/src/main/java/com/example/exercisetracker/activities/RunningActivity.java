package com.example.exercisetracker.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.exercisetracker.R;
import com.example.exercisetracker.other.DBhelper;
import com.example.exercisetracker.other.Route;
import com.example.exercisetracker.other.User;
import com.example.exercisetracker.stepcounting.StepCounter;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class RunningActivity extends AppCompatActivity {
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

    //Specialised running variables
    private float MET;
    private double distance;
    private Boolean isRunning;
    private Integer seconds;
    private Integer steps;
    private Integer calories;
    private Route route;
    private String timeStarted;
    private Date date;
    private double pace;
    private StepCounter stepCounter;

    //audio
    private TextToSpeech tts;
    private String[] quotes;

    private DecimalFormat df;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //visuals
        Objects.requireNonNull(getSupportActionBar()).hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        }
        getWindow().setNavigationBarColor(getResources().getColor(R.color.main_colour));
        getWindow().setStatusBarColor(getResources().getColor(R.color.main_colour));
        setContentView(R.layout.activity_running);
        init();
        handlePermissions();
    }


    private void init() {
        //instantiating all private variables
        long millis = System.currentTimeMillis();
        Timestamp timestamp = new Timestamp(millis);
        timeStarted = timestamp.toString().substring(11, 16);
        date = new Date(millis);
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
        MET = Float.parseFloat(getString(R.string.met_running));
        startStopBtn = findViewById(R.id.startStopBtn);
        finishBtn = findViewById(R.id.finishBtn);
        df = new DecimalFormat("#.##");

        //quotes from resources
        Resources res = getResources();
        quotes = res.getStringArray(R.array.quotes);


        //CUSTOM JAVA CLASSES
        stepCounter = new StepCounter(this, 2, 0.5f, -10f, 10f, new DecimalFormat("#.##"));
        ArrayList<Double[]> currentRoute = new ArrayList<>();
        route = new Route(currentRoute);

        //foreground services
//        Intent intent = new Intent(this, ExerciseService.class);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(intent);
//        }else{
//            startService(intent);
//        }
    }

    @SuppressLint("MissingPermission")
    private void startRunning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //requesting background permission for android q+
            //Android forces you to request this separately
            isRunning = false;
            requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1);
        }
        //click listeners
        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        //handling location changes
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if (isRunning) {
                    //location in form of latitude and longitude
                    pace = location.getSpeed();
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    Double[] entry = {latitude, longitude};
                    System.out.println(Arrays.toString(entry));
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
                if (isRunning) {
                    Sensor sensor = event.sensor;
                    if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                        //getting values from accelerometer
                        stepCounter.addEntry(0, event.values[0], event.values[1], event.values[2]);
                    } else if (sensor.getType() == Sensor.TYPE_GRAVITY & isRunning) {
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

    //saving to csv file for debugging
    private void saveToStorage(Float[] filtered_data) {
        for (int i = 0; i < filtered_data.length; i++) {
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
    }

    private void handleQuotes() {
        //starting with random quote for text to speech
        Random r = new Random();
        int currquote = r.nextInt(quotes.length);
        if (seconds % 60 == 0) {
            //every 60 seconds a quote is spoken to help motivate the user
            if (currquote > quotes.length) {
                //moving to front of quote array
                currquote = 0;
            }
            //speaking quote, and moving to next quote
            tts.speak(quotes[currquote], TextToSpeech.QUEUE_FLUSH, null);
            currquote++;
        }
    }

    private void createTimer() {
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
                    DecimalFormat df = new DecimalFormat("#.##");
                    if (route.getRouteSize() >= 2) {
                        route.calculateDistance();
                        distance = route.getDistance();
                    }
                    updateViews();
                    //allowing preprocessing to happen at the instance of a 5 second interval
                    if ((seconds % 5) == 0) {
                        stepCounter.setHasProcessed(Boolean.FALSE);
                    }
                    //updating audio
                    handleQuotes();

                }

            }
        });
    }

    private void updateViews() {
        //changing timer text view
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
        timerText.setText(time);
        //changing calorie text view
        calories = Math.round(MET * User.getWeight() * (seconds.floatValue() / 3600));
        calorieText.setText(String.format(Locale.getDefault(), "Calories:\n%d", calories));
        //changing step text view
        stepText.setText(String.format(Locale.getDefault(), "Steps:\n%d", steps));
        distText.setText(String.format("Distance:\n%sm", df.format(distance)));
        //changing pace text view
//        paceText.setText(Html.fromHtml("Pace:\n" + df.format(distance / seconds.floatValue()) + "ms<sup>-1</sup"));

        paceText.setText(Html.fromHtml("Pace:\n" + df.format(pace) + "ms<sup>-1</sup"));
    }

    private void handlePermissions() {
        //HANDLING PERMISSIONS
        //Permissions
        String[] PERMISSIONS = new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (checkPermissions(this, PERMISSIONS) == Boolean.FALSE) {
            //dealt with overriding onRequestPermissionsResult method
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

            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isRunning = true;
                    return;
                } else {
                    Toast.makeText(this, "Permissions Denied\nPlease allow permissions in settings", Toast.LENGTH_SHORT).show();
                    finishRunning();
                }
        }

    }

    private void finishRunning() {
        isRunning = false;
        sensorManager.unregisterListener(listener);

        if (locationManager != null && timeStarted != null) {
            locationManager.removeUpdates(locationListener);
            //exiting the running activity and saving data to database
            //will only save activities which last longer than 60s
            if (seconds > 60) {
                //audio text to speech to congratulate user
                tts.speak(String.format(Locale.getDefault(), "Congratulations, you burnt %d calories and ran %d steps, a total distance of %f. See you next time!", calories, steps, distance),
                        TextToSpeech.QUEUE_FLUSH, null);

                DBhelper helper = new DBhelper(RunningActivity.this);
                if (helper.saveActivity("running", date.toString(), timeStarted, seconds.toString(), calories.toString(), steps.toString(), String.valueOf(distance), null)) {
                    Toast.makeText(RunningActivity.this, "Save successful", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RunningActivity.this, "Save unsuccessful", Toast.LENGTH_SHORT).show();
                }
            } else {
                //saves space and resources on database
                Toast.makeText(RunningActivity.this, "Activity too short, save unsuccessful", Toast.LENGTH_SHORT).show();
            }

        }
        finish();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }
}

