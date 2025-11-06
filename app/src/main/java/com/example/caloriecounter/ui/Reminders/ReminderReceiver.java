package com.example.caloriecounter.ui.Reminders;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;

import com.example.caloriecounter.R;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "calorie_reminder_channel";
    private static final int NOTIFICATION_ID = 101; // A unique ID for the notification

    @Override
    public void onReceive(Context context, Intent intent) {
        // Create the NotificationChannel on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Calorie Reminder";
            String description = "Channel for daily calorie logging reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Make sure this drawable exists
                .setContentTitle("Calorie Goal Reminder")
                .setContentText("Don't forget to log your calories and check your progress!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true); // Dismiss the notification when the user taps on it

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Check for permission again right before posting (required for API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // If permission is not granted, we cannot show the notification.
            // The user must grant it from the app's UI.
            return;
        }

        // The system will now allow the notification to be posted
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
