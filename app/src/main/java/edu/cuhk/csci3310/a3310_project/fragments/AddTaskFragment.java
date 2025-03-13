package edu.cuhk.csci3310.a3310_project.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.cuhk.csci3310.a3310_project.R;
import edu.cuhk.csci3310.a3310_project.database.TaskRepository;
import edu.cuhk.csci3310.a3310_project.database.TodoListRepository;
import edu.cuhk.csci3310.a3310_project.models.Category;
import edu.cuhk.csci3310.a3310_project.models.Task;
import edu.cuhk.csci3310.a3310_project.models.TodoList;

public class AddTaskFragment extends Fragment {
    private EditText titleEditText;
    private EditText descriptionEditText;
    private AutoCompleteTextView listDropdown;
    private EditText dueDateInput;
    private RadioGroup priorityRadioGroup;
    private Button saveButton;
    private Button cancelButton;
    private long taskId = -1; // -1 means new task

    private TaskRepository taskRepository;
    private TodoListRepository listRepository;
    private List<TodoList> allLists = new ArrayList<>();
    private long selectedListId = -1;
    private long selectedDueDate = 0; // 0 means no due date

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_task, container, false);

        // Initialize repositories
        taskRepository = new TaskRepository(requireContext());
        listRepository = new TodoListRepository(requireContext());

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
            selectedListId = getArguments().getLong("listId", -1);
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

        // Load task data if editing existing task
        if (taskId != -1) {
            loadTaskData();
        }

        return view;
    }

    private void setupListDropdown() {
        // Define the list of items for the dropdown
        String[] lists = {"Work", "Personal", "Shopping", "Homework", "Others"};

        // Create an ArrayAdapter using the list and a default layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), // Context
                android.R.layout.simple_dropdown_item_1line, // Layout for dropdown items
                lists // List of items
        );

        // Set the adapter to the AutoCompleteTextView
        listDropdown.setAdapter(adapter);


        listDropdown.setOnItemClickListener((adapterView, view, position, id) -> {
            // Get the selected list ID
            if (position >= 0 && position < allLists.size()) {
                selectedListId = allLists.get(position).getId();
            }
        });


        // Optional: Set a threshold for showing suggestions
        listDropdown.setThreshold(1); // Show suggestions after typing 1 character
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        // If editing task with due date, set the calendar to that date
        if (selectedDueDate > 0) {
            calendar.setTimeInMillis(selectedDueDate);
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format the date for display
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    dueDateInput.setText(selectedDate);

                    // Store the date as milliseconds
                    Calendar selected = Calendar.getInstance();
                    selected.set(selectedYear, selectedMonth, selectedDay, 0, 0, 0);
                    selected.set(Calendar.MILLISECOND, 0);
                    selectedDueDate = selected.getTimeInMillis();
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void loadTaskData() {
        // Load the id of the task from database
        Task task = taskRepository.getTaskById(taskId);

        // Populate fields with task data
        titleEditText.setText(task.getTitle());
        descriptionEditText.setText(task.getDescription());

        // Set the selected list
        listDropdown.setText(task.getCategory().getName(), false);

        // Set due date if it exists
        selectedDueDate = task.getDueDate();
        if (selectedDueDate > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selectedDueDate);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1; // Month is 0-based
            int year = calendar.get(Calendar.YEAR);
            dueDateInput.setText(day + "/" + month + "/" + year);
        }

        // Set priority
        int priority = task.getPriority();
        switch (priority) {
            case 2:
                priorityRadioGroup.check(R.id.priority_high);
                break;
            case 1:
                priorityRadioGroup.check(R.id.priority_medium);
                break;
            case 0:
            default:
                priorityRadioGroup.check(R.id.priority_low);
                break;
        }
    }

    private void saveTask() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        int priority = getSelectedPriority();

        // Validate input
        if (title.isEmpty()) {
            titleEditText.setError("Title is required");
            return;
        }

        if (selectedListId == -1) {
            Toast.makeText(requireContext(), "Please select a list", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create or update task
        Task task;
        if (taskId == -1) {
            // Create new task
            task = new Task();
        } else {
            // Update existing task
            task = taskRepository.getTaskById(taskId);
            if (task == null) {
                task = new Task();
            }
        }

        // Set task properties
        task.setTitle(title);
        task.setDescription(description);
        task.setListId(selectedListId);
        task.setDueDate(selectedDueDate);
        task.setPriority(priority);
        String selectedCategoryText = listDropdown.getText().toString().trim();

        // Map displayed text to enum values
        Category selectedCategory = Category.OTHER; // Can be any default value
        for (Category category : Category.values()) {
            if (category.getName().equalsIgnoreCase(selectedCategoryText)) {
                selectedCategory = category;
                break;
            }
        }
        task.setCategory(selectedCategory);

        // Save to database
        if (taskId == -1) {
            long newTaskId = taskRepository.insertTask(task);
            if (newTaskId == -1) {
                Toast.makeText(requireContext(), "Error saving task", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            int rowsAffected = taskRepository.updateTask(task);
            if (rowsAffected == 0) {
                Toast.makeText(requireContext(), "Error updating task", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Toast.makeText(getContext(), "Changes saved successfully!", Toast.LENGTH_SHORT).show();

        // Go back to tasks list
        getParentFragmentManager().popBackStack();
    }
    private int getSelectedPriority() {
        int selectedId = priorityRadioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.priority_high) {
            return 2; // High
        } else if (selectedId == R.id.priority_medium) {
            return 1; // Medium
        } else {
            return 0; // Low (default)
        }
    }
}