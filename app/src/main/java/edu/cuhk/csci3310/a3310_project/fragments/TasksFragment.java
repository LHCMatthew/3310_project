package edu.cuhk.csci3310.a3310_project.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Calendar;
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
    private Spinner timeFilterSpinner;
    private int selectedTimeFilter = 0; // 0 = This Week, 1 = Previous Week, 2 = Previous Month, 3 = All Tasks

    private TabLayout tabLayout;
    private View currentTasksContainer;
    private View historyContainer;
    private CalendarView calendarView;
    private RecyclerView historyRecyclerView;
    private TaskAdapter historyAdapter;
    private List<Task> historyTasks = new ArrayList<>();
    private TextView historyHeader;
    private long selectedCalendarDate = Calendar.getInstance().getTimeInMillis(); // Store selected date for persisting state

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        // Initialize existing views
        recyclerView = view.findViewById(R.id.recycler_view_tasks);
        timeFilterSpinner = view.findViewById(R.id.spinner_time_filter);

        // Initialize new views
        tabLayout = view.findViewById(R.id.tab_layout);
        currentTasksContainer = view.findViewById(R.id.current_tasks_container);
        historyContainer = view.findViewById(R.id.history_container);
        calendarView = view.findViewById(R.id.calendar_view);
        historyRecyclerView = view.findViewById(R.id.recycler_view_history_tasks);
        historyHeader = view.findViewById(R.id.history_header);

        // Initialize repository
        taskRepository = new TaskRepository(requireContext());

        // Get arguments
        if (getArguments() != null) {
            listId = getArguments().getLong("listId");
            listTitle = getArguments().getString("listTitle", "Tasks");
        }

        // Set title
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(listTitle);
        }

        // Setup tabs
        setupTabs();

        // Setup time filter
        setupTimeFilter();

        // Setup RecyclerView for current tasks
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TaskAdapter(tasks, this);
        recyclerView.setAdapter(adapter);

        // Setup RecyclerView for history tasks
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        historyAdapter = new TaskAdapter(historyTasks, this);
        historyRecyclerView.setAdapter(historyAdapter);

        // Setup calendar view
        setupCalendarView();

        // Load tasks
        loadTasks();

        return view;
    }

    private void setupTabs() {
        // Add tabs
        tabLayout.addTab(tabLayout.newTab().setText("Current Tasks"));
        tabLayout.addTab(tabLayout.newTab().setText("Task History"));

        // Set tab listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        // Show current tasks
                        currentTasksContainer.setVisibility(View.VISIBLE);
                        historyContainer.setVisibility(View.GONE);
                        break;
                    case 1:
                        // Show history view
                        currentTasksContainer.setVisibility(View.GONE);
                        historyContainer.setVisibility(View.VISIBLE);
                        loadSelectedDateTasks(selectedCalendarDate);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not needed
            }
        });
    }

    private void setupCalendarView() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);

            // Store the selected date
            selectedCalendarDate = cal.getTimeInMillis();

            // Update the date in the header
            String dateStr = new java.text.SimpleDateFormat("EEEE, MMMM d, yyyy",
                    java.util.Locale.getDefault()).format(cal.getTime());
            historyHeader.setText("Tasks for " + dateStr);

            // Load tasks for selected date
            loadSelectedDateTasks(selectedCalendarDate);
        });

        // Mark dates with tasks
        markDatesWithTasks();
    }

    private void markDatesWithTasks() {
        // Get all tasks for this list
        List<Task> allTasks = taskRepository.getTasksByListId(listId);

        // TODO: In a production app, you would decorate the calendar with dates
        // that have tasks. This requires a custom calendar implementation.
        // For simplicity, we'll skip this visual enhancement.
    }

    private void loadSelectedDateTasks(long dateInMillis) {
        // Get all tasks for this list
        List<Task> allTasks = taskRepository.getTasksByListId(listId);

        // Convert date to start/end of day
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateInMillis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfDay = cal.getTimeInMillis();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        long endOfDay = cal.getTimeInMillis();

        // Filter tasks for selected date
        List<Task> tasksForDate = new ArrayList<>();
        for (Task task : allTasks) {
            long taskDate = task.getDueDate();
            if (taskDate >= startOfDay && taskDate <= endOfDay) {
                tasksForDate.add(task);
            }
        }

        // Update the history adapter
        historyTasks.clear();
        historyTasks.addAll(tasksForDate);
        historyAdapter.updateTasks(historyTasks);
    }

    private void setupTimeFilter() {
        // Create time period options
        String[] timePeriods = {"This Week", "Previous Week", "Previous Month", "All Tasks"};

        // Create adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                timePeriods
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set adapter and listener
        timeFilterSpinner.setAdapter(adapter);
        timeFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTimeFilter = position;
                loadTasks();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTimeFilter = 0; // Default to This Week
            }
        });
    }

    private void loadTasks() {
        // Load all tasks from database
        List<Task> loadedTasks = taskRepository.getTasksByListId(listId);

        // Calculate date boundaries based on selected filter
        long startTime = 0;
        long endTime = Long.MAX_VALUE;

        Calendar calendar = Calendar.getInstance();

        switch (selectedTimeFilter) {
            case 0: // This Week
                // Set to start of current week (Sunday)
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                calendar.add(Calendar.DAY_OF_MONTH, -(dayOfWeek - Calendar.SUNDAY));
                startTime = calendar.getTimeInMillis();

                // Set to end of current week (Saturday)
                calendar.add(Calendar.DAY_OF_MONTH, 6);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                endTime = calendar.getTimeInMillis();
                break;

            case 1: // Previous Week
                // Set to start of previous week (Sunday)
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                calendar.add(Calendar.DAY_OF_MONTH, -(dayOfWeek - Calendar.SUNDAY));
                calendar.add(Calendar.WEEK_OF_YEAR, -1);
                startTime = calendar.getTimeInMillis();

                // Set to end of previous week (Saturday)
                calendar.add(Calendar.DAY_OF_MONTH, 6);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                endTime = calendar.getTimeInMillis();
                break;

            case 2: // Previous Month
                // Set to start of previous month
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.add(Calendar.MONTH, -1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startTime = calendar.getTimeInMillis();

                // Set to end of previous month
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                endTime = calendar.getTimeInMillis();
                break;

            case 3: // All Tasks
                // No filtering by date range
                startTime = 0;
                endTime = Long.MAX_VALUE;
                break;
        }

        // Filter tasks based on selected time period
        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : loadedTasks) {
            if (selectedTimeFilter == 3 || (task.getDueDate() >= startTime && task.getDueDate() <= endTime)) {
                filteredTasks.add(task);
            }
        }

        // Update adapter with filtered tasks
        this.tasks.clear();
        this.tasks.addAll(filteredTasks);
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