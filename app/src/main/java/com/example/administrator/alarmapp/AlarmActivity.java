package com.example.administrator.alarmapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.administrator.alarmapp.database.AlarmContract;
import com.example.administrator.alarmapp.database.AlarmContract.AlarmEntry;
import com.example.administrator.alarmapp.database.AlarmDbHelper;

public class AlarmActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{


    ListView alarmListView ;
    AlarmCursorAdapter alarmCursorAdapter ;
    AlarmDbHelper mCreateDb;
    public final static String LOG_TAG = AlarmActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Intent intent = new Intent(AlarmActivity.this,EditAlarm.class);
                startActivity(intent);
            }
        });
        mCreateDb = new AlarmDbHelper(this);
        alarmListView = (ListView) findViewById(R.id.list_view);

        alarmCursorAdapter = new AlarmCursorAdapter(this,null,0);
        alarmListView.setAdapter(alarmCursorAdapter);
        getLoaderManager().initLoader(0,null,this);


        alarmListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(AlarmActivity.this,EditAlarm.class);
                Uri uri = ContentUris.withAppendedId(AlarmEntry.CONTENT_URI,id);
                Log.v(LOG_TAG,"uri la gi " +uri);
                i.setData(uri);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_alarm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save1) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

        return new CursorLoader(this,AlarmEntry.CONTENT_URI,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        alarmCursorAdapter.swapCursor(cursor);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        alarmCursorAdapter.swapCursor(null);
    }
}
