package com.example.caloriecounter.ui.Reminders;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Pair;

import java.util.Calendar;

public class ReminderManager {

    private static final String PREFS_NAME = "ReminderPrefs";
    private static final String PREF_KEY_HOUR = "reminder_hour";
    private static final String PREF_KEY_MINUTE = "reminder_minute";
    private static final int REMINDER_REQUEST_CODE = 123;

    private Context context;
    private AlarmManager alarmManager;
    private SharedPreferences sharedPreferences;

    public ReminderManager(Context context) {
        this.context = context.getApplicationContext(); // Use ApplicationContext to avoid memory leaks
        // This is the critical part: Initialize AlarmManager and SharedPreferences here
        this.alarmManager = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
        this.sharedPreferences = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void setDailyReminder(int hour, int minute) {
        if (alarmManager == null) {
            // Failsafe in case getSystemService returns null
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // If the time has already passed for today, schedule it for tomorrow
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REMINDER_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Set an exact repeating alarm
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );

        // Save the reminder time to SharedPreferences
        sharedPreferences.edit()
                .putInt(PREF_KEY_HOUR, hour)
                .putInt(PREF_KEY_MINUTE, minute)
                .apply();
    }

    public void cancelReminder() {
        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REMINDER_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        // If the PendingIntent exists, cancel it
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }

        // Clear the reminder time from SharedPreferences
        sharedPreferences.edit().clear().apply();
    }

    public Pair<Integer, Integer> getReminderTime() {
        if (!sharedPreferences.contains(PREF_KEY_HOUR)) {
            return null; // No reminder is set
        }
        int hour = sharedPreferences.getInt(PREF_KEY_HOUR, -1);
        int minute = sharedPreferences.getInt(PREF_KEY_MINUTE, -1);

        if (hour != -1 && minute != -1) {
            return new Pair<>(hour, minute);
        }
        return null;
    }
}
