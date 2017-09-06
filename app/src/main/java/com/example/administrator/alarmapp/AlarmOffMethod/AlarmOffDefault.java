package com.example.administrator.alarmapp.AlarmOffMethod;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.administrator.alarmapp.R;
import com.example.administrator.alarmapp.broadcast.AlarmReceiver;
import com.example.administrator.alarmapp.database.AlarmContract.AlarmEntry;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.R.attr.id;
import static com.example.administrator.alarmapp.R.raw.nhacchuong;

/**
 * Created by Administrator on 8/18/2017.
 */

public class AlarmOffDefault extends Activity{

    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;
    Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_notification);

        TextView time_text_view = (TextView) findViewById(R.id.textView);
        Button dismiss_button = (Button) findViewById(R.id.dismiss);
        Button snooze_button = (Button) findViewById(R.id.snooze);

        Intent intent = getIntent();
        uri = intent.getData();

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
                finish();
            }
        });

        startAlarm(uri);
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
}
