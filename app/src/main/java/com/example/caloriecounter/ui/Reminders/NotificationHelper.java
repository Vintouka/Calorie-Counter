package com.example.caloriecounter.ui.Reminders;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    // Unique channel ID and name
    public static final String CHANNEL_ID = "reminder_channel";
    public static final String CHANNEL_NAME = "Daily Reminders";

    /**
     * Creates the notification channel (required for Android 8.0+)
     */
    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for daily reminder notifications");

            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Builds a notification with the given message
     */
    public static Notification buildReminderNotification(Context context, String message) {
        // Ensure the notification channel exists
        createChannel(context);

        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder) // Simple built-in icon
                .setContentTitle("Calorie Counter Reminder")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();
    }
}
