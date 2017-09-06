package com.example.administrator.alarmapp.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.administrator.alarmapp.database.AlarmContract.AlarmEntry;


/**
 * Created by Administrator on 7/26/2017.
 */

public class AlarmProvider extends ContentProvider {

    private final static String LOG_TAG = AlarmProvider.class.getName();
    AlarmDbHelper mCreateDb;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int ALARMS = 100;
    private static final int ALARM_ID = 101;

    static {

        /*ex: content://com.example.administrator.alarmapp/alarm */
        sUriMatcher.addURI(AlarmContract.CONTENT_AUTHORITY,AlarmContract.PATH,ALARMS);

        /*ex: content://com.example.administrator.alarmapp/alarm/1 */
        sUriMatcher.addURI(AlarmContract.CONTENT_AUTHORITY,AlarmContract.PATH + "/#",ALARM_ID);
    }

    @Override
    public boolean onCreate() {
        mCreateDb = new AlarmDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase database = mCreateDb.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ALARMS:
                cursor =  database.query(AlarmEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case ALARM_ID:
                selection = AlarmEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor =  database.query(AlarmEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);

        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int match = sUriMatcher.match(uri);
        Log.v(LOG_TAG, "Match la gi " + match);
        switch (match) {
            case ALARMS:
                return insertAlarm(uri,values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }

    }

    private Uri insertAlarm(Uri uri, ContentValues values) {
        SQLiteDatabase db = mCreateDb.getWritableDatabase();
        long newId = db.insert(AlarmEntry.TABLE_NAME,null,values);
        getContext().getContentResolver().notifyChange(uri,null);
        return ContentUris.withAppendedId(uri,newId);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ALARMS:
                return deleteAlarm(uri,selection,selectionArgs);
            case ALARM_ID:
                selection = AlarmEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return deleteAlarm(uri,selection,selectionArgs);
            default:
                throw new IllegalArgumentException("delete error" +uri);
        }

    }

    private int deleteAlarm(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mCreateDb.getWritableDatabase();
        int delete = db.delete(AlarmEntry.TABLE_NAME,selection,selectionArgs);
        if (delete !=0) {
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return delete;
    }


    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ALARMS:
                return updateAlarm(uri,selection,selectionArgs,values);
            case ALARM_ID:
                selection = AlarmEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateAlarm(uri,selection,selectionArgs,values);
            default:
                throw new IllegalArgumentException("update error" +uri);
        }
    }

    private int updateAlarm(Uri uri, String selection, String[] selectionArgs,ContentValues values) {
        SQLiteDatabase db = mCreateDb.getWritableDatabase();
        int update =  db.update(AlarmEntry.TABLE_NAME,values,selection,selectionArgs);
        if (update !=0){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return update;
    }
}
