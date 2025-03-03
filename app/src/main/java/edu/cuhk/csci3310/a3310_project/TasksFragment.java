package edu.cuhk.csci3310.a3310_project;

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
import java.util.List;

public class TasksFragment extends Fragment implements TaskAdapter.OnTaskClickListener {
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List tasks = new ArrayList<>();
    private long listId;
    private String listTitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_tasks);

        // Get arguments
        if (getArguments() != null) {
            listId = getArguments().getLong("listId");
            listTitle = getArguments().getString("listTitle", "Tasks");
        }

        // Update toolbar title
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(listTitle);
        }

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TaskAdapter(tasks, this);
        recyclerView.setAdapter(adapter);

        // Load data
        loadTasks();

        return view;
    }

    private void loadTasks() {
        // In a real app, load from database
        // For demo, use dummy data
        tasks.clear();

        // Create dummy tasks based on listId
        if (listId == 1) { // Work list
            tasks.add(new edu.cuhk.csci3310.a3310_project.Task(1, "Finish project proposal", "Complete the document",
                    listId, System.currentTimeMillis() + 86400000 * 3, 2, false));
            tasks.add(new edu.cuhk.csci3310.a3310_project.Task(2, "Team meeting", "Discuss project timeline",
                    listId, System.currentTimeMillis() - 86400000, 1, true));
            tasks.add(new edu.cuhk.csci3310.a3310_project.Task(3, "Review code changes", "Check PR #42",
                    listId, System.currentTimeMillis(), 1, false));
            tasks.add(new edu.cuhk.csci3310.a3310_project.Task(4, "Update documentation", "Update API docs",
                    listId, System.currentTimeMillis() + 86400000 * 6, 0, false));
        } else {
            // Add other dummy tasks for other lists
            tasks.add(new edu.cuhk.csci3310.a3310_project.Task(5, "Example task for " + listTitle, "Description",
                    listId, System.currentTimeMillis(), 1, false));
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onTaskClick(edu.cuhk.csci3310.a3310_project.Task task) {
        // Open task edit fragment
        Bundle args = new Bundle();
        args.putLong("taskId", task.getId());

        AddTaskFragment addTaskFragment = new AddTaskFragment();
        addTaskFragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, addTaskFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onTaskCheckChanged(edu.cuhk.csci3310.a3310_project.Task task, boolean isChecked) {
        // Update task completed status
        task.setCompleted(isChecked);
        // In a real app, update database
        adapter.notifyDataSetChanged();
    }
}
