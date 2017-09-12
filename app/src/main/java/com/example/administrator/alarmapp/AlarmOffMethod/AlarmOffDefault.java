package com.example.administrator.alarmapp.AlarmOffMethod;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.administrator.alarmapp.AlarmCursorAdapter;
import com.example.administrator.alarmapp.R;
import com.example.administrator.alarmapp.broadcast.AlarmReceiver;
import com.example.administrator.alarmapp.database.AlarmContract.AlarmEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static android.R.attr.id;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static com.example.administrator.alarmapp.R.raw.nhacchuong;

/**
 * Created by Administrator on 8/18/2017.
 */

public class AlarmOffDefault extends Activity{

    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;
    private Uri uri;
    private final static String LOG_TAG = AlarmOffDefault.class.getName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.dialog_notification);
        this.setFinishOnTouchOutside(false);

        TextView time_text_view = (TextView) findViewById(R.id.textView);
        Button dismiss_button = (Button) findViewById(R.id.dismiss);
        Button snooze_button = (Button) findViewById(R.id.snooze);

        Intent intent = getIntent();
        uri = intent.getData();
        Log.v(LOG_TAG,"uri la gi " +uri);
        final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        /*hien thi ngay gio */
        Calendar cal  = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        time_text_view.setText(simpleDateFormat.format(cal.getTime()));


        dismiss_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (vibrator != null)
                        vibrator.cancel();
                } catch (Exception e) {

                }
                try {
                    mediaPlayer.stop();
                } catch (Exception e) {

                }
                try {
                    mediaPlayer.release();
                } catch (Exception e) {

                }
                manager.cancelAll();
                finish();
            }
        });
        snooze_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (vibrator != null)
                        vibrator.cancel();
                } catch (Exception e) {

                }
                try {
                    mediaPlayer.stop();
                } catch (Exception e) {

                }
                try {
                    mediaPlayer.release();
                } catch (Exception e) {

                }
                final Intent myIntent = new Intent(getBaseContext(),AlarmReceiver.class);
                myIntent.setData(uri);
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), 0, myIntent,0);
                Calendar calSet = Calendar.getInstance();
                calSet.add(Calendar.MINUTE,5);
                alarmManager.set(AlarmManager.RTC_WAKEUP, calSet.getTimeInMillis(),pendingIntent);
                manager.cancelAll();

                finish();

            }

        });

        startAlarm(uri);
        cancelAlarm(uri);
    }



    private void startAlarm(Uri uri) {

        Cursor cursor = queryDb(uri);
        if (cursor !=null && cursor.moveToFirst()) {
            int columnId = cursor.getColumnIndex(AlarmEntry._ID);
            int columnRingtone = cursor.getColumnIndex(AlarmEntry.COLUMN_RINGTONE);
            int columnVibrate = cursor.getColumnIndex(AlarmEntry.COLUMN_VIBRATE);
            int id = cursor.getInt(columnId);
            String ringtone = cursor.getString(columnRingtone);
            int vibrate = cursor.getInt(columnVibrate);
            cursor.close();
            if (ringtone != null) {
                if (vibrate != 0) {
                    vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    long[] pattern = {1000, 200, 200, 200};
                    vibrator.vibrate(pattern, 0);
                }
                if (ringtone.equals("default")) {
                    mediaPlayer = MediaPlayer.create(this,R.raw.nhacchuong);
                    mediaPlayer.start();
                }
                else {
                    Uri uriRingtone = Uri.parse(ringtone);
                    mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setVolume(1.0f, 1.0f);
                        mediaPlayer.setDataSource(this, uriRingtone);
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                        mediaPlayer.setLooping(true);
                        mediaPlayer.prepare();
                        mediaPlayer.start();

                    } catch (IOException e) {
                        mediaPlayer.release();
                    }
                }
            }
        }

    }

    private void cancelAlarm(Uri uri) {
        Cursor cursor =queryDb(uri);

        if (cursor!=null && cursor.moveToFirst()) {
            int columnDays = cursor.getColumnIndex(AlarmEntry.COLUMN_DAYS);
            String jsonString = cursor.getString(columnDays);
            JSONObject json = null;
            try {
                json = new JSONObject(jsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONArray items = json.optJSONArray("days");

            final ArrayList<Integer> days = new ArrayList();

            for (int i=0; i<items.length(); i++) {
                try {
                    days.add(items.getInt(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (days.size()==0) {
                ContentValues values = new ContentValues();
                values.put(AlarmEntry.COLUMN_ACTIVE,0);
                int newUpdate = getContentResolver().update(uri,values,null,null);
            }
        }

    }



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
    protected void onPause() {
        super.onPause();
        StaticWakeLock.lockOff(this);
    }

    @Override
    protected void onDestroy() {
        try {
            if (vibrator != null)
                vibrator.cancel();
        } catch (Exception e) {

        }
        try {
            mediaPlayer.stop();
        } catch (Exception e) {

        }
        try {
            mediaPlayer.release();
        } catch (Exception e) {

        }
        super.onDestroy();
    }
}
