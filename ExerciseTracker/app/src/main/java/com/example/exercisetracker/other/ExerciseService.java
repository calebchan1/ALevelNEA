package com.example.exercisetracker.other;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.exercisetracker.R;
import com.example.exercisetracker.activities.MainActivity;
import com.example.exercisetracker.activities.RunningActivity;

public class ExerciseService extends Service {
    private static final int NOTIF_ID = 1;
    private static final String NOTIF_CHANNEL_ID = "Channel_ID1";
    private static final String TAG =ExerciseService.class.getSimpleName();
    private static ExerciseService instance;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    static public ExerciseService getInstance() {
        if (instance == null) {
            instance = new ExerciseService();
        }
        return instance;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags, startId);
        Log.e(TAG,"onStartCommand()");
        createNotificationchannel();
        Intent intent1 = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0, intent1,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = createNotification1(this);
        startForeground(1,notification);
        return START_STICKY;

    }
    //<-------handling intents-------->
    private void getIntent(){

    }

    public static Intent newIntent(Context context) {
        return new Intent(context, ExerciseService.class);
    }



    private Notification createNotification1(Context context) {
        //setting the notification details
        return new NotificationCompat.Builder(context, BaseApp.CHANNEL_1_ID)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.runningman)
                .setContentTitle("Exercise Tracker")
                .setContentTitle("00:00:00")
                .build();
    }

    private void createNotificationchannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIF_CHANNEL_ID,"Foreground Notification", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        stopSelf();
        super.onDestroy();
    }
}
