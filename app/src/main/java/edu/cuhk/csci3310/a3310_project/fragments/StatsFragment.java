package edu.cuhk.csci3310.a3310_project.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.cuhk.csci3310.a3310_project.R;
import edu.cuhk.csci3310.a3310_project.database.TaskRepository;
import edu.cuhk.csci3310.a3310_project.models.Task;

public class StatsFragment extends Fragment {

    private TextView completionRateTextView;
    private ProgressBar completionProgressBar;
    private TextView currentStreakTextView;
    private TextView bestStreakTextView;
    private ViewGroup chartContainerWeekly;
    private TaskRepository taskRepository;

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
        currentStreakTextView = view.findViewById(R.id.text_current_streak);
        bestStreakTextView = view.findViewById(R.id.text_best_streak);
        chartContainerWeekly = view.findViewById(R.id.chart_container_weekly);

        // Initialize repository
        taskRepository = new TaskRepository(requireContext());

        loadStats();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStats();
    }

    private void loadStats() {
        List<Task> allTasks = taskRepository.getAllTasks();

        // Calculate completion rate
        updateCompletionRate(allTasks);

        // Calculate streaks
        updateStreaks(allTasks);

        // Create weekly chart
        createWeeklyChart(allTasks);
    }

    private void updateCompletionRate(List<Task> allTasks) {
        if (allTasks.isEmpty()) {
            completionRateTextView.setText("0%");
            completionProgressBar.setProgress(0);
            return;
        }

        int completedCount = 0;
        for (Task task : allTasks) {
            if (task.isCompleted()) {
                completedCount++;
            }
        }

        int percentage = (int) ((completedCount / (float) allTasks.size()) * 100);
        completionRateTextView.setText(percentage + "%");
        completionProgressBar.setProgress(percentage);
    }

    private void updateStreaks(List<Task> allTasks) {
        // Map to track days with completed tasks
        Map<String, Boolean> completedDays = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Current date for reference
        Calendar calendar = Calendar.getInstance();
        String today = dateFormat.format(calendar.getTime());

        // Check all tasks for completion dates
        for (Task task : allTasks) {
            if (task.isCompleted() && task.getCompletionDate() > 0) {
                String completedDate = dateFormat.format(new Date(task.getCompletionDate()));
                completedDays.put(completedDate, true);
            }
        }

        // Calculate current streak
        int currentStreak = 0;
        boolean streakActive = completedDays.containsKey(today);

        if (streakActive) {
            currentStreak = 1;
            calendar.add(Calendar.DAY_OF_MONTH, -1);

            while (true) {
                String dateStr = dateFormat.format(calendar.getTime());
                if (completedDays.containsKey(dateStr)) {
                    currentStreak++;
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                } else {
                    break;
                }
            }
        }

        // Display streak information
        currentStreakTextView.setText(currentStreak + " days");
        bestStreakTextView.setText("Best: " + Math.max(currentStreak, 7) + " days"); // Placeholder for best streak
    }

    private void createWeeklyChart(List<Task> allTasks) {
        // Create chart
        BarChart barChart = new BarChart(requireContext());
        barChart.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // Clear previous chart if any
        chartContainerWeekly.removeAllViews();
        chartContainerWeekly.addView(barChart);

        // Get last 7 days
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<BarEntry> entries = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Map to count tasks completed per day
        Map<String, Integer> completedTasksPerDay = new HashMap<>();

        // Count completed tasks per day
        for (Task task : allTasks) {
            if (task.isCompleted() && task.getCompletionDate() > 0) {
                String dateStr = fullDateFormat.format(new Date(task.getCompletionDate()));
                completedTasksPerDay.put(dateStr, completedTasksPerDay.getOrDefault(dateStr, 0) + 1);
            }
        }

        // Create chart entries for the last 7 days
        for (int i = 6; i >= 0; i--) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
            String dateStr = fullDateFormat.format(cal.getTime());
            String label = dateFormat.format(cal.getTime());
            labels.add(label);

            // Get count for this day (0 if none)
            int count = completedTasksPerDay.getOrDefault(dateStr, 0);
            entries.add(new BarEntry(6-i, count));
        }

        // Set up chart data
        BarDataSet dataSet = new BarDataSet(entries, "Tasks Completed");
        dataSet.setColor(Color.parseColor("#4285F4"));
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // Customize chart appearance
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisLeft().setGranularity(1f);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        barChart.invalidate();
    }
}