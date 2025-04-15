package edu.cuhk.csci3310.a3310_project.adapters;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.cuhk.csci3310.a3310_project.R;
import edu.cuhk.csci3310.a3310_project.models.Task;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> tasks;
    private OnTaskClickListener listener;
    private long currentWeekStartTime;

    public interface OnTaskClickListener {
        void onTaskCheckChanged(Task task, boolean isChecked);
        void onTaskClick(Task task);
        void onTaskDelete(Task task);
    }

    public TaskAdapter(List<Task> tasks, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
        calculateCurrentWeekStartTime();
    }

    private void calculateCurrentWeekStartTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Roll back to Sunday
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        calendar.add(Calendar.DAY_OF_MONTH, -(dayOfWeek - Calendar.SUNDAY));
        currentWeekStartTime = calendar.getTimeInMillis();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);

        holder.titleTextView.setText(task.getTitle());
        holder.descriptionTextView.setText(task.getDescription());

        // Format date
        if (task.getDueDate() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            String dueDate = "Due: " + sdf.format(new Date(task.getDueDate()));
            holder.dueTextView.setText(dueDate);

            // Calculate start of today (midnight)
            Calendar todayStart = Calendar.getInstance();
            todayStart.set(Calendar.HOUR_OF_DAY, 0);
            todayStart.set(Calendar.MINUTE, 0);
            todayStart.set(Calendar.SECOND, 0);
            todayStart.set(Calendar.MILLISECOND, 0);
            long todayStartTime = todayStart.getTimeInMillis();

            // Check if task is overdue (due date before today) but not completed
            if (task.getDueDate() < todayStartTime && !task.isCompleted()) {
                holder.dueTextView.setTextColor(Color.RED);
            } else {
                holder.dueTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary));
            }
        } else {
            holder.dueTextView.setText("No due date");
            holder.dueTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary));
        }
        // Check if task is before current week
        boolean isHistoricalTask = task.getDueDate() < currentWeekStartTime;

        // Set priority color
        int color;
        switch (task.getPriority()) {
            case 2: // High
                color = ContextCompat.getColor(holder.itemView.getContext(), R.color.priority_high);
                break;
            case 1: // Medium
                color = ContextCompat.getColor(holder.itemView.getContext(), R.color.priority_medium);
                break;
            default: // Low
                color = ContextCompat.getColor(holder.itemView.getContext(), R.color.priority_low);
                break;
        }
        holder.priorityView.setBackgroundColor(color);

        // Set completed status
        holder.checkBox.setChecked(task.isCompleted());

        // TESTING ONLY - Comment or remove these lines after testing
        // Original line: holder.checkBox.setEnabled(!isHistoricalTask);
        // Now enabling all checkboxes for testing
        holder.checkBox.setEnabled(!isHistoricalTask); // REMOVE AFTER TESTING

        // Visual indicators for historical tasks
        if (isHistoricalTask) {
            holder.checkBox.setAlpha(0.7f); // TESTING: Changed from 0.5f to 0.7f for better visibility while testing
            holder.itemView.setBackgroundColor(Color.parseColor("#F5F5F5")); // Light gray

            // Add historical indicator
            holder.dueTextView.setText(holder.dueTextView.getText() + " (Historical)"); // Modified for testing
        } else {
            holder.checkBox.setAlpha(1.0f);
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        if (task.isCompleted()) {
            holder.titleTextView.setPaintFlags(holder.titleTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.titleTextView.setPaintFlags(holder.titleTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }


        // TESTING ONLY - Comment or remove these lines after testing
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed() && !isHistoricalTask && listener != null) {
                // TESTING: Removed !isHistoricalTask condition to allow historical task updates
                // Original condition: if (buttonView.isPressed() && !isHistoricalTask && listener != null)
                listener.onTaskCheckChanged(task, isChecked);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskDelete(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTasks(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView titleTextView;
        TextView descriptionTextView;
        TextView dueTextView;
        View priorityView;
        View deleteButton;

        TaskViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox_task);
            titleTextView = itemView.findViewById(R.id.text_task_title);
            descriptionTextView = itemView.findViewById(R.id.text_task_description);
            dueTextView = itemView.findViewById(R.id.text_task_due);
            priorityView = itemView.findViewById(R.id.view_priority);
            deleteButton = itemView.findViewById(R.id.btn_delete_task);
        }
    }
}