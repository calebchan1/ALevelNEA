package com.example.exercisetracker.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Size;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.example.exercisetracker.R;
import com.example.exercisetracker.other.DBhelper;
import com.example.exercisetracker.other.User;
import com.example.exercisetracker.repDetection.RepCounter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class SquatsActivity extends AppCompatActivity  {
    //Text Views
    private TextView timerText;
    private TextView repText;
    private TextView calText;
    //buttons
    private Button startBtn, finishBtn, helpBtn;
    //pushup custom variables
    private Boolean isTracking, isAudio;
    private java.sql.Date date;
    private Integer seconds;
    private String timeStarted;
    private Float MET;
    private Integer reps;
    private Integer calories;
    private PoseDetector poseDetector;
    private Preview preview;
    private PreviewView tv;
    private CameraSelector cameraSelector;
    private RepCounter repcounter;

    //audio
    private TextToSpeech tts;
    private String[] quotes;

    //notification
    private NotificationManagerCompat notificationManagerCompat;

    private Graphic graphic;
    private Size displaySize;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //visuals
        Objects.requireNonNull(getSupportActionBar()).hide();
        Window w = getWindow();
        w.setNavigationBarColor(getResources().getColor(R.color.main_colour));
        w.setStatusBarColor(getResources().getColor(R.color.main_colour));
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        Rect rectangle = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
        setContentView(R.layout.activity_squats);
        init();

        //handling button clicks
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when start stop button is clicked
                if (isTracking) {
                    startBtn.setText("Resume");
                    isTracking = false;

                } else {
                    startBtn.setText("Pause");
                    isTracking = true;
                }
            }
        });
        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when finish button is clicked
                finishTracking();
            }
        });
        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when help button is clicked, show dialog to user explaining how to user
                //how app can track squats and what user should do
                String title = "How does this work?";
                String message = "This app uses Google's machine learning kit to track your movements.\n" +
                        "Place your device on a flat stable surface where your body is in full frame of the camera.\n" +
                        "Avoid places where the camera is at a steep incline.\n" +
                        "As you squat, the app will detect your reps and calories!";
                new MaterialAlertDialogBuilder(SquatsActivity.this)
                        .setTitle(title)
                        .setMessage(message).setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();

            }
        });
        //when audio button is clicked
        ImageButton audioBtn = findViewById(R.id.audioBtn);
        audioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAudio) {
                    //user requesting audio is switched off
                    audioBtn.setImageResource(R.drawable.noaudio);
                    isAudio = false;
                } else {
                    //user requesting audio switched on
                    audioBtn.setImageResource(R.drawable.audio);
                    isAudio = true;
                }
            }
        });
        //permissions
        String[] PERMISSIONS = new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        if (checkPermissions(this, PERMISSIONS) == Boolean.FALSE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //dealt with overriding onRequestPermissionsResult method
                requestPermissions(PERMISSIONS, 0);
            }
        } else {
            startTracking();
        }
        super.onCreate(savedInstanceState);
    }
    private void init(){
        //instantiating all variables
        //getting display size for graphic
        Display display = getWindowManager().getDefaultDisplay();
        Point temp = new Point();
        display.getSize(temp);
        displaySize = new Size(temp.x, temp.y);

        isAudio = true;
        isTracking = true;
        seconds = 0;
        calories = 0;
        reps = 0;

        startBtn = findViewById(R.id.startStopBtn);
        finishBtn = findViewById(R.id.finishBtn);
        helpBtn = findViewById(R.id.helpBtn);
        timerText = findViewById(R.id.timerText);
        repText = findViewById(R.id.repText);
        calText = findViewById(R.id.calText);
        TextView poseIndicatorTV = findViewById(R.id.PoseIndicator);

        //getting met from string values
        MET = Float.parseFloat(getString(R.string.met_squats));
        tv = findViewById(R.id.tv);
        TextView debug = findViewById(R.id.debugTV);
        //min distance is by a fifth of the screen height
        //uncertainty is 1/10 of the screen height
        repcounter = new RepCounter(this,1, poseIndicatorTV, debug, displaySize.getHeight() / 9f);

        //getting current date and time
        long millis = System.currentTimeMillis();
        Timestamp timestamp = new Timestamp(millis);
        timeStarted = timestamp.toString().substring(11, 16);
        date = new java.sql.Date(millis);

        //quotes from resources
        Resources res = getResources();
        quotes = res.getStringArray(R.array.quotes);

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
    }

    private void startTracking(){
        //NOTIFICATION MANAGER
        notificationManagerCompat = NotificationManagerCompat.from(this);

        createTimer();
        handleCamera();
        //MLK tracking, instantiating pose detector
        PoseDetectorOptions options = new PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build();
        poseDetector = PoseDetection.getClient(options);
    }

    private void createTimer() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 1000);
                //updating live notification every second
                //sendOnChannel1();
                if (isTracking == Boolean.TRUE) {
                    seconds++;
                    //updating timer view
                    int hours = seconds / 3600;
                    int minutes = (seconds % 3600) / 60;
                    int secs = seconds % 60;
                    String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
                    calories = Math.round(MET * User.getWeight() * (seconds.floatValue() / 3600));
                    reps = repcounter.getReps();
                    //updating text views
                    timerText.setText(time);
                    calText.setText("Calories:\n" + calories.toString());
                    repText.setText("Reps:\n" + reps.toString());
                    handleQuotes();
                }

            }
        });
    }

    private void handleQuotes() {
        if (isAudio) {
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
    }

    private void handleCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        //getting display size (dependent on device)


        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        //instantiating ImageAnalysis, with user's phone display dimensions
//                        .setTargetResolution()
                        .setTargetResolution(displaySize)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
        //setting the configuration for the image analysis
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(SquatsActivity.this), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {

                int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                @SuppressLint("UnsafeOptInUsageError") Image image = imageProxy.getImage();
                if (image != null) {
                    //receiving the input image from camera
                    InputImage inputimage = InputImage.fromMediaImage(image, rotationDegrees);
                    Task<Pose> result = poseDetector.process(inputimage).addOnSuccessListener(new OnSuccessListener<Pose>() {
                        @Override
                        public void onSuccess(@NonNull Pose pose) {
                            if (isTracking) {
                                //when the pose detector successfully can attach to image
                                //Receiving and processing landmarks from Google's ML kit software
                                List<PoseLandmark> allPoseLandmarks = pose.getAllPoseLandmarks();
                                processLandmarks(allPoseLandmarks);
                                //drawing on the landmarks onto the user's screen
                                graphic.drawGraphic(allPoseLandmarks);
                            } else {
                                graphic.clearGraphic();
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //when the pose detector cannot attach to image
                            System.out.println("Failed Pose Detection");
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Pose>() {
                        @Override
                        public void onComplete(@NonNull Task<Pose> task) {
                            //making sure to close the instance of the image to allow the next image to be processed
                            imageProxy.close();
                        }
                    });
                }
            }
        });

        //attaching the image analysis object to the camera
        cameraProviderFuture.addListener(() -> {
            try {
                //configuring camera to preview.
                ProcessCameraProvider provider = cameraProviderFuture.get();
                preview = new Preview.Builder().build();
                preview.setSurfaceProvider(tv.getSurfaceProvider());
                cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();
                try {
                    //binding the camera, preview and analyser together
                    provider.unbindAll();
                    provider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));

        graphic = new Graphic(displaySize);

    }

    private void processLandmarks(List<PoseLandmark> allLandmarks) {
        //method to deal with analyzing the landmarks in a particular instance, provided by ML Kit
        if (!allLandmarks.isEmpty() && allLandmarks.get(PoseLandmark.LEFT_HIP).getInFrameLikelihood() > 0.5f &&
                allLandmarks.get(PoseLandmark.LEFT_KNEE).getInFrameLikelihood() > 0.5f) {
            repcounter.addEntry(allLandmarks);
        }
    }

    // --------PERMISSIONS------------
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
        isTracking = Boolean.FALSE;
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {//checking if all permissions are granted on UI dialog
            boolean granted = true;
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    granted = false;
                }
            }
            if (granted) {
                isTracking = true;
                startTracking();
            } else {
                Toast.makeText(this, "Permissions Denied\nPlease allow permissions in settings", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

    private void finishTracking() {
        //handles the safe closing of the activity, and presenting any information to the user
        isTracking = false;
        if (seconds > 60) {
            if (isAudio){
                //audio text to speech to congratulate user
                tts.speak(String.format(Locale.getDefault(), "Congratulations, you burnt %d calories and did %d reps. See you next time!", calories, reps),
                        TextToSpeech.QUEUE_FLUSH, null);
            }
            //saving activity results to database, as long as activity lasted for more than a minute
            DBhelper helper = new DBhelper(SquatsActivity.this);
            if (helper.saveActivity("squats", date.toString(), timeStarted, seconds.toString(), calories.toString(), null, null, reps.toString())) {
                Toast.makeText(SquatsActivity.this, "Save successful", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SquatsActivity.this, "Save unsuccessful", Toast.LENGTH_SHORT).show();
            }
        } else {
            //saves space and resources on database
            Toast.makeText(SquatsActivity.this, "Activity Not Saved (Too Short)", Toast.LENGTH_SHORT).show();
        }
        this.finish();
    }

    //composition class (cannot draw graphic without the push up activity
    private class Graphic {
        //hash map, associating landmark name to graphic View
        private final Map<String, View> graphicViewsMap;
        private final Size displaySize;
        private float scalex;
        private float scaley;
        private int yoffset;
        private int xoffset;
        //n pixels to offset in order to fit scaled up image on view

        private Graphic(Size displaySize) {
            //HASH MAP to store all views corresponding to landmarks
            graphicViewsMap = new HashMap<String, View>();
            graphicViewsMap.put("nose", (View) findViewById(R.id.nose));
            graphicViewsMap.put("left_shoulder", (View) findViewById(R.id.left_shoulder));
            graphicViewsMap.put("right_shoulder", (View) findViewById(R.id.right_shoulder));
            graphicViewsMap.put("left_elbow", (View) findViewById(R.id.lelbow));
            graphicViewsMap.put("right_elbow", (View) findViewById(R.id.relbow));
            graphicViewsMap.put("left_hip", (View) findViewById(R.id.left_hip));
            graphicViewsMap.put("right_hip", (View) findViewById(R.id.right_hip));
            graphicViewsMap.put("left_knee", (View) findViewById(R.id.left_knee));
            graphicViewsMap.put("right_knee", (View) findViewById(R.id.right_knee));
            this.displaySize = displaySize;
        }

//        private int gcd(int p, int q){
//            //euclid's algorithm to find smallest possible ratio
//            if (q == 0) return p;
//            else return gcd(q, p % q);
//        }
//        private int ratio(int a, int b) {
//            final int gcd = gcd(a,b);
//            return (b/gcd);
//        }
//
//        private void setScaleFactor(Point displaySize, Size imageSize){
//            Toast.makeText(PushUpActivity.this, String.format("Ratio of y: %d",ratio(displaySize.x,displaySize.y)), Toast.LENGTH_SHORT).show();
//            Toast.makeText(PushUpActivity.this, String.format("Display size: %d, %d",displaySize.x,displaySize.y), Toast.LENGTH_SHORT).show();
//            scalex = 2.25f;
//            scaley = 2.25f;
////            int[] arr = new int[2];
////            tv.getLocationInWindow(arr);
////            yoffset = arr[1];
//        }

        private void updateLandmarkGraphic(String name, Float x, Float y) {
            View view = graphicViewsMap.get(name);
            if (view != null) {
                view.setVisibility(View.VISIBLE);
                //inverting x coordinate, as camera is in mirroring position
                view.setX(displaySize.getWidth() - x);
                view.setY(y);
            }

        }

        private void clearGraphic() {
            for (View view : graphicViewsMap.values()) {
                view.setVisibility(View.GONE);
            }
        }

        private void drawGraphic(List<PoseLandmark> allLandmarks) {
            if (allLandmarks.isEmpty()) {
                //if no landmarks are detected, remove points from graphic
                clearGraphic();

            }
            //drawing points on the preview, corresponding to where ML Kit has detected the positions
            //of the body parts
            for (PoseLandmark landmark : allLandmarks) {
                switch (landmark.getLandmarkType()) {
                    case PoseLandmark.LEFT_SHOULDER:
                        updateLandmarkGraphic("left_shoulder", landmark.getPosition().x, landmark.getPosition().y);
                        break;
                    case PoseLandmark.RIGHT_SHOULDER:
                        updateLandmarkGraphic("right_shoulder", landmark.getPosition().x, landmark.getPosition().y);
                        break;
                    case PoseLandmark.NOSE:
                        updateLandmarkGraphic("nose", landmark.getPosition().x, landmark.getPosition().y);
                        break;
                    case PoseLandmark.LEFT_ELBOW:
                        updateLandmarkGraphic("left_elbow", landmark.getPosition().x, landmark.getPosition().y);
                        break;
                    case PoseLandmark.RIGHT_ELBOW:
                        updateLandmarkGraphic("right_elbow", landmark.getPosition().x, landmark.getPosition().y);
                        break;
                    case PoseLandmark.LEFT_HIP:
                        updateLandmarkGraphic("left_hip", landmark.getPosition().x, landmark.getPosition().y);
                        break;
                    case PoseLandmark.RIGHT_HIP:
                        updateLandmarkGraphic("right_hip", landmark.getPosition().x, landmark.getPosition().y);
                        break;
                    case PoseLandmark.LEFT_KNEE:
                        updateLandmarkGraphic("left_knee", landmark.getPosition().x, landmark.getPosition().y);
                        break;
                    case PoseLandmark.RIGHT_KNEE:
                        updateLandmarkGraphic("right_knee", landmark.getPosition().x, landmark.getPosition().y);
                        break;
                }

            }
        }

    }
}
