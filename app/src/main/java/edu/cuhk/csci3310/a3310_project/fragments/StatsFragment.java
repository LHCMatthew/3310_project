package edu.cuhk.csci3310.a3310_project.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.components.Legend;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;

import edu.cuhk.csci3310.a3310_project.R;
import edu.cuhk.csci3310.a3310_project.database.TaskRepository;
import edu.cuhk.csci3310.a3310_project.models.Category;
import edu.cuhk.csci3310.a3310_project.models.Task;
import edu.cuhk.csci3310.a3310_project.database.TodoListRepository;
import edu.cuhk.csci3310.a3310_project.models.TodoList;
import edu.cuhk.csci3310.a3310_project.reward.PointsManager;

public class StatsFragment extends Fragment {

    private TextView completionRateTextView;
    private ProgressBar completionProgressBar;
    private ViewGroup chartContainerWeekly;
    private TaskRepository taskRepository;
    private Spinner listFilterSpinner;
    private List<TodoList> todoLists = new ArrayList<>();
    private TodoListRepository listRepository;
    private long selectedListId = -1; // -1 means "All"
    private ViewGroup chartContainerCategory;
    private Spinner timeFilterSpinner;
    private int selectedTimePeriod = 0; // 0 = Today, 1 = Last Week, 2 = Last Month
    private TextView pointsTextView;
    private TextView tasksOnTimeTextView;
    private TextView tasksLateTextView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        // Set title
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Statistics");
        }

        // Initialize views
        completionRateTextView = view.findViewById(R.id.text_completion_rate);
        completionProgressBar = view.findViewById(R.id.progress_completion);
        chartContainerWeekly = view.findViewById(R.id.chart_container_weekly);
        chartContainerCategory = view.findViewById(R.id.chart_container_category);

        // Initialize repository
        taskRepository = new TaskRepository(requireContext());

        // Initialize filter spinner
        listFilterSpinner = view.findViewById(R.id.spinner_list_filter);
        listRepository = new TodoListRepository(requireContext());
        setupListFilter();

        // Initialize time filter spinner
        timeFilterSpinner = view.findViewById(R.id.spinner_time_filter);
        setupTimeFilter();

        // Initialize points-related views
        pointsTextView = view.findViewById(R.id.text_points);
        tasksOnTimeTextView = view.findViewById(R.id.text_tasks_on_time);
        tasksLateTextView = view.findViewById(R.id.text_tasks_late);

        loadStats();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStats();
        updatePointsStats();
    }

    private void loadStats() {
        List<Task> allTasks = taskRepository.getAllTasks();
        if (allTasks == null) {
            allTasks = new ArrayList<>();
        }

        // Filter tasks based on selected list if needed
        List<Task> filteredTasks = new ArrayList<>();

        if (selectedListId == -1) {
            // "All" selected, use all tasks
            filteredTasks.addAll(allTasks);
        } else {
            // Filter by selected list ID
            for (Task task : allTasks) {
                if (task.getListId() == selectedListId) {
                    filteredTasks.add(task);
                }
            }
        }

        // Use filtered tasks for stats
        updateCompletionRate(filteredTasks);
        createWeeklyChart(filteredTasks);
        createCategoryPieChart(filteredTasks);
    }

    private void updatePointsStats() {
        if (getContext() == null) return;

        int points = PointsManager.getPoints(requireContext());
        int tasksOnTime = PointsManager.getTasksCompletedOnTime(requireContext());
        int tasksLate = PointsManager.getTasksCompletedLate(requireContext());

        // Update UI if views exist
        if (pointsTextView != null) {
            pointsTextView.setText("Total Points: " + points);
        }

        if (tasksOnTimeTextView != null) {
            tasksOnTimeTextView.setText("On Time: " + tasksOnTime);
        }

        if (tasksLateTextView != null) {
            tasksLateTextView.setText("Late: " + tasksLate);
        }
    }

    private void setupListFilter() {
        // Get all lists from repository
        todoLists = listRepository.getAllLists();
        if (todoLists == null) {
            todoLists = new ArrayList<>();
        }

        // Create list names with "All" option
        List<String> listNames = new ArrayList<>();
        listNames.add("All");

        for (TodoList list : todoLists) {
            listNames.add(list.getTitle());
        }
        // Create adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                listNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set adapter and listener
        listFilterSpinner.setAdapter(adapter);
        listFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // "All" selected
                    selectedListId = -1;
                } else if (position - 1 < todoLists.size()) {
                    // Specific list selected with bounds check
                    selectedListId = todoLists.get(position - 1).getId();
                } else {
                    // Fallback if position is out of bounds
                    selectedListId = -1;
                }
                // Refresh stats with selected filter
                loadStats();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedListId = -1;
            }
        });
    }

    private void updateCompletionRate(List<Task> allTasks) {
        // Get today's date range
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long todayStart = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long todayEnd = calendar.getTimeInMillis();

        // Filter tasks due today
        List<Task> todayTasks = new ArrayList<>();
        for (Task task : allTasks) {
            if (task.getDueDate() >= todayStart && task.getDueDate() <= todayEnd) {
                todayTasks.add(task);
            }
        }

        // Update UI text to reflect today's focus - with null check
        View view = getView();
        if (view != null) {
            TextView titleTextView = view.findViewById(R.id.text_completion_title);
            if (titleTextView != null) {
                titleTextView.setText("Today's Completion Rate");
            }
        }

        // Handle case with no tasks today - with null checks
        if (todayTasks.isEmpty()) {
            if (completionRateTextView != null) completionRateTextView.setText("No tasks today");
            if (completionProgressBar != null) completionProgressBar.setProgress(0);
            return;
        }

        // Calculate completion rate for today's tasks
        int completedCount = 0;
        for (Task task : todayTasks) {
            if (task.isCompleted()) {
                completedCount++;
            }
        }

        int percentage = (int) ((completedCount / (float) todayTasks.size()) * 100);
        if (completionRateTextView != null) completionRateTextView.setText(percentage + "%");
        if (completionProgressBar != null) completionProgressBar.setProgress(percentage);
    }

    private void createWeeklyChart(List<Task> allTasks) {
        // Create chart components
        BarChart barChart = new BarChart(requireContext());
        barChart.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        chartContainerWeekly.removeAllViews();
        chartContainerWeekly.addView(barChart);

        // Set up date formatting
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<BarEntry> entries = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE", Locale.getDefault());

        // Start 6 days before today to show a full week with today at the end
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -6);

        // Process 7 days in chronological order
        for (int i = 0; i < 7; i++) {
            // Set day start/end times for filtering
            Calendar dayCal = (Calendar) cal.clone();
            dayCal.set(Calendar.HOUR_OF_DAY, 0);
            dayCal.set(Calendar.MINUTE, 0);
            dayCal.set(Calendar.SECOND, 0);
            dayCal.set(Calendar.MILLISECOND, 0);
            long dayStart = dayCal.getTimeInMillis();

            dayCal.set(Calendar.HOUR_OF_DAY, 23);
            dayCal.set(Calendar.MINUTE, 59);
            dayCal.set(Calendar.SECOND, 59);
            long dayEnd = dayCal.getTimeInMillis();

            // Calculate completion rate for this day
            int totalTasks = 0;
            int completedTasks = 0;
            for (Task task : allTasks) {
                if (task.getDueDate() >= dayStart && task.getDueDate() <= dayEnd) {
                    totalTasks++;
                    if (task.isCompleted()) {
                        completedTasks++;
                    }
                }
            }

            float percentage = totalTasks > 0 ? (completedTasks * 100f / totalTasks) : 0f;

            // Add label and data point for this day
            labels.add(dateFormat.format(cal.getTime()));
            entries.add(new BarEntry(i, percentage));

            // Move to next day
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Configure chart display
        BarDataSet dataSet = new BarDataSet(entries, "Completion Rate (%)");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
        dataSet.setValueTextSize(10f);

        // Format bar values to show percentage
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int)value + "%";
            }
        });

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // Style chart
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisLeft().setAxisMaximum(100f);
        barChart.getAxisLeft().setGranularity(10f);

        // Format Y-axis labels to show percentage
        barChart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int)value + "%";
            }
        });

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        barChart.invalidate();
    }

    private void setupTimeFilter() {
        // Create time period options
        String[] timePeriods = {"Today", "Last Week", "Last Month"};

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
                selectedTimePeriod = position;
                loadStats();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTimePeriod = 0; // Default to Today
            }
        });
    }

    private void createCategoryPieChart(List<Task> allTasks) {
        // Create chart
        PieChart pieChart = new PieChart(requireContext());
        pieChart.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        chartContainerCategory.removeAllViews();
        chartContainerCategory.addView(pieChart);

        // Calculate date ranges based on selected time period
        long startTime, endTime;
        Calendar cal = Calendar.getInstance();
        endTime = cal.getTimeInMillis(); // Current time as end time

        switch (selectedTimePeriod) {
            case 0: // Today
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                startTime = cal.getTimeInMillis();
                break;
            case 1: // Last Week
                cal.add(Calendar.DAY_OF_MONTH, -7);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                startTime = cal.getTimeInMillis();
                break;
            case 2: // Last Month
                cal.add(Calendar.MONTH, -1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                startTime = cal.getTimeInMillis();
                break;
            default:
                startTime = 0; // All time
        }

        // Count completed tasks by category within time period
        Map<Category, Integer> categoryCounts = new HashMap<>();
        for (Category category : Category.values()) {
            categoryCounts.put(category, 0);
        }

        for (Task task : allTasks) {
            if (task.isCompleted()) {
                // Use completion date if it exists and is valid, otherwise fall back to due date
                long taskDate = task.getCompletionDate();
                if (taskDate == 0) {
                    // If completion date is not available, use due date as fallback
                    taskDate = task.getDueDate();
                }

                // Check if the task date is within our filter period
                if (taskDate >= startTime && taskDate <= endTime) {
                    Category category = task.getCategory();
                    if (category != null) {
                        categoryCounts.put(category, categoryCounts.get(category) + 1);
                    }
                }
            }
        }

        // Create entries
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<Category, Integer> entry : categoryCounts.entrySet()) {
            if (entry.getValue() > 0) {
                entries.add(new PieEntry(entry.getValue(), entry.getKey().getName()));
            }
        }

        // Rest of your existing pie chart configuration code...

        // Handle case with no completed tasks
        if (entries.isEmpty()) {
            entries.add(new PieEntry(1, "No completed tasks"));
        }

        // Create dataset with empty title string
        PieDataSet dataSet = new PieDataSet(entries, "");
        ArrayList<Integer> colors = new ArrayList<>();

        // Use purple-toned colors from resources
        colors.add(ContextCompat.getColor(requireContext(), R.color.category_color_1));
        colors.add(ContextCompat.getColor(requireContext(), R.color.category_color_2));
        colors.add(ContextCompat.getColor(requireContext(), R.color.category_color_3));
        colors.add(ContextCompat.getColor(requireContext(), R.color.category_color_4));
        colors.add(ContextCompat.getColor(requireContext(), R.color.category_color_5));

        if (entries.size() == 1 && entries.get(0).getLabel().equals("No completed tasks")) {
            colors.clear();
            colors.add(Color.parseColor("#D3D3D3")); // Lighter gray for "no tasks" case
        }

        dataSet.setColors(colors);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);

        // Format values to show count
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int)value);
            }
        });

        // Configure chart
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setUsePercentValues(false);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.animateY(1000);

        // Configure legend
        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setTextSize(12f);
        legend.setWordWrapEnabled(true);

        pieChart.invalidate();
    }
}