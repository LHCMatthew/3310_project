package edu.cuhk.csci3310.a3310_project;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.fragment.app.Fragment;

import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;

import edu.cuhk.csci3310.a3310_project.ListsFragment;
import edu.cuhk.csci3310.a3310_project.AddTaskFragment;

// MainActivity.java
public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup FAB
        fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> {
            Fragment currentFragment = getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);

            if (currentFragment instanceof ListsFragment) {
                // Add new list
                showAddListDialog();
            } else if (currentFragment instanceof TasksFragment) {
                // Add new task
                AddTaskFragment addTaskFragment = new AddTaskFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, addTaskFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        // Setup bottom navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_lists)
            {
                selectedFragment = new ListsFragment();
            }
            else if (item.getItemId() == R.id.nav_today)
            {
                // selectedFragment = new TodayFragment(); (need to be implemented)
            }
            else
            {
                // selectedFragment = new StatsFragment(); (need to be implemented)
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });

        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ListsFragment())
                    .commit();
        }
    }

    private void showAddListDialog() {
        // Show dialog to add a new list
        // Implementation depends on your UI design preferences
    }
}