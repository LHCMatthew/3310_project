package edu.cuhk.csci3310.a3310_project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class AddTaskFragment extends Fragment {
    private EditText titleEditText;
    private EditText descriptionEditText;
    private Button saveButton;
    private Button cancelButton;
    private long taskId = -1; // -1 means new task

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_task, container, false);

        // Initialize views
        titleEditText = view.findViewById(R.id.edit_task_title);
        descriptionEditText = view.findViewById(R.id.edit_task_description);
        saveButton = view.findViewById(R.id.button_save);
        cancelButton = view.findViewById(R.id.button_cancel);

        // Get arguments
        if (getArguments() != null) {
            taskId = getArguments().getLong("taskId", -1);
        }

        // Set title
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar()
                    .setTitle(taskId == -1 ? "New Task" : "Edit Task");
        }

        // Load task data if editing existing task
        if (taskId != -1) {
            loadTaskData();
        }

        // Setup button listeners
        saveButton.setOnClickListener(v -> saveTask());
        cancelButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
    }

    private void loadTaskData() {
        // In a real app, load from database
        // For demo, just set some dummy data
        titleEditText.setText("Example Task");
        descriptionEditText.setText("Example Description");
    }

    private void saveTask() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        if (title.isEmpty()) {
            titleEditText.setError("Title is required");
            return;
        }

        // In a real app, save to database

        // Go back
        getParentFragmentManager().popBackStack();
    }
}