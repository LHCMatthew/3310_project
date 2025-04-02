package edu.cuhk.csci3310.a3310_project.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cuhk.csci3310.a3310_project.R;
import edu.cuhk.csci3310.a3310_project.adapters.GroupedTaskAdapter;
import edu.cuhk.csci3310.a3310_project.adapters.TaskAdapter;
import edu.cuhk.csci3310.a3310_project.database.TaskRepository;
import edu.cuhk.csci3310.a3310_project.models.Task;
import edu.cuhk.csci3310.a3310_project.models.TaskGroup;
import edu.cuhk.csci3310.a3310_project.models.TaskWithList;
import edu.cuhk.csci3310.a3310_project.reward.PointsManager;

public class TodayFragment extends Fragment implements TaskAdapter.OnTaskClickListener {
    private RecyclerView recyclerView;
    private GroupedTaskAdapter adapter;
    private List<TaskWithList> tasksWithList = new ArrayList<>();
    private TaskRepository taskRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_tasks);

        // Initialize repository
        taskRepository = new TaskRepository(requireContext());

        // Set title
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Today");
        }

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GroupedTaskAdapter(this);
        recyclerView.setAdapter(adapter);

        // Load tasks
        loadTodaysTasks();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTodaysTasks();
    }

    private void loadTodaysTasks() {
        // Get today's timestamps
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);

        long todayStart = today.getTimeInMillis();
        long todayEnd = tomorrow.getTimeInMillis() - 1;

        // Get tasks for today with list names
        List<TaskWithList> todayTasksWithList = new ArrayList<>();

        // Get tasks from all lists
        List<Task> tasks = taskRepository.getAllTasks();
        for (Task task : tasks) {
            long dueDate = task.getDueDate();
            if (dueDate >= todayStart && dueDate <= todayEnd) {
                // Get list name from repository using task's listId
                String listName = taskRepository.getListNameById(task.getListId());
                todayTasksWithList.add(new TaskWithList(task, listName));
            }
        }

        // Group tasks by list
        Map<String, List<TaskWithList>> tasksByList = new HashMap<>();
        for (TaskWithList taskWithList : todayTasksWithList) {
            String listName = taskWithList.getListName();
            if (!tasksByList.containsKey(listName)) {
                tasksByList.put(listName, new ArrayList<>());
            }
            tasksByList.get(listName).add(taskWithList);
        }

        // Create grouped items list
        List<TaskGroup> groupedItems = new ArrayList<>();
        for (Map.Entry<String, List<TaskWithList>> entry : tasksByList.entrySet()) {
            // Add header
            groupedItems.add(new TaskGroup(entry.getKey()));
            // Add tasks
            for (TaskWithList taskWithList : entry.getValue()) {
                groupedItems.add(new TaskGroup(taskWithList));
            }
        }

        // Save reference to update tasks
        this.tasksWithList = todayTasksWithList;

        // Update adapter
        adapter.setItems(groupedItems);
    }

    @Override
    public void onTaskCheckChanged(Task task, boolean isChecked) {
        // Update task completed status
        task.setCompleted(isChecked);

        // Set completion date if being marked as complete
        if (isChecked) {
            task.setCompletionDate(System.currentTimeMillis());

            // Process points for task completion
            PointsManager.processTaskCompletion(requireContext(), task);
        } else {
            task.setCompletionDate(0);
        }

        // Update in database
        taskRepository.updateTask(task);

        // Find and update the task in the tasksWithList collection
        for (TaskWithList item : tasksWithList) {
            if (item.getTask().getId() == task.getId()) {
                item.getTask().setCompleted(isChecked);
                if (isChecked) {
                    item.getTask().setCompletionDate(System.currentTimeMillis());
                } else {
                    item.getTask().setCompletionDate(0);
                }
                break;
            }
        }

        // Find position in adapter
        int position = adapter.findTaskPosition(task.getId());
        if (position != -1) {
            recyclerView.post(() -> adapter.notifyItemChanged(position));
        } else {
            // Fallback to reloading all data
            recyclerView.post(() -> loadTodaysTasks());
        }
    }

    @Override
    public void onTaskClick(Task task) {
        // Handle task click (implementation not shown)
    }

    @Override
    public void onTaskDelete(Task task) {
        // Delete task from database
        taskRepository.deleteTask(task.getId());

        // Reload the grouped tasks
        recyclerView.post(() -> loadTodaysTasks());

        // Show toast message
        Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
    }
}