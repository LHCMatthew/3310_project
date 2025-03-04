package edu.cuhk.csci3310.a3310_project;

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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> tasks;
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(edu.cuhk.csci3310.a3310_project.Task task);
        void onTaskCheckChanged(edu.cuhk.csci3310.a3310_project.Task task, boolean isChecked);
    }

    public TaskAdapter(List<Task> tasks, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
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
        edu.cuhk.csci3310.a3310_project.Task task = tasks.get(position);
        holder.titleTextView.setText(task.getTitle());
        holder.descriptionTextView.setText(task.getDescription());

        // Format date
        if (task.getDueDate() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            String dueDate = "Due: " + sdf.format(new Date(task.getDueDate()));
            holder.dueTextView.setText(dueDate);
        } else {
            holder.dueTextView.setText("No due date");
        }

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
        if (task.isCompleted()) {
            holder.titleTextView.setPaintFlags(holder.titleTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.titleTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_muted));
        } else {
            holder.titleTextView.setPaintFlags(holder.titleTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.titleTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
        }

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onTaskCheckChanged(task, isChecked);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task);
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

        TaskViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox_task);
            titleTextView = itemView.findViewById(R.id.text_task_title);
            descriptionTextView = itemView.findViewById(R.id.text_task_description);
            dueTextView = itemView.findViewById(R.id.text_task_due);
            priorityView = itemView.findViewById(R.id.view_priority);
        }
    }
}
