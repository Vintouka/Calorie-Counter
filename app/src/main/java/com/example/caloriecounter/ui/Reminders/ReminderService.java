package com.example.caloriecounter.ui.Reminders;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class ReminderService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Get the reminder message
        String message = intent.getStringExtra("message");
        if (message == null || message.isEmpty()) {
            message = "It's time for your reminder!";
        }

        // Build the notification using the helper
        Notification notification =
                NotificationHelper.buildReminderNotification(this, message);

        // Start as a foreground service
        startForeground(1, notification);

        // Stop service after showing notification
        stopSelf();

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
