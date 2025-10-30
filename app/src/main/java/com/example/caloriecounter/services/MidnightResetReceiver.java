package com.example.caloriecounter.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.caloriecounter.utils.DateUtils;

public class MidnightResetReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_DATE_CHANGED.equals(intent.getAction()) ||
                Intent.ACTION_TIME_CHANGED.equals(intent.getAction())) {

            // Check if we've already reset today
            SharedPreferences prefs = context.getSharedPreferences("AppState", Context.MODE_PRIVATE);
            String lastResetDate = prefs.getString("last_reset_date", "");
            String today = DateUtils.getTodayDate();

            if (!today.equals(lastResetDate)) {
                // Save the reset date
                prefs.edit().putString("last_reset_date", today).apply();

                // Note: Entries are already saved in the database with dates,
                // so we don't need to do anything else. The OverviewViewModel
                // automatically shows only today's entries.
            }
        }
    }
}