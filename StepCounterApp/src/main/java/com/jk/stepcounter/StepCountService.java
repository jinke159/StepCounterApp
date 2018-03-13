package com.jk.stepcounter;

import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.jk.stepcounter.database.StepCountDbHelper;
import com.jk.stepcounter.utils.Utils;

import java.util.List;

import static android.hardware.Sensor.TYPE_STEP_COUNTER;

public class StepCountService extends Service {


    private static final String TAG = "StepCountService";
    final StepCountBinder mIBinder = new StepCountBinder();

    class StepCountBinder extends Binder {
        private StepCountChangeListener mStepCountChangeListener = null;

        void setStepCountChangeListener(StepCountChangeListener stepCountChangeListener) {
            mStepCountChangeListener = stepCountChangeListener;
        }

        int getTodayStepCount() {
            return mTodayStepCount;
        }

        void saveData() {
            StepCountService.this.saveData();
        }
    }

    public static final int JOB_ID = 0X111828;

    private StepCountDbHelper mDbHelper = null;

    private BroadcastReceiver mBroadcastReceiver;

    private boolean firstStart = true;

    /**
     * 服务开始时或者写入数据库时的步数
     * 每次启动服务时或者写入数据库时，记录当前开机后总步数 {@link #mSumStepCount}
     */
    public int mStartStepCount;


    /**
     * 开机以来总步数
     */
    public int mSumStepCount;


    /**
     * 今天的总步数 <br/>
     * mTodayStepCount = {@link #mSumStepCount} - {@link #mStartStepCount} + {@link #mDatabaseStepCount} <br/>
     * {@link #mServiceStepCount} + {@link #mDatabaseStepCount}
     * 需要写入数据库
     */
    public int mTodayStepCount;


    /**
     * 服务开启后今天记录的步数 <br/>
     * {@link #mSumStepCount} - {@link #mStartStepCount}
     */
    public int mServiceStepCount;


    /**
     * 数据库中的步数 <br/>
     * 每次更新数据库时记录成今天总步数
     */
    public int mDatabaseStepCount;

    /**
     * 今天的日期
     */
    public String mDate = null;


    public StepCounterListener mStepCounterListener;
    public Sensor mStepCounter;
    private boolean isOpen = false;

    public StepCountService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mIBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initBroadcastReceiver();


        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            //总步数传感器，统计开机以来的总步数
            mStepCounter = sensorManager.getDefaultSensor(TYPE_STEP_COUNTER);

            registerSensor();
        } else {
            String NO_FIND_SENSOR = "获取传感器失败，可能是禁止了权限";
            Toast.makeText(this, NO_FIND_SENSOR, Toast.LENGTH_LONG).show();
        }

        new Thread() {
            @Override
            public void run() {
                mDbHelper = StepCountDbHelper.getInstance(getApplicationContext());

                mDate = Utils.queryDate(mDbHelper);

                if (mDate == null) {
                    mDate = "";
                }

                String nowDate = Utils.getNowDate();
                if (mDate.equals(nowDate)) {

                    mDatabaseStepCount = Utils.query(mDbHelper, mDate);

                    mTodayStepCount = mServiceStepCount + mDatabaseStepCount;

                    refreshTodayStepCount();
                } else {
                    newDay(nowDate);

                }
            }
        }.start();

        startJobService();

    }

    private void refreshTodayStepCount() {
        if (mIBinder.mStepCountChangeListener != null) {
            mIBinder.mStepCountChangeListener.stepCountChange(mTodayStepCount);
        }
    }

    private void startJobService() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean bootStart = sharedPref.getBoolean(SettingsFragment.START_SERVICE, false);
        if (!bootStart) {
            return;
        }
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler == null) {
            return;
        }
        List<JobInfo> jobInfos = jobScheduler.getAllPendingJobs();
        if (!jobInfos.isEmpty()) {
            for (JobInfo jobInfo : jobInfos) {
                if (jobInfo.getId() == JOB_ID) {
                    return;
                }
            }
        }

        Utils.startJob(JOB_ID, this, jobScheduler);

    }

    private void initBroadcastReceiver() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_DATE_CHANGED);
        intentFilter.addAction(Intent.ACTION_SHUTDOWN);

        // mId allows you to update the notification later on.
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                if (action != null) {
                    switch (action) {
                        case Intent.ACTION_DATE_CHANGED:


                            new Thread() {
                                @Override
                                public void run() {
                                    //先保存当天数据
                                    updateData();
                                    //再写入新的一天
                                    saveData();
                                }
                            }.start();


                            break;
                        case Intent.ACTION_SHUTDOWN:

                            saveData();
                            break;
                    }
                }
            }
        };
        registerReceiver(mBroadcastReceiver, intentFilter);

    }


    @Override
    public void onDestroy() {
        saveData();
        unregisterSensor();

        unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();


    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        saveData();

        unregisterSensor();

        unregisterReceiver(mBroadcastReceiver);
        super.onTaskRemoved(rootIntent);

    }

    private void registerSensor() {
        //注册传感器事件监听器

        if (isOpen) {
            return;
        }
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER) &&
                getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR)) {
            mStepCounterListener = new StepCounterListener();

            if (sensorManager != null) {

                if (mStepCounter == null) {
                    mStepCounter = sensorManager.getDefaultSensor(TYPE_STEP_COUNTER);
                }

                sensorManager.registerListener(mStepCounterListener, mStepCounter, SensorManager.SENSOR_DELAY_NORMAL);
                isOpen = true;
            }
        }
    }

    private void unregisterSensor() {
        //解注册传感器事件监听器
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER) &&
                getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR)) {

            if (sensorManager != null) {
                sensorManager.unregisterListener(mStepCounterListener);
            }
            isOpen = false;
            mStepCounterListener = null;
            mStepCounter = null;
        }
    }

    private void saveData() {
        if (mDbHelper == null) {
            mDbHelper = StepCountDbHelper.getInstance(getApplicationContext());
        }


        if (mDate == null) {
            mDate = Utils.queryDate(mDbHelper);

        }
        String nowDate = Utils.getNowDate();
        if (mDate.equals(nowDate)) {

            updateData();

        } else {
            newDay(nowDate);

        }


    }

    private void updateData() {
        if (Utils.updateData(mDbHelper, mDate, mTodayStepCount, 0, System.currentTimeMillis())) {
            //记录当前总步数
            mStartStepCount = mSumStepCount;

            //刷新当前数据库的步数
            mDatabaseStepCount = Utils.query(mDbHelper, mDate);
            //将临时数据重置
            todayStepCountChanged();

        } else {
            Log.i(TAG, "updateData: 保存数据时可能出现错误");
        }
    }

    private void newDay(String nowDate) {
        if (Utils.insertData(mDbHelper, nowDate, System.currentTimeMillis())) {
            //记录当前总步数
            mStartStepCount = mSumStepCount;
            //刷新当前数据库的步数
            mDatabaseStepCount = 0;
            todayStepCountChanged();


            mDate = nowDate;

        } else {
            Log.i(TAG, "updateData: 保存数据时可能出现错误");

        }
    }

    private void todayStepCountChanged() {
        //将临时数据重置
        mServiceStepCount = mSumStepCount - mStartStepCount;
        //重新计算今天步数
        mTodayStepCount = mServiceStepCount + mDatabaseStepCount;
        refreshTodayStepCount();
    }

    public class StepCounterListener implements SensorEventListener {


        StepCounterListener() {


        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            //步数发生改变时调用

            mSumStepCount = (int) event.values[0];

            if (firstStart) {
                mStartStepCount = mSumStepCount;
                firstStart = false;
            }

            todayStepCountChanged();

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            //精度发生改变时调用

        }
    }


    interface StepCountChangeListener {
        void stepCountChange(int todayStepCount);
    }


}
