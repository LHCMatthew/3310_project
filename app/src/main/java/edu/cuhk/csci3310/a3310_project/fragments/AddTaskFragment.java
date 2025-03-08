package edu.cuhk.csci3310.a3310_project.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.Calendar;

import edu.cuhk.csci3310.a3310_project.R;

public class AddTaskFragment extends Fragment {
    private EditText titleEditText;
    private EditText descriptionEditText;
    private AutoCompleteTextView listDropdown;
    private EditText dueDateInput;
    private RadioGroup priorityRadioGroup;
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
        listDropdown = view.findViewById(R.id.list_dropdown);
        dueDateInput = view.findViewById(R.id.due_date_input);
        priorityRadioGroup = view.findViewById(R.id.priority_radio_group);
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

        // Setup list dropdown
        setupListDropdown();

        // Setup due date picker
        dueDateInput.setOnClickListener(v -> showDatePicker());

        // Setup button listeners
        saveButton.setOnClickListener(v -> saveTask());
        cancelButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
    }

    private void setupListDropdown() {
        // Define the list of items for the dropdown
        String[] lists = {"Work", "Personal", "Shopping", "Homework"};

        // Create an ArrayAdapter using the list and a default layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), // Context
                android.R.layout.simple_dropdown_item_1line, // Layout for dropdown items
                lists // List of items
        );

        // Set the adapter to the AutoCompleteTextView
        listDropdown.setAdapter(adapter);


        listDropdown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
            }
        });


        // Optional: Set a threshold for showing suggestions
        listDropdown.setThreshold(1); // Show suggestions after typing 1 character
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    dueDateInput.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void loadTaskData() {
        // In a real app, load from database
        // For demo, just set some dummy data
        titleEditText.setText("Example Task");
        descriptionEditText.setText("Example Description");
        listDropdown.setText("Work");
        dueDateInput.setText("05/03/2025");

        // Set priority based on task data
        int priority = 2; // Example: High priority
        switch (priority) {
            case 2:
                priorityRadioGroup.check(R.id.priority_high);
                break;
            case 1:
                priorityRadioGroup.check(R.id.priority_medium);
                break;
            case 0:
                priorityRadioGroup.check(R.id.priority_low);
                break;
        }
    }

    private void saveTask() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String list = listDropdown.getText().toString().trim();
        String dueDate = dueDateInput.getText().toString().trim();
        int priority = getSelectedPriority(); // Get selected priority

        if (title.isEmpty()) {
            titleEditText.setError("Title is required");
            return;
        }

        // In a real app, save to database

        // Go back
        getParentFragmentManager().popBackStack();
    }

    private int getSelectedPriority() {
        int selectedId = priorityRadioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.priority_high) {
            return 2; // High
        } else if (selectedId == R.id.priority_medium) {
            return 1; // Medium
        } else if (selectedId == R.id.priority_low) {
            return 0; // Low
        } else {
            return -1; // No priority selected
        }
    }
}