package com.example.exercisetracker.other;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.exercisetracker.R;
import com.example.exercisetracker.activities.MainActivity;
import com.example.exercisetracker.activities.RunningActivity;

public class ExerciseService extends Service {
    public static final int NOTIF_ID = 1;
    private static final String NOTIF_CHANNEL_ID = "Channel_ID1";
    private static final String TAG =ExerciseService.class.getSimpleName();
    private static ExerciseService instance;
    private NotificationManager manager;
    private NotificationChannel channel;
    private PendingIntent pendingIntent;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags, startId);
        Log.e(TAG,"onStartCommand()");
        createNotificationChannel();

        //Every notification should respond to a tap, usually to open an activity in your app that corresponds to the notification.
        // To do so, you must specify a content intent defined with a PendingIntent object and pass it to setContentIntent().
        pendingIntent = PendingIntent.getActivity(this,0, intent,PendingIntent.FLAG_IMMUTABLE);
        Notification notification = createNotification1(this);
        manager.notify(NOTIF_ID,notification);
        return START_STICKY;

    }
    //<-------handling intents-------->
//    private PendingIntent getIntent(){
//        Intent intent1 = new Intent(this, RunningActivity.class);
//        intent1.setAction(Intent.ACTION_MAIN);
//        return PendingIntent.getActivity(this,0, intent1,PendingIntent.FLAG_IMMUTABLE);
//    }

    private Notification createNotification1(Context context) {
        //setting the notification details
        return new NotificationCompat.Builder(context, BaseApp.CHANNEL_1_ID)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.runningman)
                .setContentTitle("00:00:00")
                .setContentText("Content text")
                .setContentIntent(pendingIntent)
                .build();
    }

    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(NOTIF_CHANNEL_ID,"Foreground Notification", NotificationManager.IMPORTANCE_LOW);
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
}
