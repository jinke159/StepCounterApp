package com.jk.stepcounter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.jk.stepcounter.utils.Utils;

import java.util.Objects;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean boot = sharedPref.getBoolean(SettingsFragment.BOOT_START, false);

        if (boot && Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {
            Utils.startStepCountService(context);

        }

    }
}
