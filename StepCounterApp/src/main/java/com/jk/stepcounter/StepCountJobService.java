package com.jk.stepcounter;

import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class StepCountJobService extends JobService {
    boolean mBound = false;
    private StepCountService.StepCountBinder mBinder;
    public static final int id = 0;

    @Override
    public boolean onStartJob(final JobParameters params) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isStartService = sharedPref.getBoolean(SettingsFragment.START_SERVICE, false);
        Intent service = new Intent(this, StepCountService.class);
        if (isStartService){
//            startService(service);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int i=0;
                    while (!mBound){
                        if (i>=4)break;
                        i++;
                        SystemClock.sleep(2000);
                    }

                    if (mBound) {


                        mBinder.saveData();
                    }


                    jobFinished(params,false);
                }
            }).start();
            return true;
        }else {
            stopService(service);
            JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (jobScheduler != null) {
                jobScheduler.cancel(StepCountService.JOB_ID);
            }
            return false;
        }

    }

    @Override
    public void onCreate() {

        Intent intent = new Intent(this, StepCountService.class);
        if (!bindService(intent, mConnection, Context.BIND_AUTO_CREATE)) {

            if (mBound) {
                unbindService(mConnection);
                mBound = false;
            }

        }


        super.onCreate();
    }

    @Override
    public void onDestroy() {

        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        super.onDestroy();
    }



    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }



    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mBinder = (StepCountService.StepCountBinder) service;
//            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

            mBinder = null;
            mBound = false;
        }
    };



}
