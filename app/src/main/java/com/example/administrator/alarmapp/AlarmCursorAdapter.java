package com.example.administrator.alarmapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

import static android.R.attr.type;
import static android.R.attr.x;
import static android.R.attr.y;
import static android.R.id.list;
import static android.R.id.navigationBarBackground;
import static com.example.administrator.alarmapp.R.id.textView;
import static java.security.AccessController.getContext;

/**
 * Created by Administrator on 7/19/2017.
 */

public class AlarmCursorAdapter extends CursorAdapter {

    private final static int RQS_1 = 1;
    private final static String LOG_TAG = AlarmCursorAdapter.class.getName();
    private final static int MAX_LAYOUT_COUNT = 2;
    private final static int  LIST_ITEMT_1 = 1;
//    private ArrayList<Integer> days ;
    private int type;


    public AlarmCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);

    }

    private ArrayList<Integer> RetriveDay(Cursor cursor) {
            int columnDays = cursor.getColumnIndex(AlarmEntry.COLUMN_DAYS);
            String jsonString = cursor.getString(columnDays);
            JSONObject json = null;
            try {
                json = new JSONObject(jsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONArray items = json.optJSONArray("days");

            ArrayList<Integer> day = new ArrayList();

            for (int i = 0; i < items.length(); i++) {
                try {
                    day.add(items.getInt(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        return day;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
//        days = RetriveDay(cursor);
//        Log.v(LOG_TAG,"size laf gi " +days.size());
        int position = cursor.getPosition();
        type = getItemViewType(position);
        if (type == 0){
            return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
        }
        else{
            return LayoutInflater.from(context).inflate(R.layout.list_item2,parent,false);
        }
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        View listview = view;
        final int columnID = cursor.getColumnIndex(AlarmEntry._ID);
        int columnTime = cursor.getColumnIndex(AlarmEntry.COLUMN_TIME);
        int columnDays = cursor.getColumnIndex(AlarmEntry.COLUMN_DAYS);
        int columnActive = cursor.getColumnIndex(AlarmEntry.COLUMN_ACTIVE);
        int columnRingtone = cursor.getColumnIndex(AlarmEntry.COLUMN_RINGTONE);
        int columnVibrate = cursor.getColumnIndex(AlarmEntry.COLUMN_VIBRATE);
        /*hien thi thoi gian */
        final String display_time = cursor.getString(columnTime);
        Log.v(LOG_TAG,"thoi gian la "+display_time);
        TextView display_time_text_view = (TextView) listview.findViewById(R.id.list_time_text_view);
        display_time_text_view.setText("" +display_time);
        display_time_text_view.setTypeface(display_time_text_view.getTypeface(), Typeface.BOLD);        // for Bold only

        String jsonString = cursor.getString(columnDays);
        final ArrayList<Integer> days = new ArrayList();
        /*hien thi ngay */
        if (type == 0) {
            TextView display_days_text_view = (TextView) listview.findViewById(R.id.list_day_text_view);
            JSONObject json = null;
            try {
                json = new JSONObject(jsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONArray items = json.optJSONArray("days");


            for (int i = 0; i < items.length(); i++) {
                try {
                    days.add(items.getInt(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            StringBuilder item = cutDays(context,days);
            Log.v(LOG_TAG,"item la gi " +item);
            display_days_text_view.setText(""+item);

        }
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
        final int id = cursor.getInt(columnID);
        final Uri uri = ContentUris.withAppendedId(AlarmEntry.CONTENT_URI,id);
        Log.v(LOG_TAG,"uri la adapter la gi " +uri);


        /*xu ly nghe check box*/
        CompoundButton.OnCheckedChangeListener checkedChange = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent myIntent = new Intent(context,AlarmReceiver.class);
                myIntent.setData(uri);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                int x = id*7;
                int y = x-7;
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, x, myIntent,0);

                if (isChecked) {
                    values.put(AlarmEntry.COLUMN_ACTIVE,1);
                    int newUpdate = context.getContentResolver().update(uri,values,null,null);
                    Calendar calSet = Calendar.getInstance();
                    try {
                        calSet = setAlarm(display_time);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calSet.getTimeInMillis(),pendingIntent);

                    if (type==0) {
                        for (int i = 0; i < days.size(); i++) {

                            int dayRepeat = days.get(i) + 1;
                            Log.v(LOG_TAG,"day repeat la " +dayRepeat);
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
                }else {
                    values.put(AlarmEntry.COLUMN_ACTIVE,0);
                    int newUpdate = context.getContentResolver().update(uri,values,null,null);

                    alarmManager.cancel(pendingIntent);

                    if (type == 0) {
                        for (int i = 0; i < days.size(); i++) {
                            int dayRepeat = days.get(i) + 1;
                            PendingIntent pendingRepeat = PendingIntent.getBroadcast(context, y+dayRepeat, myIntent,0);
                            alarmManager.cancel(pendingRepeat);
                        }
                    }
                }
            }
        };
        checkActived.setOnCheckedChangeListener(checkedChange);

        /*tich checkbox */
        int active = cursor.getInt(columnActive);

        if (active!=0) {
            checkActived.setChecked(true);
        }
        else {
            checkActived.setChecked(false);
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

    @Override
    public int getViewTypeCount() {
        return MAX_LAYOUT_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = (Cursor) getItem(position);
        ArrayList<Integer> days = RetriveDay(cursor);

        return (days.size() == 0) ? 1 : 0;
    }

    /*cat gon ngay String sap xep theo thu tu */
    private StringBuilder cutDays(Context context, ArrayList<Integer> selectedItems) {
        String [] items =   context.getResources().getStringArray(R.array.array_days_optical);
        StringBuilder item = new StringBuilder();
        if (selectedItems.size() == 7) item.append(context.getString(R.string.everday_repeat));
        else if (selectedItems.size()==0) item.append(context.getString(R.string.never_repeat));
        else {
            for (int i = 0; i < selectedItems.size(); i++) {
                Log.v(LOG_TAG,"selectedItem la gi " +selectedItems.get(i).toString());
                item.append(items[selectedItems.get(i)].toString().substring(0, 3));
                if (i != selectedItems.size() - 1) {
                    item.append(", ");
                }
            }
        }
        return item;
    }
}
