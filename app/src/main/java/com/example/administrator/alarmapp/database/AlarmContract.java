package com.example.administrator.alarmapp.database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Administrator on 7/26/2017.
 */

public final class AlarmContract {

    public static final String CONTENT_AUTHORITY = "com.example.administrator.alarmapp";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH = "alarm";

    private AlarmContract (){

    }

    public static final class AlarmEntry implements BaseColumns{

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI,PATH);

        /*ten cua bang*/
        public final static String TABLE_NAME = "alarm";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_TIME = "time";

        public final static String COLUMN_DAYS = "days";

        public final static String COLUMN_RINGTONE = "ringtone";

        public final static String COLUMN_VIBRATE = "vibrate";

        public final static String COLUMN_ACTIVE = "active";

        public final static String COLUMN_ALARM_OFF = "method";

    }
}
