package com.example.exercisetracker.other;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.exercisetracker.R;
import com.example.exercisetracker.activities.RunningActivity;
import com.example.exercisetracker.stepcounting.StepCounter;

import java.sql.Date;
import java.text.DecimalFormat;

public class ExerciseService extends Service implements SensorEventListener {
    public static final int NOTIF_ID = 1;
    private static final String NOTIF_CHANNEL_ID = "Channel_ID1";
    private static final String TAG = ExerciseService.class.getSimpleName();
    private static ExerciseService instance;
    // Binder given to clients
    private final IBinder binder = new LocalBinder();
    private NotificationManager manager;
    private NotificationChannel channel;
    private PendingIntent pendingIntent;
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        seconds++;
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            //getting values from accelerometer
            stepCounter.addEntry(0, event.values[0], event.values[1], event.values[2]);
            pace = stepCounter.calculatePace(event.values[0], event.values[1], event.values[2]);
        } else if (sensor.getType() == Sensor.TYPE_GRAVITY & isRunning) {
            //getting values from gravimeter
            stepCounter.addEntry(1, event.values[0], event.values[1], event.values[2]);
        }
        //PROCESSING DATA
        if ((seconds % 5) == 0 && (!stepCounter.isEmpty())) {
            stepCounter.countSteps();
            steps = stepCounter.getSteps();
            createNotification1(this, steps.toString(), "e");
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.e(TAG, "onStartCommand()");
        createNotificationChannel();
        seconds = 0;
        StepCounter stepCounter = new StepCounter(this, 2, 0.5f, -10f, 10f, new DecimalFormat("#.##"));
        //Every notification should respond to a tap, usually to open an activity in your app that corresponds to the notification.
        // To do so, you must specify a content intent defined with a PendingIntent object and pass it to setContentIntent().
        Intent notificationIntent = new Intent(this, RunningActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = createNotification1(this, "00:00:00", "text");
        startForeground(1, notification);
        return START_STICKY;

    }

    private Notification createNotification1(Context context, String title, String text) {
        //setting the notification details
        return new NotificationCompat.Builder(context, BaseApp.CHANNEL_1_ID)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.running)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .build();
    }
    //<-------handling intents-------->
//    private PendingIntent getIntent(){
//        Intent intent1 = new Intent(this, RunningActivity.class);
//        intent1.setAction(Intent.ACTION_MAIN);
//        return PendingIntent.getActivity(this,0, intent1,PendingIntent.FLAG_IMMUTABLE);
//    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(NOTIF_CHANNEL_ID, "Foreground Notification", NotificationManager.IMPORTANCE_LOW);
            manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        manager.cancelAll();
        stopSelf();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public ExerciseService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ExerciseService.this;
        }
    }
}
