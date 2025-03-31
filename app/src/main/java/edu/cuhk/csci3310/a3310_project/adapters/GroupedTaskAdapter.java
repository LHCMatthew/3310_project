package edu.cuhk.csci3310.a3310_project.adapters;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.cuhk.csci3310.a3310_project.R;
import edu.cuhk.csci3310.a3310_project.adapters.TaskAdapter;
import edu.cuhk.csci3310.a3310_project.models.Task;
import edu.cuhk.csci3310.a3310_project.models.TaskGroup;
import edu.cuhk.csci3310.a3310_project.models.TaskWithList;

public class GroupedTaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<TaskGroup> items = new ArrayList<>();
    private TaskAdapter.OnTaskClickListener listener;

    public GroupedTaskAdapter(TaskAdapter.OnTaskClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TaskGroup.TYPE_HEADER) {
            View headerView = inflater.inflate(R.layout.item_list_header, parent, false);
            return new HeaderViewHolder(headerView);
        } else {
            View taskView = inflater.inflate(R.layout.item_task_with_list, parent, false);
            return new TaskViewHolder(taskView, listener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TaskGroup item = items.get(position);

        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(item.getListTitle());
        } else if (holder instanceof TaskViewHolder) {
            ((TaskViewHolder) holder).bind(item.getTaskWithList());
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    public void setItems(List<TaskGroup> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    // Find position of task by ID
    public int findTaskPosition(long taskId) {
        for (int i = 0; i < items.size(); i++) {
            TaskGroup item = items.get(i);
            if (item.getType() == TaskGroup.TYPE_TASK &&
                    item.getTaskWithList().getTask().getId() == taskId) {
                return i;
            }
        }
        return -1;
    }

    // ViewHolder for headers
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.text_list_title);
        }

        public void bind(String title) {
            titleTextView.setText(title);
        }
    }

    // ViewHolder for tasks
    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkBox;
        private TextView titleTextView;
        private TextView descriptionTextView;
        private TextView dueTextView;
        private View priorityView;
        private ImageView deleteButton;
        private TaskAdapter.OnTaskClickListener listener;

        public TaskViewHolder(@NonNull View itemView, TaskAdapter.OnTaskClickListener listener) {
            super(itemView);
            this.listener = listener;

            checkBox = itemView.findViewById(R.id.checkbox_task);
            titleTextView = itemView.findViewById(R.id.text_task_title);
            descriptionTextView = itemView.findViewById(R.id.text_task_description);
            dueTextView = itemView.findViewById(R.id.text_task_due);
            priorityView = itemView.findViewById(R.id.view_priority);
            deleteButton = itemView.findViewById(R.id.btn_delete_task);

            // Hide the list name text since we're showing it in the header
            TextView listTextView = itemView.findViewById(R.id.text_task_list);
            if (listTextView != null) {
                listTextView.setVisibility(View.GONE);
            }
        }

        public void bind(TaskWithList taskWithList) {
            Task task = taskWithList.getTask();

            titleTextView.setText(task.getTitle());
            descriptionTextView.setText(task.getDescription());

            SimpleDateFormat format = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            String dueText = "Due: " + format.format(new Date(task.getDueDate()));
            dueTextView.setText(dueText);

            // Set priority color
            int color;
            switch (task.getPriority()) {
                case 2: // High
                    color = ContextCompat.getColor(itemView.getContext(), R.color.priority_high);
                    break;
                case 1: // Medium
                    color = ContextCompat.getColor(itemView.getContext(), R.color.priority_medium);
                    break;
                default: // Low
                    color = ContextCompat.getColor(itemView.getContext(), R.color.priority_low);
                    break;
            }
            priorityView.setBackgroundColor(color);

            // Set completed state without triggering listener
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(task.isCompleted());

            // Strikethrough text if completed
            if (task.isCompleted()) {
                titleTextView.setPaintFlags(titleTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                titleTextView.setPaintFlags(titleTextView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }

            // Setup listeners
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onTaskCheckChanged(task, isChecked);
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(task);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskDelete(task);
                }
            });
        }
    }
}
