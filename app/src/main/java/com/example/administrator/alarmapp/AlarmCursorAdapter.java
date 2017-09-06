package com.example.administrator.alarmapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.administrator.alarmapp.broadcast.AlarmReceiver;
import com.example.administrator.alarmapp.database.AlarmContract;
import com.example.administrator.alarmapp.database.AlarmContract.AlarmEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.R.attr.x;
import static android.R.attr.y;
import static android.R.id.list;
import static android.R.id.navigationBarBackground;
import static java.security.AccessController.getContext;

/**
 * Created by Administrator on 7/19/2017.
 */

public class AlarmCursorAdapter extends CursorAdapter {

    private final static int RQS_1 = 1;
    public final static String LOG_TAG = AlarmCursorAdapter.class.getName();
    public AlarmCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        View listview =view;
        final int columnID = cursor.getColumnIndex(AlarmEntry._ID);
        int columnTime = cursor.getColumnIndex(AlarmEntry.COLUMN_TIME);
        int columnDays = cursor.getColumnIndex(AlarmEntry.COLUMN_DAYS);
        int columnActive = cursor.getColumnIndex(AlarmEntry.COLUMN_ACTIVE);
        int columnRingtone = cursor.getColumnIndex(AlarmEntry.COLUMN_RINGTONE);
        int columnVibrate = cursor.getColumnIndex(AlarmEntry.COLUMN_VIBRATE);

        /*hien thi thoi gian */
        String display_time = cursor.getString(columnTime);
        Log.v(LOG_TAG,"thoi gian la "+display_time);
        TextView display_time_text_view = (TextView) listview.findViewById(R.id.list_time_text_view);
        display_time_text_view.setText("" +display_time);
        /*hien thi ngay */
        TextView display_days_text_view = (TextView) listview.findViewById(R.id.list_day_text_view);
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

        if (days.size() == 0)  display_days_text_view.setText("Never");
        if (days.size() == 7 ) display_days_text_view.setText("Everyday");
        /*ringtone*/
        String ringtone = cursor.getString(columnRingtone);
        /*vibrate*/
        int vibrate = cursor.getInt(columnVibrate);
        /* xu ly viec kich hoat check box */
        CheckBox checkActived = (CheckBox) listview.findViewById(R.id.list_check_box);
        final ContentValues values = new ContentValues();
        values.put(AlarmEntry.COLUMN_TIME,display_time);
        values.put(AlarmEntry.COLUMN_DAYS,jsonString);
        values.put(AlarmEntry.COLUMN_RINGTONE,ringtone);
        values.put(AlarmEntry.COLUMN_VIBRATE,vibrate);
        int id = cursor.getInt(columnID);
        final Uri uri = ContentUris.withAppendedId(AlarmEntry.CONTENT_URI,id);
        Log.v(LOG_TAG,"uri la adapter la gi " +uri);
        /*xu ly nghe check box*/
        CompoundButton.OnCheckedChangeListener checkedChange = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    values.put(AlarmEntry.COLUMN_ACTIVE,1);
                    int newUpdate = context.getContentResolver().update(uri,values,null,null);
                }else {
                    values.put(AlarmEntry.COLUMN_ACTIVE,0);
                    int newUpdate = context.getContentResolver().update(uri,values,null,null);
                }
            }
        };
        checkActived.setOnCheckedChangeListener(checkedChange);
        /*kich hoat alarm */
        Intent myIntent = new Intent(context,AlarmReceiver.class);
        myIntent.setData(uri);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, x, myIntent,0);

        int active = cursor.getInt(columnActive);
        int x = (id+1)*7;
        int y = x-7;

        if (active!=0) {
            checkActived.setChecked(true);
            Calendar calSet = Calendar.getInstance();
                try {
                    calSet = setAlarm(display_time);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

             alarmManager.set(AlarmManager.RTC_WAKEUP, calSet.getTimeInMillis(),pendingIntent);
            if (days.size()!=0) {
                for (int i = 0; i < days.size(); i++) {

                    int dayRepeat = days.get(i) + 1;
                    Calendar calRepeating = Calendar.getInstance();
                    try {
                        calRepeating = setRepeatAlarm(display_time, dayRepeat);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    PendingIntent pendingRepeat = PendingIntent.getBroadcast(context, y+dayRepeat, myIntent,0);
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calRepeating.getTimeInMillis(),7 * 24 * 60 * 60 * 1000, pendingRepeat);
                }
            }

        }
        else {
            checkActived.setChecked(false);
            alarmManager.cancel(pendingIntent);

            if (days.size()!=0) {
                for (int i = 0; i < days.size(); i++) {
                    int dayRepeat = days.get(i) + 1;
                    PendingIntent pendingRepeat = PendingIntent.getBroadcast(context, y+dayRepeat, myIntent,0);
                    alarmManager.cancel(pendingRepeat);
                }
            }
        }

    }

    private Calendar setRepeatAlarm(String displayTime,int dayOfWeek) throws ParseException {
        /*covert tu string ---> ra thoi gian */
        Calendar calSet = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        calSet.setTime(simpleDateFormat.parse(displayTime));
        int hour_x  = calSet.get(Calendar.HOUR_OF_DAY);
        int minute_x = calSet.get(Calendar.MINUTE);
        /*set thoi gian de thong bao */
        Calendar targetCal = Calendar.getInstance();
        targetCal.set(Calendar.HOUR_OF_DAY,hour_x);
        targetCal.set(Calendar.MINUTE,minute_x);
        targetCal.set(Calendar.DAY_OF_WEEK,dayOfWeek);

        if(targetCal.getTimeInMillis() < System.currentTimeMillis()) {
            targetCal.add(Calendar.DAY_OF_YEAR, 7);
        }

        return targetCal;
    }
    private Calendar setAlarm(String displayTime) throws ParseException {
        /*covert tu string ---> ra thoi gian */
        Calendar calSet = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        calSet.setTime(simpleDateFormat.parse(displayTime));
        int hour_x  = calSet.get(Calendar.HOUR_OF_DAY);
        int minute_x = calSet.get(Calendar.MINUTE);

        /*set thoi gian de thong bao */
        Calendar targetCal = Calendar.getInstance();
        targetCal.set(Calendar.HOUR_OF_DAY,hour_x);
        targetCal.set(Calendar.MINUTE,minute_x);

        Calendar calNow = Calendar.getInstance();
        if (targetCal.compareTo(calNow) <=0) {
            targetCal.add(Calendar.DATE,1);
        }

        return targetCal;
    }
}
