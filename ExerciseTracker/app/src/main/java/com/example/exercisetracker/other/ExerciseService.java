package com.example.exercisetracker.other;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.exercisetracker.R;

public class ExerciseService extends Service {
    private static final int NOTIF_ID = 1;
    private static final String NOTIF_CHANNEL_ID = "Channel_Id";
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

        Notification notification = createNotification1(this);
        startForeground(1,notification);

        //do heavy work on a background thread
        //stopSelf();
        return START_STICKY;

    }
    public static Intent newIntent(Context context) {
        return new Intent(context, ExerciseService.class);
    }
    public Notification createNotification1(Context context) {
        return new NotificationCompat.Builder(context, BaseApp.CHANNEL_1_ID)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.mipmap.appicon)
                .setContentTitle("Test")
                .build();
    }


}
