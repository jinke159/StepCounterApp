package com.jk.stepcounter;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;

import com.dome.jk.fitdome.R;
import com.jk.stepcounter.utils.Utils;

import java.util.List;

/**
 * Created by 17652 on 2018/2/26.
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String START_SERVICE = "start_service";
    public static final String BOOT_START = "boot_start";
    public static final String SAVE_FREQUENCY = "save_frequency";

    private SettingsActivity mActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        EditTextPreference saveFrequency = (EditTextPreference) findPreference(SAVE_FREQUENCY);
        saveFrequency.setSummary(String.format("每%s分钟保存一次步数", saveFrequency.getText()));

    }

    @Override
    public void onResume() {
        super.onResume();
        mActivity = (SettingsActivity) getActivity();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        JobScheduler jobScheduler = (JobScheduler) mActivity.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        //当设置更改时调用
        switch (key) {
            //是否开启服务
            case START_SERVICE:
                boolean aBoolean = sharedPreferences.getBoolean(key, false);
                if (aBoolean) {
                    Utils.startStepCountService(mActivity);
                } else {

                    if (jobScheduler != null) {
                        jobScheduler.cancel(StepCountService.JOB_ID);
                    }

                    mActivity.stopService(new Intent(mActivity, StepCountService.class));

                    SwitchPreference bootStart = (SwitchPreference) findPreference(BOOT_START);
                    bootStart.setChecked(false);

                }
                break;

            case BOOT_START:
                //获取是否开机启动

                boolean boot = sharedPreferences.getBoolean(key, false);
                if (jobScheduler == null) return;
                JobInfo jobInfo = null;

                    //获取所有的jobinfo对象
                    List<JobInfo> allPendingJobs = jobScheduler.getAllPendingJobs();
                    //遍历查找jobid
                    for (JobInfo allPendingJob : allPendingJobs) {
                        if (allPendingJob.getId() == StepCountService.JOB_ID) {
                            jobInfo = allPendingJob;
                            break;
                        }
                    }
                    //如果存在jobinfo对象，说明有此任务，查看是否开机启动，如果不是开机启动，将任务取消并重新开始
                    //没有job对象则没有运行此任务则不操作
                    if (jobInfo != null && jobInfo.isPersisted() != boot) {
                        jobScheduler.cancel(StepCountService.JOB_ID);
                        Utils.startJob(StepCountService.JOB_ID, mActivity, jobScheduler);

                }

                break;
            case SAVE_FREQUENCY:
                String string = sharedPreferences.getString(key, "120");
                EditTextPreference saveFrequency = (EditTextPreference) findPreference(SAVE_FREQUENCY);
                int integer;
                integer = Utils.string2Int(string);

                if (integer < 15) {
                    integer = 15;
                    string = Integer.toString(integer);
                    sharedPreferences.edit().putString(key, string).apply();

                } else if (integer > 720) {
                    integer = 720;
                    string = Integer.toString(integer);
                    sharedPreferences.edit().putString(key, string).apply();
                }
                saveFrequency.setText(string);
                saveFrequency.setSummary(String.format("每%d分钟保存一次步数", integer));

                if (jobScheduler == null || !sharedPreferences.getBoolean(START_SERVICE, false)) return;
                jobScheduler.cancel(StepCountService.JOB_ID);
                Utils.startJob(StepCountService.JOB_ID, mActivity, jobScheduler);

                break;

        }
    }
}
