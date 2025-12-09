package com.example.caloriecounter.ui.Reminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.NotificationManagerCompat;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // Get message from intent
        String message = intent.getStringExtra("message");
        if (message == null || message.isEmpty()) {
            message = "It's time for your reminder!";
        }

        // Ensure notification channel exists and build notification
        android.app.Notification notification = NotificationHelper.buildReminderNotification(context, message);

        // Android 13+ permission check
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return; // Stop if permission not granted
            }
        }

        // Show notification
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(1001, notification);
    }
}
