package com.example.administrator.alarmapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.content.Loader;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.audiofx.BassBoost;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.administrator.alarmapp.broadcast.AlarmReceiver;
import com.example.administrator.alarmapp.database.AlarmContract;
import com.example.administrator.alarmapp.database.AlarmContract.AlarmEntry;
import com.example.administrator.alarmapp.database.AlarmDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.R.attr.id;
import static android.R.attr.name;
import static android.webkit.ConsoleMessage.MessageLevel.LOG;
import static com.example.administrator.alarmapp.R.string.rington;

public class EditAlarm extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private  final String LOG_TAG = EditAlarm.class.getName();
    AlarmDbHelper mCreateDb;
    /*set thoi giam bao thuc */
    LinearLayout timeLinearLayout;
    TextView textAlarmPrompt;
    TimePickerDialog timePickerDialog;
    private final static int RQS_1 = 1;
    private int hour_x;
    private int minute_x;

    /*set ngay bao thuc*/
    LinearLayout daysLinearLayout;
    TextView textDays;
    AlertDialog dialog;
    ArrayList<Integer> selectedItems = new ArrayList();
    String [] items ;
    private boolean [] checkItems;

    /*set rington */
    LinearLayout ringtonLinearLayout;
    TextView textRington;
    private String [] itemsRington;
    private final int CODE_REQUEST_RINGTON =1;
    private String chosenRingtone;
    private Uri uriRingtone;

    /*set rung */
    LinearLayout vibrateLinearLayout;
    CheckBox vibrateCheckbox;
    private int checked;

    /*actived */
    private int mActived;
    private Uri uriIntent;
    /*tat bao thuc */
    LinearLayout alarmOffMethod;
    TextView alarmOffTextView;
    String [] itemsMethod;
    private int mPosition;
    private static final int DEFAULT = 0;
    private static final int GAME = 1;
    AlertDialog dialog1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_alarm);

         /*chinh dialog hen gio */
        timeLinearLayout = (LinearLayout) findViewById(R.id.time_edit_activity);
        textAlarmPrompt = (TextView) findViewById(R.id.time_edit_text_view);

         /* chinh ngay hen gio */
        daysLinearLayout = (LinearLayout) findViewById(R.id.repeat_edit_activity);
        textDays = (TextView) findViewById(R.id.day_edit_text_view);

         /*chon rington cho */
        ringtonLinearLayout = (LinearLayout) findViewById(R.id.ringtone_edit_activity);
        textRington = (TextView) findViewById(R.id.ringtone_edit_text_view);

        /*chon che do rung */
        vibrateLinearLayout = (LinearLayout) findViewById(R.id.vibrate_edit_activity);
        vibrateCheckbox = (CheckBox) findViewById(R.id.checkbox_vibrate);

        /*che do tat bao thuc */
        alarmOffMethod = (LinearLayout) findViewById(R.id.method_edit_activity);
        alarmOffTextView = (TextView) findViewById(R.id.method_edit_text_view);

        mCreateDb = new AlarmDbHelper(this);

        Intent intent = getIntent();
        uriIntent = intent.getData();

        if (uriIntent == null) {
            setTitle(getString(R.string.add_alarm));

             /* gio mac dinh  */
            setTitle(getString(R.string.set_alarm));
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            textAlarmPrompt.setText(simpleDateFormat.format(calendar.getTime()));
            Calendar calNow = Calendar.getInstance();
            hour_x = calNow.get(Calendar.HOUR_OF_DAY);
            minute_x = calNow.get(Calendar.MINUTE);
            /*ngay mac dinh*/
            textDays.setText(R.string.never_repeat);

            /*ringtone mac dinh */
            chosenRingtone = "default";
            textRington.setText("DEFAULT");

            /*rung mac dinh */
            vibrateCheckbox.setChecked(true);
            checked = 1;

            /*alarm off default*/
            mPosition = 0;
            alarmOffTextView.setText(R.string.default_string);

        }else {
            setTitle(getString(R.string.edit_alarm));
            getLoaderManager().initLoader(0,null,this);
        }




        /*nghe click gio */
        timeLinearLayout.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.N)
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                openTimePickerDialog(true);
            }
        });
        /*nghe click ngay */
        items = getResources().getStringArray(R.array.array_days_optical);
        checkItems = new boolean[items.length];
        daysLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMultiChoiceItems();
            }
        });
        /*nghe lick ringtone*/
        ringtonLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCustomItems();
            }
        });
        /*chon che do rung */
        vibrateLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVibrate();
            }
        });
        vibrateCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVibrate();
            }
        });
        /*chon che do tat bao thuc */
        alarmOffMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAlarmOffMethod();
            }
        });


    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.action_save:
                try {
                    saveAlarm();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveAlarm() throws JSONException {
        /*lay thoi gian bao thuc*/

        String time = textAlarmPrompt.getText().toString().trim();
        /*lay ngay bao thuc */
        JSONObject json = new JSONObject();
        json.put("days",new JSONArray(selectedItems));
        String daysList = json.toString();
        /*lay ringtone */
        String ringtone = chosenRingtone;
        /*chon che do rung */
        int vibrate = checked;
        /*che do tat bao thuc*/
        int alarmOff = mPosition;
        Log.v(LOG_TAG," position luu vao la gi " +alarmOff);



        ContentValues values = new ContentValues();

        values.put(AlarmEntry.COLUMN_ACTIVE,1);
        values.put(AlarmEntry.COLUMN_TIME,time);
        values.put(AlarmEntry.COLUMN_DAYS,daysList);
        values.put(AlarmEntry.COLUMN_RINGTONE,ringtone);
        values.put(AlarmEntry.COLUMN_VIBRATE,vibrate);
        values.put(AlarmEntry.COLUMN_ALARM_OFF,alarmOff);


        if (uriIntent == null) {
            Uri uri = getContentResolver().insert(AlarmEntry.CONTENT_URI,values);

            if (uri != null) {
                Toast.makeText(this,getString( R.string.saved_successful),Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,getString(R.string.saved_fault), Toast.LENGTH_SHORT).show();
            }
        }else {
            int newUp = getContentResolver().update(uriIntent,values,null,null);

            if (newUp > 0) {
                Toast.makeText(this,getString( R.string.saved_successful),Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,getString(R.string.saved_fault), Toast.LENGTH_SHORT).show();
            }
        }

        /*set thoi gian bao thuc */

        Calendar calendar = Calendar.getInstance();
        Calendar calNow = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,hour_x);
        calendar.set(Calendar.MINUTE,minute_x);

        if (calendar.compareTo(calNow) <=0) {
            calendar.add(Calendar.DATE,1);
        }

        Log.v(LOG_TAG,"gio " +calendar.get(Calendar.HOUR_OF_DAY) +" phut " + calendar.get(Calendar.MINUTE));
        int id = 0;
        Cursor cursor = getContentResolver().query(AlarmEntry.CONTENT_URI,null,null,null,null);
        while (cursor.moveToNext()) {
            if (cursor.isLast()) {
                int columnID = cursor.getColumnIndex(AlarmEntry._ID);
                id = cursor.getInt(columnID);
            }
        }


        Intent myIntent = new Intent(this,AlarmReceiver.class);
        Uri uri = ContentUris.withAppendedId(AlarmEntry.CONTENT_URI,id);
        myIntent.setData(uri);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int x = id*7;
        int y = x-7;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, x, myIntent,0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),pendingIntent);
        if (selectedItems.size()!=0) {

            for (int i=0; i<selectedItems.size(); i++) {
                int dayRepeat = selectedItems.get(i)+1;
                Log.v(LOG_TAG,"selected item " + dayRepeat);
                calendar.set(Calendar.DAY_OF_WEEK,dayRepeat);

                if(calendar.getTimeInMillis() < System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 7);
                }

                PendingIntent pendingRepeat = PendingIntent.getBroadcast(this, y+dayRepeat, myIntent,0);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),7 * 24 * 60 * 60 * 1000, pendingRepeat);
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void openTimePickerDialog(boolean is24h) {
        final String calSetPrevious = textAlarmPrompt.getText().toString();
        timePickerDialog = new TimePickerDialog(EditAlarm.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                textAlarmPrompt.setText(calSetPrevious);

                Calendar calNow = Calendar.getInstance();
                Calendar calSet = (Calendar) calNow.clone();
                calSet.set(Calendar.HOUR_OF_DAY,hourOfDay);
                calSet.set(Calendar.MINUTE,minute);
                hour_x = hourOfDay;
                minute_x = minute;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                textAlarmPrompt.setText(simpleDateFormat.format(calSet.getTime()));

            }
        },hour_x,minute_x,is24h);
        timePickerDialog.show();
    }



    /*dialog chon ngay*/

    private void openMultiChoiceItems() {

        AlertDialog.Builder builder = new AlertDialog.Builder(EditAlarm.this);
        builder.setTitle(R.string.repeat);

        builder.setMultiChoiceItems(items, checkItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position, boolean isChecked) {
              if(isChecked) {
                      selectedItems.add(position);
              }else {
                  selectedItems.remove(Integer.valueOf(position));
              }
            }
        });


        builder.setCancelable(false);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Collections.sort(selectedItems);
                StringBuilder item = cutDays(selectedItems);
                textDays.setText(item);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    if(dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        dialog = builder.create();
        dialog.show();
    }

    private StringBuilder cutDays(ArrayList<Integer> selectedItems) {
        StringBuilder item = new StringBuilder();
        if (selectedItems.size() == items.length) item.append(getString(R.string.everday_repeat));
        else if (selectedItems.size()==0) item.append(getString(R.string.never_repeat));
        else {
            for (int i = 0; i < selectedItems.size(); i++) {
                item.append(items[selectedItems.get(i)].toString().substring(0, 3));
                if (i != selectedItems.size() - 1) {
                    item.append(", ");
                }
            }
        }
        return item;
    }



    /*chon rington */

    private void openCustomItems() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditAlarm.this);
        builder.setTitle(rington);
        itemsRington = getResources().getStringArray(R.array.array_rington_optical);
        builder.setItems(itemsRington, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                switch (position) {
                    case 0: {
                        chosenRingtone = "default";
                        textRington.setText("DEFAULT");
                    }
                    case 1: {
                        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                        startActivityForResult(intent, CODE_REQUEST_RINGTON);
                        break;
                    }
                    case 2:{
                           chosenRingtone = null;
                           textRington.setText("");
                    }

                }
            }
        });


        dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == CODE_REQUEST_RINGTON)
        {
            uriRingtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            displayTone(uriRingtone);
        }
    }

    private void displayTone(Uri uriRingtone) {
        if (uriRingtone != null)
        {
            Ringtone ringtoneUri = RingtoneManager.getRingtone(EditAlarm.this,uriRingtone);
            String name = ringtoneUri.getTitle(EditAlarm.this);

            chosenRingtone = uriRingtone. toString();
            textRington.setText("" + name);
            Log.v(LOG_TAG,"chosen ringtone 1" + name);

        }
        else
        {
            textRington.setText("NONE");
            chosenRingtone = "";
        }
    }

    /*chon che do rung */

    private void openVibrate() {
        if (checked == 1) {
            vibrateCheckbox.setChecked(false);
            checked = 0;
        }else {
            vibrateCheckbox.setChecked(true);
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(1000);
            checked = 1;
        }
    }
    /*che do bao thuc */
    private void setAlarmOffMethod(){
        AlertDialog.Builder builder = new AlertDialog.Builder(EditAlarm.this);
        builder.setTitle(R.string.alarm_off_method);

        itemsMethod = getResources().getStringArray(R.array.array_method_optical);
        builder.setItems(itemsMethod, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                mPosition = position;
                Log.v(LOG_TAG," position la gi " +position);
                displayAlarmOffMethod(mPosition);
            }
        });

        dialog1 = builder.create();
        dialog1.show();

    }

    private void displayAlarmOffMethod(int mPosition) {

        switch (mPosition) {
            case DEFAULT:
                alarmOffTextView.setText(R.string.default_string);
                break;
            case GAME:
                alarmOffTextView.setText(R.string.english_game);
                break;
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String [] projection = {
                AlarmEntry._ID,
                AlarmEntry.COLUMN_TIME,
                AlarmEntry.COLUMN_DAYS,
                AlarmEntry.COLUMN_RINGTONE,
                AlarmEntry.COLUMN_VIBRATE,
                AlarmEntry.COLUMN_ACTIVE,
                AlarmEntry.COLUMN_ALARM_OFF
        };
        return new CursorLoader(this,uriIntent,projection,null,null,null);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            int columnTime = cursor.getColumnIndex(AlarmEntry.COLUMN_TIME);
            int columnDays = cursor.getColumnIndex(AlarmEntry.COLUMN_DAYS);
            int columnRingtone = cursor.getColumnIndex(AlarmEntry.COLUMN_RINGTONE);
            int columnVibrate = cursor.getColumnIndex(AlarmEntry.COLUMN_VIBRATE);
            int columnActive = cursor.getColumnIndex(AlarmEntry.COLUMN_ACTIVE);
            int columnAlarmOff = cursor.getColumnIndex(AlarmEntry.COLUMN_ALARM_OFF);

            /*hien thi thoi gian */
            String displayTime = cursor.getString(columnTime);
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            Date date =null;
            try {
                date = simpleDateFormat.parse(displayTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            cal.setTime(date);
            hour_x = cal.get(Calendar.HOUR_OF_DAY);
            minute_x = cal.get(Calendar.MINUTE);
            textAlarmPrompt.setText(displayTime);

            /* hien thi thoi gian */
            String jsonString = cursor.getString(columnDays);
            JSONObject json = null;
            try {
                json = new JSONObject(jsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONArray items = json.optJSONArray("days");


            for (int i=0; i<items.length(); i++) {
                try {
                    selectedItems.add(items.getInt(i));
                    checkItems[i] = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            StringBuilder item = cutDays(selectedItems);
            textDays.setText(item);
            /*hien thi ringtone */
            chosenRingtone = cursor.getString(columnRingtone);
            Log.v(LOG_TAG,"ringtone la gi " +chosenRingtone);
            if (chosenRingtone!=null){
                if(chosenRingtone.equals("default")) {
                    textRington.setText("DEFAULT");
                }else {
                    uriRingtone = Uri.parse(chosenRingtone);
                    displayTone(uriRingtone);
                }
            }
            else {
                textRington.setText("NONE");
            }

            /*che do rung */
            int vibrate = cursor.getInt(columnVibrate);
            Log.v(LOG_TAG,"vibrate la " +vibrate);
            checked = vibrate;
            if (vibrate == 1) {
                vibrateCheckbox.setChecked(true);
            }else {
                vibrateCheckbox.setChecked(false);
            }

            /*che do tat bao thuc */
            int alarmOff = cursor.getInt(columnAlarmOff);
            Log.v(LOG_TAG,"alarm off la gi " + alarmOff);
            displayAlarmOffMethod(alarmOff);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
