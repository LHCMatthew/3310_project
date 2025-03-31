package edu.cuhk.csci3310.a3310_project.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import edu.cuhk.csci3310.a3310_project.R;
import edu.cuhk.csci3310.a3310_project.activity.MainActivity;

public class NotificationHelper {
    private static final String CHANNEL_ID = "todo_reminders";
    private static final String CHANNEL_NAME = "Task Reminders";
    private static final String CHANNEL_DESCRIPTION = "Notifications for upcoming tasks";

    // Create notification channel (required for Android 8.0+)
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Show a task reminder notification
    public static void showTaskReminderNotification(Context context, long taskId, String taskTitle, String listName, long reminderTime) {
        // Create an intent to open the app when notification is tapped
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) taskId,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_today)
                .setContentTitle("Task Reminder")
                .setContentText("Task \"" + taskTitle + "\" is due in " + reminderTime + " minutes from now, don't forget!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Show the notification
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify((int) taskId, builder.build());
        } catch (SecurityException e) {
            // Handle missing notification permission
            e.printStackTrace();
        }
    }
}