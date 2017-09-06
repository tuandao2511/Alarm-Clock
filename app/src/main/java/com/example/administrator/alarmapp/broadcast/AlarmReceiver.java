package com.example.administrator.alarmapp.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Administrator on 7/20/2017.
 */

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Alarm received!", Toast.LENGTH_LONG).show();
        Uri uri = intent.getData();
        Intent i = new Intent(context,AlarmService.class);
        i.setData(uri);
        context.startService(i);
    }

}
