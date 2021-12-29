package com.example.exercisetracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Size;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
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
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class PushUpActivity extends AppCompatActivity{


    //Text Views
    private TextView timerText,repText,calText;
    //buttons
    private Button startBtn, finishBtn;

    //pushup custom variables
    private Boolean isTracking;
    private Integer seconds;
    private Date timeStarted;
    private Float MET;

    private PoseDetector poseDetector;
    private Preview preview;
    private PreviewView tv;
    private CameraSelector cameraSelector;

    private int statusBarHeight;

    private Graphic graphic;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //visuals
        Objects.requireNonNull(getSupportActionBar()).hide();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        }
        Window w = getWindow();
        w.setNavigationBarColor(getResources().getColor(R.color.main_colour));
        w.setStatusBarColor(getResources().getColor(R.color.main_colour));
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        Rect rectangle = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
        statusBarHeight = rectangle.top;

        setContentView(R.layout.activity_pushup);
        //instantiating all variables
        startBtn = findViewById(R.id.startStopBtn);
        finishBtn = findViewById(R.id.finishBtn);
        timerText = findViewById(R.id.timerText);
        repText = findViewById(R.id.repText);
        calText = findViewById(R.id.calText);
        MET = Float.parseFloat(getString(R.string.met_pushup));
        tv = findViewById(R.id.tv);

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

        if (checkPermissions(this, PERMISSIONS) == Boolean.FALSE){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //dealt with overriding onRequestPermissionsResult method
                requestPermissions(PERMISSIONS,0);
            }
        }
        else {
            startTracking();
        }

    }
    private void startTracking(){
        isTracking = Boolean.TRUE;
        seconds = 0;
        timeStarted = Calendar.getInstance().getTime();
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this,1000);
                if (isTracking == Boolean.TRUE){
                    seconds++;
                    //updating timer view
                    int hours = seconds / 3600;
                    int minutes = (seconds % 3600) / 60;
                    int secs = seconds % 60;
                    String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
                    timerText.setText(time);


                }
            }
        });
        graphic = new Graphic();
        handleCamera();
        //MLK tracking, instantiating pose detector
        PoseDetectorOptions options = new PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build();
        poseDetector = PoseDetection.getClient(options);

    }


    private void handleCamera(){
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        //getting display size (dependent on device)
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        Size imageSize = new Size(size.x,680);
//        graphic.setScaleFactor(size,imageSize);
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        //instantiating ImageAnalysis, with user's phone display dimensions
                        .setTargetResolution(new Size(size.x, size.y))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(PushUpActivity.this), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                @SuppressLint("UnsafeOptInUsageError") Image image = imageProxy.getImage();
                if (image != null){
                    //receiving the input image from camera
                    InputImage inputimage = InputImage.fromMediaImage(image,rotationDegrees);
                    Task<Pose> result = poseDetector.process(inputimage).addOnSuccessListener(new OnSuccessListener<Pose>() {
                        @Override
                        public void onSuccess(@NonNull Pose pose) {
                            //when the pose detector successfully can attach to image
                            System.out.println("Successful Pose Detection");
                            List<PoseLandmark> allPoseLandmarks = pose.getAllPoseLandmarks();
                            processLandmarks(allPoseLandmarks);

                            graphic.drawGraphic(allPoseLandmarks,size);

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

        cameraProviderFuture.addListener(() ->{
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
                        provider.bindToLifecycle((LifecycleOwner) this,cameraSelector,preview, imageAnalysis);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
        },ContextCompat.getMainExecutor(this));
    }

    private void processLandmarks(List<PoseLandmark> allLandmarks){
        for (PoseLandmark landmark: allLandmarks){
        }
    }


    private class Graphic{
        //composition class (cannot draw graphic without the push up activity
        private final View noseView;
        private final View reyeView;
        private final View leyeView;
        private final View lelbowView;
        private final View relbowView;
        private float scalex;
        private float scaley;
        private int yoffset;
        private int xoffset;
        //n pixels to offset in order to fit scaled up image on view

        private Graphic(){
            noseView = findViewById(R.id.nose);
            reyeView = findViewById(R.id.leftEye);
            leyeView = findViewById(R.id.rightEye);
            lelbowView = findViewById(R.id.lelbow);
            relbowView = findViewById(R.id.relbow);
            yoffset = 170;
            xoffset = -30;
        }

//        private void setScaleFactor(Point displaySize, Size imageSize){
//            System.out.println(String.format("Display size: %d, %d",displaySize.x,displaySize.y));
//            scalex = (float) (displaySize.x / imageSize.getWidth());
//            scaley = (float) (displaySize.y / imageSize.getHeight());
//            int[] arr = new int[2];
//            tv.getLocationInWindow(arr);
//            yoffset = arr[1];
//        }

        private void drawGraphic(List<PoseLandmark> allLandmarks, Point displaySize){
            for (PoseLandmark landmark: allLandmarks){
                switch(landmark.getLandmarkType()){
                    case PoseLandmark.LEFT_SHOULDER:
                        //inverting x coordinate, as camera is in mirroring position
                        noseView.setX(displaySize.x-landmark.getPosition().x +xoffset);
                        noseView.setY(landmark.getPosition().y+ yoffset);
                        System.out.println("x: "+ landmark.getPosition().x + " y: "+ landmark.getPosition().y);
                        System.out.println("y offset:" + yoffset);
                        break;
                    case PoseLandmark.RIGHT_SHOULDER:
                        //inverting x coordinate, as camera is in mirroring position
                        reyeView.setX(displaySize.x-landmark.getPosition().x+xoffset);
                        reyeView.setY(landmark.getPosition().y+yoffset);
                        break;
                    case PoseLandmark.NOSE:
                        //inverting x coordinate, as camera is in mirroring position
                        leyeView.setX(displaySize.x-landmark.getPosition().x+xoffset);
                        leyeView.setY(landmark.getPosition().y+yoffset);
                        break;
                    case PoseLandmark.LEFT_ELBOW:
                        //inverting x coordinate, as camera is in mirroring position
                        lelbowView.setX(displaySize.x-landmark.getPosition().x+xoffset);
                        lelbowView.setY(landmark.getPosition().y+yoffset);
                        break;
                    case PoseLandmark.RIGHT_ELBOW:
                        //inverting x coordinate, as camera is in mirroring position
                        relbowView.setX(displaySize.x-landmark.getPosition().x);
                        relbowView.setY(landmark.getPosition().y+yoffset);
                        break;
                }

            }
        }

    }
    // --------PERMISSIONS------------
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
        isTracking = Boolean.FALSE;
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 0:
                //checking if all permissions are granted on UI dialog
                boolean granted = true;
                for (int result : grantResults) {
                    if (result == PackageManager.PERMISSION_DENIED) {
                        granted = false;
                    }
                }
                if (granted){
                    startTracking();
                }
                else{
                    finishTracking();
                }
                return;
        }
    }


    private void finishTracking(){
        isTracking = false;
        this.finish();

    }

}
