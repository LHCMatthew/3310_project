package edu.cuhk.csci3310.a3310_project.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import edu.cuhk.csci3310.a3310_project.R;
import edu.cuhk.csci3310.a3310_project.fragments.AddTaskFragment;
import edu.cuhk.csci3310.a3310_project.fragments.ListsFragment;
import edu.cuhk.csci3310.a3310_project.fragments.TasksFragment;

import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import androidx.appcompat.app.AlertDialog;

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
        // Create an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New List");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Enter list name");

        // Add padding to the input field
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);

        // Set up the buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            String listTitle = input.getText().toString().trim();
            if (!listTitle.isEmpty()) {
                // Find the current ListsFragment and call its addNewList method
                Fragment currentFragment = getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
                if (currentFragment instanceof ListsFragment) {
                    ((ListsFragment) currentFragment).addNewList(listTitle);
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}