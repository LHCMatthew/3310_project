package edu.cuhk.csci3310.a3310_project.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.cuhk.csci3310.a3310_project.R;
import edu.cuhk.csci3310.a3310_project.database.TaskRepository;
import edu.cuhk.csci3310.a3310_project.database.TodoListRepository;
import edu.cuhk.csci3310.a3310_project.models.Category;
import edu.cuhk.csci3310.a3310_project.models.Task;
import edu.cuhk.csci3310.a3310_project.models.TodoList;
import edu.cuhk.csci3310.a3310_project.notification.NotificationScheduler;

public class AddTaskFragment extends Fragment {
    private EditText titleEditText;
    private EditText descriptionEditText;
    private AutoCompleteTextView listDropdown;
    private TextView dueDateInput, startTimeInput, endTimeInput;
    private Switch allDaySwitch;
    private long selectedStartTime;
    private long selectedEndTime;
    private RadioGroup priorityRadioGroup;
    private Button saveButton;
    private Button cancelButton;
    private long taskId = -1; // -1 means new task

    private TaskRepository taskRepository;
    private TodoListRepository listRepository;
    private List<TodoList> allLists = new ArrayList<>();
    private long selectedListId = -1;
    private long selectedDueDate = 0; // 0 means no due date
    private TextView reminderTimeText;
    private long reminderTimeOffset = 0; // in minutes before due date

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
        dueDateInput = view.findViewById(R.id.input_due_date);
        startTimeInput = view.findViewById(R.id.input_start_time);
        endTimeInput = view.findViewById(R.id.input_end_time);
        allDaySwitch = view.findViewById(R.id.switch_all_day);
        priorityRadioGroup = view.findViewById(R.id.priority_radio_group);
        saveButton = view.findViewById(R.id.button_save);
        cancelButton = view.findViewById(R.id.button_cancel);
        reminderTimeText = view.findViewById(R.id.reminder_time_text);
        reminderTimeText.setOnClickListener(v -> showReminderOptions());


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
        startTimeInput.setOnClickListener(v -> showTimePicker(true));
        endTimeInput.setOnClickListener(v -> showTimePicker(false));

        allDaySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            startTimeInput.setEnabled(!isChecked);
            endTimeInput.setEnabled(!isChecked);
            if (isChecked) {
                startTimeInput.setText("All day");
                endTimeInput.setText("All day");
            } else {
                updateTimeDisplay();
            }
        });

        // Setup button listeners
        saveButton.setOnClickListener(v -> saveTask());
        cancelButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Load task data if editing existing task
        if (taskId != -1) {
            loadTaskData();
        }

        return view;
    }

    private void showReminderOptions() {
        if (selectedDueDate == 0) {
            Toast.makeText(getContext(), "Please select a due date first", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] options = {"None", "5 minutes before", "15 minutes before", "30 minutes before",
                "1 hour before", "2 hours before", "1 day before", "Custom"};

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Set Reminder")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // None
                            reminderTimeOffset = 0;
                            reminderTimeText.setText("None");
                            break;
                        case 1: // 5 minutes
                            reminderTimeOffset = 5;
                            reminderTimeText.setText("5 minutes before");
                            break;
                        case 2: // 15 minutes
                            reminderTimeOffset = 15;
                            reminderTimeText.setText("15 minutes before");
                            break;
                        case 3: // 30 minutes
                            reminderTimeOffset = 30;
                            reminderTimeText.setText("30 minutes before");
                            break;
                        case 4: // 1 hour
                            reminderTimeOffset = 60;
                            reminderTimeText.setText("1 hour before");
                            break;
                        case 5: // 2 hours
                            reminderTimeOffset = 120;
                            reminderTimeText.setText("2 hours before");
                            break;
                        case 6: // 1 day
                            reminderTimeOffset = 1440;
                            reminderTimeText.setText("1 day before");
                            break;
                        case 7: // Custom
                            showCustomReminderDialog();
                            break;
                    }
                });
        builder.create().show();
    }

    private void showCustomReminderDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom_reminder, null);
        EditText hoursInput = dialogView.findViewById(R.id.hours_input);
        EditText minutesInput = dialogView.findViewById(R.id.minutes_input);

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Custom Reminder")
                .setView(dialogView)
                .setPositiveButton("Set", (dialog, which) -> {
                    try {
                        int hours = hoursInput.getText().toString().isEmpty() ? 0 :
                                Integer.parseInt(hoursInput.getText().toString());
                        int minutes = minutesInput.getText().toString().isEmpty() ? 0 :
                                Integer.parseInt(minutesInput.getText().toString());

                        if (hours == 0 && minutes == 0) {
                            Toast.makeText(getContext(), "Please enter a valid time", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        reminderTimeOffset = hours * 60 + minutes;
                        String displayText = "";
                        if (hours > 0) {
                            displayText += hours + " hour" + (hours > 1 ? "s" : "");
                        }
                        if (minutes > 0) {
                            if (!displayText.isEmpty()) displayText += " ";
                            displayText += minutes + " minute" + (minutes > 1 ? "s" : "");
                        }
                        reminderTimeText.setText(displayText + " before");
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
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

    private void showTimePicker(boolean isStartTime) {
        if (selectedDueDate == 0) {
            Toast.makeText(getContext(), "Please select a date first", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar calendar = Calendar.getInstance();
        if (isStartTime && selectedStartTime > 0) {
            calendar.setTimeInMillis(selectedStartTime);
        } else if (!isStartTime && selectedEndTime > 0) {
            calendar.setTimeInMillis(selectedEndTime);
        }

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, selectedMinute) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.setTimeInMillis(selectedDueDate);
                    selected.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selected.set(Calendar.MINUTE, selectedMinute);

                    if (isStartTime) {
                        selectedStartTime = selected.getTimeInMillis();
                        if (selectedEndTime == 0) {
                            // Set end time to 1 hour after start time by default
                            Calendar endTime = (Calendar) selected.clone();
                            endTime.add(Calendar.HOUR_OF_DAY, 1);
                            selectedEndTime = endTime.getTimeInMillis();
                        }
                    } else {
                        selectedEndTime = selected.getTimeInMillis();
                    }
                    updateTimeDisplay();
                },
                hour,
                minute,
                true
        );
        timePickerDialog.show();
    }

    private void updateTimeDisplay() {
        // Update the time display based on selected start and end times
        if (selectedStartTime > 0) {
            // Check if start time is valid, it cannot be in the past
            if(selectedStartTime+ 60*1000 < System.currentTimeMillis()){
                Toast.makeText(getContext(), "Start time must be later than current time, please select again", Toast.LENGTH_SHORT).show();
                selectedStartTime = 0;
                selectedEndTime = 0;
                startTimeInput.setText("");
                endTimeInput.setText("");
                return;
            }
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            startTimeInput.setText(timeFormat.format(new Date(selectedStartTime)));
            // Check if end time is valid
            if (selectedEndTime >= selectedStartTime && selectedEndTime > 0) {
                endTimeInput.setText(timeFormat.format(new Date(selectedEndTime)));
            }
            // Reset the end time if it is invalid
            else{
                Toast.makeText(getContext(), "End time must be later than start time, please select again", Toast.LENGTH_SHORT).show();
                selectedStartTime = 0;
                selectedEndTime = 0;
                startTimeInput.setText("");
                endTimeInput.setText("");
            }
        }
        // Reset the time display if start time is not set, -1 means both time are not set
        else{
            startTimeInput.setText("");
            endTimeInput.setText("");
        }
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

        selectedStartTime = task.getStartTime();
        selectedEndTime = task.getEndTime();
        if(task.isAllday()) {
            allDaySwitch.setChecked(true);
            startTimeInput.setEnabled(false);
            endTimeInput.setEnabled(false);
            startTimeInput.setText("All day");
            endTimeInput.setText("All day");
        }
        else if(selectedStartTime > 0 && selectedEndTime > 0) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            startTimeInput.setText(timeFormat.format(new Date(selectedStartTime)));
            endTimeInput.setText(timeFormat.format(new Date(selectedEndTime)));
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

        if (task.getReminderTime() > 0) {
            reminderTimeOffset = task.getReminderTime();

            // Format the reminder time display
            long minutes = reminderTimeOffset;

            String displayText = "";
            if (minutes > 0) {
                displayText += minutes + " minute" + (minutes > 1 ? "s" : "");
            }
            reminderTimeText.setText(displayText + " before");
        } else {
            reminderTimeText.setText("None");
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

        // Add this validation for due date
        if (selectedDueDate == 0) {
            Toast.makeText(requireContext(), "Please select a date", Toast.LENGTH_SHORT).show();
            // Highlight the due date field with error styling
            dueDateInput.setBackgroundResource(R.drawable.error_background);
            return;
        } else {
            // Reset background if validation passes
            dueDateInput.setBackgroundResource(android.R.drawable.edit_text);
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
        task.setAllday(allDaySwitch.isChecked());
        String selectedCategoryText = listDropdown.getText().toString().trim();
        task.setReminderTime(reminderTimeOffset);

        if(selectedStartTime > 0 && selectedEndTime > 0) {
            task.setStartTime(selectedStartTime);
            task.setEndTime(selectedEndTime);
        }

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

        // Schedule notification for the task
        NotificationScheduler.scheduleTaskReminder(requireContext(), task);

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