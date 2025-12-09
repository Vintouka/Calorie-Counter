package com.example.caloriecounter.ui.Reminders;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReminderManager {

    private static final String PREFS_NAME = "ReminderPrefs";
    private static final String PREF_KEY_REMINDERS = "reminders";

    private Context context;
    private AlarmManager alarmManager;
    private SharedPreferences sharedPreferences;

    public ReminderManager(Context context) {
        this.context = context.getApplicationContext();
        this.alarmManager = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
        this.sharedPreferences = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }


    public void addReminder(int hour, int minute, String message) {
        try {
            JSONArray reminders = getRemindersJson();

            // Generate a unique request code based on time + hash of message
            int requestCode = (hour * 60 + minute) + message.hashCode();

            // Save reminder details
            JSONObject reminderObj = new JSONObject();
            reminderObj.put("hour", hour);
            reminderObj.put("minute", minute);
            reminderObj.put("message", message);
            reminderObj.put("requestCode", requestCode);

            reminders.put(reminderObj);
            sharedPreferences.edit()
                    .putString(PREF_KEY_REMINDERS, reminders.toString())
                    .apply();

            // Schedule the alarm
            scheduleAlarm(hour, minute, message, requestCode);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void cancelReminder(int requestCode) {
        JSONArray reminders = getRemindersJson();
        JSONArray updated = new JSONArray();

        for (int i = 0; i < reminders.length(); i++) {
            try {
                JSONObject obj = reminders.getJSONObject(i);
                int code = obj.getInt("requestCode");
                if (code == requestCode) {
                    // Cancel this alarm
                    Intent intent = new Intent(context, ReminderReceiver.class);
                    PendingIntent pi = PendingIntent.getBroadcast(
                            context,
                            code,
                            intent,
                            PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
                    );
                    if (pi != null) {
                        alarmManager.cancel(pi);
                        pi.cancel();
                    }
                } else {
                    updated.put(obj);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Save updated reminders
        sharedPreferences.edit().putString(PREF_KEY_REMINDERS, updated.toString()).apply();
    }


    public List<JSONObject> getAllReminders() {
        JSONArray reminders = getRemindersJson();
        List<JSONObject> list = new ArrayList<>();
        for (int i = 0; i < reminders.length(); i++) {
            try {
                list.add(reminders.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    private JSONArray getRemindersJson() {
        String saved = sharedPreferences.getString(PREF_KEY_REMINDERS, "[]");
        try {
            return new JSONArray(saved);
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

    private void scheduleAlarm(int hour, int minute, String message, int requestCode) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("message", message);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }


    public void cancelAll() {
        List<JSONObject> all = getAllReminders();
        for (JSONObject obj : all) {
            try {
                cancelReminder(obj.getInt("requestCode"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        sharedPreferences.edit().remove(PREF_KEY_REMINDERS).apply();
    }
}
