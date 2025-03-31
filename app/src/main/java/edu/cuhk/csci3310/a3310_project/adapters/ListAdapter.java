package edu.cuhk.csci3310.a3310_project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

import edu.cuhk.csci3310.a3310_project.R;
import edu.cuhk.csci3310.a3310_project.database.TaskRepository;
import edu.cuhk.csci3310.a3310_project.models.Task;
import edu.cuhk.csci3310.a3310_project.models.TodoList;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {
    private List<TodoList> lists;
    private OnListClickListener listener;
    private TaskRepository taskRepository;

    public interface OnListClickListener {
        void onListClick(TodoList todoList);
    }

    public ListAdapter(List<TodoList> lists, OnListClickListener listener, TaskRepository taskRepository) {
        this.lists = lists;
        this.listener = listener;
        this.taskRepository = taskRepository;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list, parent, false);
        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        TodoList todoList = lists.get(position);
        holder.titleTextView.setText(todoList.getTitle());

        // Get count of tasks for this week only
        int thisWeekTaskCount = getThisWeekTaskCount(todoList.getId());
        holder.countTextView.setText(String.valueOf(thisWeekTaskCount));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onListClick(todoList);
            }
        });
    }

    private int getThisWeekTaskCount(long listId) {
        // Get all tasks for this list
        List<Task> allTasks = taskRepository.getTasksByListId(listId);

        // Calculate this week's boundaries (Sunday to Saturday)
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Roll back to Sunday
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        calendar.add(Calendar.DAY_OF_MONTH, -(dayOfWeek - Calendar.SUNDAY));
        long startOfWeek = calendar.getTimeInMillis();

        // Roll forward to Saturday
        calendar.add(Calendar.DAY_OF_MONTH, 6);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        long endOfWeek = calendar.getTimeInMillis();

        // Count tasks for this week only
        int count = 0;
        for (Task task : allTasks) {
            if (task.getDueDate() >= startOfWeek && task.getDueDate() <= endOfWeek) {
                count++;
            }
        }

        return count;
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    public void updateLists(List<TodoList> newLists) {
        this.lists = newLists;
        notifyDataSetChanged();
    }

    static class ListViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView countTextView;

        ListViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.text_list_title);
            countTextView = itemView.findViewById(R.id.text_list_count);
        }
    }
}