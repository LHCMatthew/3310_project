package edu.cuhk.csci3310.a3310_project.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.cuhk.csci3310.a3310_project.R;
import edu.cuhk.csci3310.a3310_project.adapters.TaskAdapter;
import edu.cuhk.csci3310.a3310_project.database.TaskRepository;
import edu.cuhk.csci3310.a3310_project.models.Task;

import android.widget.Toast;

public class TodayFragment extends Fragment implements TaskAdapter.OnTaskClickListener {
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> tasks = new ArrayList<>();
    private TaskRepository taskRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_tasks);

        // Initialize repository
        taskRepository = new TaskRepository(requireContext());

        // Set title
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Today");
        }

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TaskAdapter(tasks, this);
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
        // Get today's start and end timestamps
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);

        long todayStart = today.getTimeInMillis();
        long todayEnd = tomorrow.getTimeInMillis() - 1;

        // Get all tasks and filter for today's tasks
        List<Task> allTasks = new ArrayList<>();
        List<Task> todaysTasks = new ArrayList<>();

        // Get tasks from all lists
        List<Task> tasks = taskRepository.getAllTasks();
        for (Task task : tasks) {
            long dueDate = task.getDueDate();
            if (dueDate >= todayStart && dueDate <= todayEnd) {
                todaysTasks.add(task);
            }
        }

        // Update adapter
        this.tasks.clear();
        this.tasks.addAll(todaysTasks);
        adapter.updateTasks(this.tasks);
    }

    @Override
    public void onTaskClick(Task task) {
        // Open task edit fragment
        Bundle args = new Bundle();
        args.putLong("taskId", task.getId());
        args.putLong("listId", task.getListId());

        AddTaskFragment addTaskFragment = new AddTaskFragment();
        addTaskFragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, addTaskFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onTaskCheckChanged(Task task, boolean isChecked) {
        // Update task completed status
        task.setCompleted(isChecked);
        taskRepository.updateTask(task);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onTaskDelete(Task task) {
        // Delete task from database
        taskRepository.deleteTask(task.getId());
    
        // Update UI
        tasks.remove(task);
        adapter.notifyDataSetChanged();
    
        // Show toast message
        Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
    }
}
