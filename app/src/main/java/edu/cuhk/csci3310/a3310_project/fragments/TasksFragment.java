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
    private List tasks = new ArrayList<>();
    private long listId;
    private String listTitle;
    private TaskRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_tasks);

        // Initialize repository upon startup
        repository = new TaskRepository(getContext());

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

    // Reload tasks when loaded back to this fragment
    @Override
    public void onResume() {
        super.onResume();
        // Reload tasks when coming back to this fragment
        loadTasks();
    }

    private void loadTasks() {
        // In a real app, load from database
        // For demo, use dummy data
        tasks.clear();
        tasks.addAll(repository.getTasksByListId(listId));
        adapter.updateTasks(tasks);
    }

    //This has logical issues, should create one more fragment to show the task details, addTaskFragment
    //should only be used to create or edit tasks
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

        // Update in database
        repository.updateTask(task);

        // Update UI
        adapter.notifyDataSetChanged();
    }
}
