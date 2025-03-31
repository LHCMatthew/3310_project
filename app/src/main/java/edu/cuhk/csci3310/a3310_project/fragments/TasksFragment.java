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
import java.util.List;

import edu.cuhk.csci3310.a3310_project.R;
import edu.cuhk.csci3310.a3310_project.adapters.TaskAdapter;
import edu.cuhk.csci3310.a3310_project.database.TaskRepository;
import edu.cuhk.csci3310.a3310_project.models.Task;

public class TasksFragment extends Fragment implements TaskAdapter.OnTaskClickListener {
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> tasks = new ArrayList<>();
    private long listId;
    private String listTitle;
    private TaskRepository taskRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_tasks);

        // Initialize repository
        taskRepository = new TaskRepository(requireContext());

        // Get arguments
        if (getArguments() != null) {
            listId = getArguments().getLong("listId");
            listTitle = getArguments().getString("listTitle", "Tasks");
        }

        // Set title
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("List: " + listTitle);
        }

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TaskAdapter(tasks, this);
        recyclerView.setAdapter(adapter);

        // Load tasks
        loadTasks();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks();
    }

    private void loadTasks() {
        // Load tasks from database
        List<Task> loadedTasks = taskRepository.getTasksByListId(listId);

        // Update adapter
        this.tasks.clear();
        this.tasks.addAll(loadedTasks);
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

        // Set completion date when completed
        if (isChecked) {
            task.setCompletionDate(System.currentTimeMillis());
        } else {
            task.setCompletionDate(0); // Reset completion date when unchecked
        }

        // Update in database
        taskRepository.updateTask(task);

        // Update the local list first
        for (Task t : tasks) {
            if (t.getId() == task.getId()) {
                t.setCompleted(isChecked);
                if (isChecked) {
                    t.setCompletionDate(System.currentTimeMillis());
                } else {
                    t.setCompletionDate(0);
                }
                break;
            }
        }

        // Find the position of the changed task
        int position = -1;
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId() == task.getId()) {
                position = i;
                break;
            }
        }

        // Only update the specific item that changed
        final int finalPosition = position;
        if (finalPosition != -1) {
            recyclerView.post(() -> adapter.notifyItemChanged(finalPosition));
        } else {
            // Fallback to reloading all data
            recyclerView.post(() -> loadTasks());
        }
    }

    @Override
    public void onTaskDelete(Task task) {
        // Delete task from database
        taskRepository.deleteTask(task.getId());

        // Remove from list and update UI
        tasks.remove(task);
        adapter.notifyDataSetChanged();

        // Show toast message
        Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
    }
}