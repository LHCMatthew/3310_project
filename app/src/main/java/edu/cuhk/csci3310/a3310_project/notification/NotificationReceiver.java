package edu.cuhk.csci3310.a3310_project.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import edu.cuhk.csci3310.a3310_project.database.TaskRepository;
import edu.cuhk.csci3310.a3310_project.models.Task;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long taskId = intent.getLongExtra("taskId", -1);
        if (taskId != -1) {
            // Get task details from database
            TaskRepository taskRepository = new TaskRepository(context);
            Task task = taskRepository.getTaskById(taskId);

            if (task != null && !task.isCompleted()) {
                // Get list name for this task
                String listName = taskRepository.getListNameById(task.getListId());

                // Show notification
                NotificationHelper.showTaskReminderNotification(
                        context,
                        task.getId(),
                        task.getTitle(),
                        listName
                );
            }
        }
    }
}