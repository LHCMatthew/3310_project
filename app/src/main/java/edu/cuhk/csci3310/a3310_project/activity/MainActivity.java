package edu.cuhk.csci3310.a3310_project.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import edu.cuhk.csci3310.a3310_project.R;
import edu.cuhk.csci3310.a3310_project.fragments.AddListFragment;
import edu.cuhk.csci3310.a3310_project.fragments.AddTaskFragment;
import edu.cuhk.csci3310.a3310_project.fragments.ListsFragment;
import edu.cuhk.csci3310.a3310_project.fragments.StatsFragment;
import edu.cuhk.csci3310.a3310_project.fragments.TasksFragment;
import edu.cuhk.csci3310.a3310_project.fragments.TodayFragment;

import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import androidx.appcompat.app.AlertDialog;

import edu.cuhk.csci3310.a3310_project.notification.*;

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

        // Create notification channel
        NotificationHelper.createNotificationChannel(this);

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        101);
            }
        }

        // Add FragmentManager listener to handle fab visibility
        getSupportFragmentManager().addFragmentOnAttachListener((fragmentManager, fragment) -> {
            if (fragment instanceof AddTaskFragment || fragment instanceof TodayFragment) {
                fab.hide();
            }
            else {
                fab.show();
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof ListsFragment || currentFragment instanceof TasksFragment) {
                fab.show();
            }
            else {
                fab.hide();
            }
        });

        // Setup FAB
        fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> {
            Fragment currentFragment = getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);

            if (currentFragment instanceof ListsFragment) {
                // Add new list
                Bundle args = new Bundle();
                AddListFragment addListFragment = new AddListFragment();
                addListFragment.setArguments(args);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, addListFragment)
                        .addToBackStack(null)
                        .commit();
            } else if (currentFragment instanceof TasksFragment) {
                // Add new task
                Bundle args = new Bundle();
                AddTaskFragment addTaskFragment = new AddTaskFragment();
                currentFragment.getArguments();
                assert currentFragment.getArguments() != null;
                // Because if listId = null, it means that there are no lists, and
                // it is not possible to add a task without a list,
                // i.e. TasksFragment cannot be accessed without clicking on an existing list item.
                long ListID = currentFragment.getArguments().getLong("listId");
                args.putLong("listId", ListID);
                addTaskFragment.setArguments(args);
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
                 selectedFragment = new TodayFragment();
            }
            else
            {
                 selectedFragment = new StatsFragment();
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
}