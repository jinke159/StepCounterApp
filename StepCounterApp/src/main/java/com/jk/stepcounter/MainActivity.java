package com.jk.stepcounter;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.dome.jk.fitdome.R;
import com.jk.stepcounter.adapter.MainRecyclerViewAdapter;
import com.jk.stepcounter.database.StepCountDbHelper;
import com.jk.stepcounter.info.StepCountInfo;
import com.jk.stepcounter.utils.Utils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private MainRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    boolean isBindService = false;
    private StepCountService.StepCountBinder mStepCountBinder;
    private ArrayList<StepCountInfo> mStepCountInfos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        Utils.startStepCountService(this);
        mRecyclerView =  findViewById(R.id.recyclerView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);


        // specify an adapter
        mStepCountInfos = new ArrayList<>();
        mAdapter = new MainRecyclerViewAdapter(mStepCountInfos);
        mRecyclerView.setAdapter(mAdapter);

        new Thread(new Runnable() {
            @Override
            public void run() {

                mStepCountInfos = Utils.queryAllMore(StepCountDbHelper.getInstance(getApplicationContext()),mStepCountInfos,0);
                mAdapter.setDataSet(mStepCountInfos);

            }
        }).start();

    }


    //bind服务
    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isStart = sharedPref.getBoolean(SettingsFragment.START_SERVICE, false);
        if (!isStart){
            mAdapter.setServiceStatus(" 后台计步服务已关闭");

        }else {
            mAdapter.setServiceStatus("");

        }
        if (Utils.isStepSensor(this)) {
            Intent intent = new Intent(this, StepCountService.class);
            bindService(intent, mConnection, BIND_AUTO_CREATE);
        }

    }

    //unbind服务
    @Override
    protected void onStop() {
        super.onStop();

        if (isBindService) {
            unbindService(mConnection);

            isBindService = false;
        }
    }

    //Bind服务的监听
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mStepCountBinder = (StepCountService.StepCountBinder) service;


            int i = mStepCountBinder.getTodayStepCount();
            String s = String.valueOf(i);
            mAdapter.setTodayData(s);
            mStepCountBinder.setStepCountChangeListener(new StepCountService.StepCountChangeListener() {
                @Override
                public void stepCountChange(int todayStepCount) {
                    String text = Integer.toString(todayStepCount);
                    mAdapter.setTodayData(text);
                }
            });
            isBindService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBindService = false;
            mStepCountBinder.setStepCountChangeListener(null);
            mStepCountBinder = null;

        }
    };


    public void openSetting(View view) {
        startActivity(new Intent(this, SettingsActivity.class));

    }
}
