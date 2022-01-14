package com.example.exercisetracker.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
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
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.example.exercisetracker.other.User;
import com.example.exercisetracker.other.DBhelper;
import com.example.exercisetracker.repDetection.RepCounter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

public class PushUpActivity extends AppCompatActivity {
    //Text Views
    private TextView timerText, repText, calText, poseIndicatorTV;
    //buttons
    private Button startBtn, finishBtn;
    //pushup custom variables
    private Boolean isTracking;
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
    private int currquote;
    private String[] quotes;
    //notification
    private NotificationManagerCompat notificationManagerCompat;
    private Notification notification;

    private Graphic graphic;
    private Size displaySize;
    private FrameLayout frameLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //visuals
        Objects.requireNonNull(getSupportActionBar()).hide();
        Window w = getWindow();
        w.setNavigationBarColor(getResources().getColor(R.color.main_colour));
        w.setStatusBarColor(getResources().getColor(R.color.main_colour));
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        Rect rectangle = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = rectangle.top;
        setContentView(R.layout.activity_pushup);

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
    }

    private void init() {
        isTracking = Boolean.TRUE;
        seconds = 0;
        calories = 0;
        reps = 0;
        //instantiating all variables
        startBtn = findViewById(R.id.startStopBtn);
        finishBtn = findViewById(R.id.finishBtn);
        timerText = findViewById(R.id.timerText);
        repText = findViewById(R.id.repText);
        calText = findViewById(R.id.calText);
        poseIndicatorTV = findViewById(R.id.PoseIndicator);
        MET = Float.parseFloat(getString(R.string.met_pushup));
        tv = findViewById(R.id.tv);
        frameLayout = findViewById(R.id.framelayout);
        TextView debug = findViewById(R.id.debugTV);
        repcounter = new RepCounter(this, poseIndicatorTV,debug,10f,100f);

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

        //getting display size for graphic
        Display display = getWindowManager().getDefaultDisplay();
        Point temp = new Point();
        display.getSize(temp);
        displaySize = new Size(temp.x, temp.y);
    }

    private void startTracking() {
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

    private void createTimer(){
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
                }
                handleQuotes();

            }
        });
    }

    private void handleQuotes(){
        //starting with random quote for text to speech
        Random r = new Random();
        currquote = r.nextInt(quotes.length);
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
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(PushUpActivity.this), new ImageAnalysis.Analyzer() {
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
                                if (isTracking){
                                //when the pose detector successfully can attach to image
                                //Receiving and processing landmarks from Google's ML kit software
                                List<PoseLandmark> allPoseLandmarks = pose.getAllPoseLandmarks();
                                processLandmarks(allPoseLandmarks);
                                //drawing on the landmarks onto the user's screen
                                graphic.drawGraphic(allPoseLandmarks);
                                }
                                else{
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
//        List<PointF3D> landmarks = new ArrayList<>();
//        for (PoseLandmark landmark : allLandmarks) {
//            landmarks.add(landmark.getPosition3D());
//        }
        if (!allLandmarks.isEmpty() && allLandmarks.get(PoseLandmark.NOSE).getInFrameLikelihood()>0.9f
                && allLandmarks.get(PoseLandmark.LEFT_HIP).getInFrameLikelihood()>0.9f &&
                allLandmarks.get(PoseLandmark.LEFT_KNEE).getInFrameLikelihood()>0.9f) {
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
        switch (requestCode) {
            case 0:
                //checking if all permissions are granted on UI dialog
                boolean granted = true;
                for (int result : grantResults) {
                    if (result == PackageManager.PERMISSION_DENIED) {
                        granted = false;
                    }
                }
                if (granted) {
                    startTracking();
                } else {
                    finishTracking();
                }
                return;
        }
    }

    private void finishTracking() {
        //handles the safe closing of the activity, and presenting any information to the user
        isTracking = false;
        //audio text to speech to congratulate user
        tts.speak(String.format("Congratulations, you burnt %d calories and did %d reps. See you next time!", calories, reps), TextToSpeech.QUEUE_FLUSH, null);

        if (seconds > 60) {
            //saving activity results to database, as long as activity lasted for more than a minute
            DBhelper helper = new DBhelper(PushUpActivity.this);
            if (helper.saveActivity("pushup", date.toString(), timeStarted, seconds.toString(), calories.toString(), null, null, reps.toString())) {
                Toast.makeText(PushUpActivity.this, "Save successful", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(PushUpActivity.this, "Save unsuccessful", Toast.LENGTH_SHORT).show();
            }
        } else {
            //saves space and resources on database
            Toast.makeText(PushUpActivity.this, "Activity Not Saved (Too Short)", Toast.LENGTH_SHORT).show();
        }
        //destroying notification
        notificationManagerCompat.cancel(1);
        this.finish();
    }

    //<---------Graphics---------->
    private class Graphic {
        //composition class (cannot draw graphic without the push up activity

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
            view.setVisibility(View.VISIBLE);
            //inverting x coordinate, as camera is in mirroring position
            view.setX(displaySize.getWidth() - x);
            view.setY(y);
        }

        private void clearGraphic(){
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
                        updateLandmarkGraphic("left_hip",landmark.getPosition().x,landmark.getPosition().y);
                        break;
                    case PoseLandmark.RIGHT_HIP:
                        updateLandmarkGraphic("right_hip",landmark.getPosition().x,landmark.getPosition().y);
                        break;
                    case PoseLandmark.LEFT_KNEE:
                        updateLandmarkGraphic("left_knee",landmark.getPosition().x,landmark.getPosition().y);
                        break;
                    case PoseLandmark.RIGHT_KNEE:
                        updateLandmarkGraphic("right_knee",landmark.getPosition().x,landmark.getPosition().y);
                        break;
                }

            }
        }

    }
}
