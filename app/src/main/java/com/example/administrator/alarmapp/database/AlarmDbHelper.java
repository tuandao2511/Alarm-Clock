package com.example.administrator.alarmapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.administrator.alarmapp.database.AlarmContract.AlarmEntry;
import static android.R.attr.version;

/**
 * Created by Administrator on 7/26/2017.
 */

public class AlarmDbHelper extends SQLiteOpenHelper {
    private static final String LOG_TAG = AlarmDbHelper.class.getName();

    private static final String DATABASE_NAME = "testing1.db";

    private static final int DATABASE_VERSION = 1;

    private static final  String SQL_CREATE_DATABASE = "CREATE TABLE " + AlarmEntry.TABLE_NAME + "("
            + AlarmEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + AlarmEntry.COLUMN_ACTIVE + " INTEGER NOT NULL,"
            + AlarmEntry.COLUMN_TIME + " TEXT NOT NULL,"
            + AlarmEntry.COLUMN_DAYS + " TEXT NOT NULL,"
            + AlarmEntry.COLUMN_RINGTONE + " TEXT,"
            + AlarmEntry.COLUMN_ALARM_OFF + " INTEGER NOT NULL,"
            + AlarmEntry.COLUMN_VIBRATE + " INTEGER NOT NULL)" ;


    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + AlarmEntry.TABLE_NAME;


    public AlarmDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /*tao database */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v(LOG_TAG,"da tao bang " +SQL_CREATE_DATABASE);

        db.execSQL(SQL_CREATE_DATABASE);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
