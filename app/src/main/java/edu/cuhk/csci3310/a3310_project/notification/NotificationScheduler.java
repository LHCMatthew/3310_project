package edu.cuhk.csci3310.a3310_project.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Date;

import edu.cuhk.csci3310.a3310_project.models.Task;

public class NotificationScheduler {

    // Schedule a notification for a task (currently set for 1 hour before the due time)
    public static void scheduleTaskReminder(Context context, Task task) {
        // Only schedule if the task has a due date and is not completed
        if (task.getStartTime() > 0 && !task.isCompleted()) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.putExtra("taskId", task.getId());

            // Create a unique request code based on task ID
            int requestCode = (int) task.getId();

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Set notification time
            long notificationTime;
            if(task.isAllday()) {
                notificationTime = task.getDueDate(); // set it to the due date (at 00:00 of the due date)
            }
            else
                notificationTime = task.getStartTime() - task.getReminderTime() * 60 * 1000; // Convert minutes to milliseconds

            Log.d("NotificationScheduler", "Task: " + task.getTitle());
            Log.d("NotificationScheduler", "Current time: " + System.currentTimeMillis());
            Log.d("NotificationScheduler", "Notification time: " + notificationTime);
            Log.d("NotificationScheduler", "Start time: " + task.getStartTime());

            // If the time is in the past, don't schedule
            if (notificationTime > System.currentTimeMillis()) {
                Log.d("NotificationScheduler", "Scheduling notification for: " + new Date(notificationTime));
                try {
                    // For Android S (12) and above, check for permission
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
                        } else {
                            // Fallback to inexact alarm if we don't have permission
                            alarmManager.set(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
                            Log.d("NotificationScheduler", "Using inexact alarm (no permission for exact)");
                        }
                    } else {
                        // For versions below Android S
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
                    }
                } catch (SecurityException e) {
                    // Fallback to inexact alarm if exception occurs
                    alarmManager.set(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
                    Log.e("NotificationScheduler", "SecurityException when scheduling exact alarm: " + e.getMessage());
                }
            } else {
                Log.d("NotificationScheduler", "Not scheduling: time is in the past");
            }
        }
    }
}