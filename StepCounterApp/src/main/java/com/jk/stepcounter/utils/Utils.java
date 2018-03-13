package com.jk.stepcounter.utils;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.dome.jk.fitdome.R;
import com.jk.stepcounter.SettingsFragment;
import com.jk.stepcounter.StepCountJobService;
import com.jk.stepcounter.StepCountService;
import com.jk.stepcounter.info.StepCountInfo;
import com.jk.stepcounter.database.StepCountContract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by 17652 on 2018/2/21.
 */

public class Utils {

    public static String stampToDate(long timeMillis) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(timeMillis);
        return simpleDateFormat.format(date);
    }


    public static String stampToDate(String timeMillis) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Long aLong = Long.valueOf(timeMillis);
        Date date = new Date(aLong);
        return simpleDateFormat.format(date);


    }

    public static void notify(Context context, String title, String content, int id) {


        Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_notification_def)
                .setContentTitle(title)
                .setContentText(content);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        if (mNotificationManager != null) {
            mNotificationManager.notify(id, builder.build());
        }

    }


    public static String getNowDate() {
        long timeStamp = System.currentTimeMillis();
        return stampToDate(timeStamp);
    }

    public static boolean insertData(SQLiteOpenHelper dbHelper, String date, long nowTimestamp) {

        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        //日期
        values.put(StepCountContract.StepData.COLUMN_NAME_DATE, date);
        //今日步数
        values.put(StepCountContract.StepData.COLUMN_NAME_TODAY_FINAL_STEP_COUNT, "0");
        //最后一步时间戳
        values.put(StepCountContract.StepData.COLUMN_NAME_TIMESTAMP, "0");
        //写入数据库时间戳
        values.put(StepCountContract.StepData.WRITE_DATABASE_TIMESTAMP, Long.toString(nowTimestamp));

        // Insert the new row, returning the primary key value of the new row
        long newRowId =
                db.insert(StepCountContract.StepData.TABLE_NAME, null, values);
        return newRowId != -1;
    }

    public static int query(SQLiteOpenHelper dbHelper, String date) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                StepCountContract.StepData.COLUMN_NAME_TODAY_FINAL_STEP_COUNT
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = StepCountContract.StepData.COLUMN_NAME_DATE + " = ?";
        String[] selectionArgs = {date};

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                StepCountContract.StepData.COLUMN_NAME_DATE + " DESC";

        Cursor c = db.query(
                StepCountContract.StepData.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
        if (c == null) {
            return 0;
        }

        if (c.moveToFirst()) {
            String stepCount = c.getString(c.getColumnIndex(StepCountContract.StepData.COLUMN_NAME_TODAY_FINAL_STEP_COUNT));
            c.close();

            return Integer.valueOf(stepCount);

        }
        c.close();
        return 0;
    }

    /**
     * @param dbHelper 数据库帮助类对象
     * @return 最后一天的日期
     */
    public static String queryDate(SQLiteOpenHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                StepCountContract.StepData.COLUMN_NAME_DATE
        };


        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                StepCountContract.StepData.COLUMN_NAME_DATE + " DESC";

        Cursor c = db.query(
                StepCountContract.StepData.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
        if (c == null) return "";

        if (c.moveToFirst()) {
            String stepCount = c.getString(c.getColumnIndex(StepCountContract.StepData.COLUMN_NAME_DATE));
            c.close();
            if (TextUtils.isEmpty(stepCount)) {
                return "";
            }

            return stepCount;

        }
        c.close();

        return "";
    }

    public static ArrayList<StepCountInfo> queryAll(SQLiteOpenHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                StepCountContract.StepData.COLUMN_NAME_DATE,
                StepCountContract.StepData.COLUMN_NAME_TODAY_FINAL_STEP_COUNT
        };

        // Filter results WHERE "title" = 'My Title'
//        String selection = StepData.COLUMN_NAME_DATE + " = ?";
//        String[] selectionArgs = {date};

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                StepCountContract.StepData.COLUMN_NAME_DATE + " DESC";

        Cursor c = db.query(
                StepCountContract.StepData.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
        ArrayList<StepCountInfo> stepCountInfos = null;
        if (c.moveToFirst()) {
            String date;
            String stepCount;
            stepCountInfos = new ArrayList<>();
            do {
                date = c.getString(c.getColumnIndexOrThrow(projection[0]));
                stepCount = c.getString(c.getColumnIndexOrThrow(projection[1]));
                stepCountInfos.add(new StepCountInfo(date, stepCount));
            } while (c.moveToNext());
        }

        c.close();
        return stepCountInfos;
    }

    public static ArrayList<StepCountInfo> queryAllMore(SQLiteOpenHelper dbHelper, @NonNull ArrayList<StepCountInfo> list, int i) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                StepCountContract.StepData.COLUMN_NAME_DATE,
                StepCountContract.StepData.COLUMN_NAME_TODAY_FINAL_STEP_COUNT
        };

        // Filter results WHERE "title" = 'My Title'
//        String selection = StepData.COLUMN_NAME_DATE + " = ?";
//        String[] selectionArgs = {date};

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                StepCountContract.StepData.COLUMN_NAME_DATE + " DESC";

        String limit =
                String.valueOf(i * 14) + "," + (i + 1) * 14;
        Cursor c = db.query(
                StepCountContract.StepData.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder,                      // The sort order
                limit                           // 查询的范围
        );

        if (c.moveToFirst()) {
            String date;
            String stepCount;

            do {
                date = c.getString(c.getColumnIndexOrThrow(projection[0]));
                stepCount = c.getString(c.getColumnIndexOrThrow(projection[1]));
                list.add(new StepCountInfo(date, stepCount));
            } while (c.moveToNext());
        }

        c.close();
        return list;
    }

    /**
     * @param dbHelper     数据库帮助类
     * @param date         要修改哪一天的数据
     * @param stepCount    修改的步数
     * @param timestamp    最后一步时间戳
     * @param nowTimestamp 写入数据库的时间
     * @return true写入成功
     */
    public static boolean updateData(SQLiteOpenHelper dbHelper, String date, int stepCount, long timestamp, long nowTimestamp) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(StepCountContract.StepData.COLUMN_NAME_TODAY_FINAL_STEP_COUNT, Integer.toString(stepCount));

//        if (timestamp != null)
        values.put(StepCountContract.StepData.COLUMN_NAME_TIMESTAMP, Long.toString(timestamp));

//        if (nowTimestamp != null)
        values.put(StepCountContract.StepData.WRITE_DATABASE_TIMESTAMP, Long.toString(nowTimestamp));


        // Which row to update, based on the title
        String selection = StepCountContract.StepData.COLUMN_NAME_DATE + " LIKE ?";
        String[] selectionArgs = {date};

        int count =
                db.update(
                        StepCountContract.StepData.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
        return count >= 1;
    }

    public static void startJob(int JOB_ID, Context context, JobScheduler jobScheduler) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean bootStart = sharedPref.getBoolean(SettingsFragment.BOOT_START, false);
        String saveFrequency = sharedPref.getString(SettingsFragment.SAVE_FREQUENCY, "120");
        int min = Utils.string2Int(saveFrequency);

        JobInfo.Builder jobBuilder = new JobInfo.Builder(JOB_ID, new ComponentName(context, StepCountJobService.class))
                .setPeriodic(min * 60000);
        if (bootStart)
            jobBuilder.setPersisted(true);//开机启动

        JobInfo build = jobBuilder
//                .setMinimumLatency(50000)// 任务最少延迟时间
//                .setOverrideDeadline(60000)// 任务deadline，当到期没达到指定条件也会开始执行 ;
                .build();

        jobScheduler.schedule(build);
    }

    public static int string2Int(String string) {
        int integer;
        try {

            integer = Integer.valueOf(string);
        } catch (NumberFormatException ignored) {
            Log.e("string2Int", "onSharedPreferenceChanged: ", ignored);
            integer = 120;
        }
        return integer;
    }

    public static void startStepCountService(Context context) {
        if (!isStepSensor(context)) {
            Toast.makeText(context, "不支持计步", Toast.LENGTH_LONG).show();
            return;
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isStart = sharedPref.getBoolean(SettingsFragment.START_SERVICE, false);
        if (isStart) {
            context.startService(new Intent(context, StepCountService.class));
        }
    }

    //检测是否支持计步
    public static boolean isStepSensor(Context context) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER);
    }
}
