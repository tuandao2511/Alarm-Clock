package com.example.administrator.alarmapp.broadcast;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.IntDef;

import com.example.administrator.alarmapp.AlarmOffMethod.AlarmOffDefault;
import com.example.administrator.alarmapp.AlarmOffMethod.StaticWakeLock;
import com.example.administrator.alarmapp.R;
import com.example.administrator.alarmapp.database.AlarmContract.AlarmEntry;

import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by Administrator on 8/4/2017.
 */

public class AlarmService extends Service {

    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;
    private static final String LOG_TAG = AlarmService.class.getName();
    NotificationManager manager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Uri uri = intent.getData();
        Log.v(LOG_TAG,"uri " +uri);

        Cursor cursor = queryDb(uri);
        if (cursor !=null && cursor.moveToFirst()){
            int columnId = cursor.getColumnIndex(AlarmEntry._ID);
            int id = cursor.getInt(columnId);
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Intent notifIntent = new Intent(this, AlarmOffDefault.class);
            notifIntent.setData(uri);
            notifIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setSmallIcon(R.drawable.ic_notication);
            builder.setColor(ContextCompat.getColor(this, R.color.colorAccent));
            builder.setContentTitle(getString(R.string.wake_up));
            builder.setAutoCancel(true);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                stackBuilder.addParentStack(AlarmOffDefault.class);
                stackBuilder.addNextIntent(notifIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                builder.setContentIntent(resultPendingIntent);
                builder.setFullScreenIntent(resultPendingIntent,true);
            }
            manager.notify(id, builder.build());

        }

        return START_NOT_STICKY;
    }

//    private void DefaultNotification(int id) {
//        Log.v(LOG_TAG,"da implement o day");
//        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        Intent notifIntent = new Intent(this, AlarmOffDefault.class);
//        notifIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//        builder.setSmallIcon(R.mipmap.ic_launcher);
//        builder.setColor(ContextCompat.getColor(this, R.color.colorPrimary));
//        builder.setContentTitle(getString(R.string.wake_up));
//
//
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
//            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//            stackBuilder.addParentStack(AlarmOffDefault.class);
//            stackBuilder.addNextIntent(notifIntent);
//            PendingIntent resultPendingIntent =
//                    stackBuilder.getPendingIntent(
//                            0,
//                            PendingIntent.FLAG_UPDATE_CURRENT
//                    );
//            builder.setContentIntent(resultPendingIntent);
//            builder.setFullScreenIntent(resultPendingIntent,true);
//        }
//        manager.notify(id, builder.build());
//
//    }

    private Cursor queryDb(Uri uri) {
        String [] projection = {
                AlarmEntry._ID,
                AlarmEntry.COLUMN_TIME,
                AlarmEntry.COLUMN_DAYS,
                AlarmEntry.COLUMN_RINGTONE,
                AlarmEntry.COLUMN_VIBRATE,
                AlarmEntry.COLUMN_ACTIVE,
                AlarmEntry.COLUMN_ALARM_OFF
        };
        return getContentResolver().query(uri,projection,null,null,null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
