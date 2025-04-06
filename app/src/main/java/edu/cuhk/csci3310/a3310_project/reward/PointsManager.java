package edu.cuhk.csci3310.a3310_project.reward;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import edu.cuhk.csci3310.a3310_project.models.Task;

public class PointsManager {
    private static final String TAG = "PointsManager";
    private static final String PREFS_NAME = "todo_points_prefs";
    private static final String KEY_POINTS = "user_points";
    private static final String KEY_TASKS_COMPLETED_ON_TIME = "tasks_completed_on_time";
    private static final String KEY_TASKS_COMPLETED_LATE = "tasks_completed_late";
    private static final String KEY_PROCESSED_TASK_IDS = "processed_task_ids";

    // Award points based on priority and timeliness
    public static void processTaskCompletion(Context context, Task task) {
        if (task == null || !task.isCompleted() || task.getDueDate() <= 0) {
            return; // No points for tasks without due dates
        }

        // Check if this task has already been processed for points
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> processedTaskIds = prefs.getStringSet(KEY_PROCESSED_TASK_IDS, new HashSet<>());

        String taskIdStr = String.valueOf(task.getId());
        if (processedTaskIds.contains(taskIdStr)) {
            // Task already processed, don't award/deduct points again
            return;
        }

        int pointsChange;
        boolean onTime;

        // Get the date-only components by resetting time to midnight
        Calendar completionCal = Calendar.getInstance();
        completionCal.setTimeInMillis(task.getCompletionDate());
        completionCal.set(Calendar.HOUR_OF_DAY, 0);
        completionCal.set(Calendar.MINUTE, 0);
        completionCal.set(Calendar.SECOND, 0);
        completionCal.set(Calendar.MILLISECOND, 0);

        Calendar dueCal = Calendar.getInstance();
        dueCal.setTimeInMillis(task.getDueDate());
        dueCal.set(Calendar.HOUR_OF_DAY, 0);
        dueCal.set(Calendar.MINUTE, 0);
        dueCal.set(Calendar.SECOND, 0);
        dueCal.set(Calendar.MILLISECOND, 0);

        // Get date-only timestamps for comparison
        Date completionDate = completionCal.getTime();
        Date dueDate = dueCal.getTime();

        // Calculate points based on whether task was completed on time
        if (completionDate.before(dueDate) || completionDate.equals(dueDate)) {
            // Task completed on time - award points based on priority
            onTime = true;

            switch (task.getPriority()) {
                case 2: // High priority
                    pointsChange = 5;
                    break;
                case 1: // Medium priority
                    pointsChange = 3;
                    break;
                default: // Low priority
                    pointsChange = 1;
                    break;
            }
        } else {
            // Task completed late - deduct points based on days late
            onTime = false;

            // Calculate days late (date difference only)
            long completionTimeMs = completionCal.getTimeInMillis();
            long dueTimeMs = dueCal.getTimeInMillis();
            long diffMs = completionTimeMs - dueTimeMs;
            long daysLate = diffMs / (1000 * 60 * 60 * 24); // Convert ms to days

            // Cap penalty at -5 points but scale based on priority
            int basePenalty = Math.min(5, (int)(daysLate + 1));
            switch (task.getPriority()) {
                case 2: // High priority - higher penalty
                    pointsChange = -basePenalty * 2;
                    break;
                case 1: // Medium priority
                    pointsChange = -basePenalty;
                    break;
                default: // Low priority - smaller penalty
                    pointsChange = -Math.max(1, basePenalty / 2);
                    break;
            }
        }

        // Mark this task as processed
        Set<String> updatedProcessedTaskIds = new HashSet<>(processedTaskIds);
        updatedProcessedTaskIds.add(taskIdStr);
        prefs.edit().putStringSet(KEY_PROCESSED_TASK_IDS, updatedProcessedTaskIds).apply();

        // Update points and show notification
        updatePoints(context, pointsChange, onTime);
        showPointsNotification(context, pointsChange);
    }

    // Method to remove a task ID from processed list (use when deleting a task)
    public static void removeProcessedTaskId(Context context, long taskId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> processedTaskIds = prefs.getStringSet(KEY_PROCESSED_TASK_IDS, new HashSet<>());

        String taskIdStr = String.valueOf(taskId);
        if (processedTaskIds.contains(taskIdStr)) {
            Set<String> updatedProcessedTaskIds = new HashSet<>(processedTaskIds);
            updatedProcessedTaskIds.remove(taskIdStr);
            prefs.edit().putStringSet(KEY_PROCESSED_TASK_IDS, updatedProcessedTaskIds).apply();
        }
    }

    private static void updatePoints(Context context, int pointsDelta, boolean onTime) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Update total points
        int currentPoints = prefs.getInt(KEY_POINTS, 0);
        editor.putInt(KEY_POINTS, currentPoints + pointsDelta);

        // Update statistics
        if (onTime) {
            int tasksOnTime = prefs.getInt(KEY_TASKS_COMPLETED_ON_TIME, 0);
            editor.putInt(KEY_TASKS_COMPLETED_ON_TIME, tasksOnTime + 1);
        } else {
            int tasksLate = prefs.getInt(KEY_TASKS_COMPLETED_LATE, 0);
            editor.putInt(KEY_TASKS_COMPLETED_LATE, tasksLate + 1);
        }

        editor.apply();
        Log.d(TAG, "Points updated: " + pointsDelta + ", new total: " + (currentPoints + pointsDelta));
    }

    private static void showPointsNotification(Context context, int points) {
        String message;
        if (points > 0) {
            message = "Congratulations! You earned " + points + " points!";
        } else {
            message = "You lost " + Math.abs(points) + " points for completing late.";
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static int getPoints(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_POINTS, 0);
    }

    public static int getTasksCompletedOnTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_TASKS_COMPLETED_ON_TIME, 0);
    }

    public static int getTasksCompletedLate(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_TASKS_COMPLETED_LATE, 0);
    }
}
