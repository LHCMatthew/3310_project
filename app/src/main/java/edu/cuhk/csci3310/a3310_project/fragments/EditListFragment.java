package edu.cuhk.csci3310.a3310_project.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import edu.cuhk.csci3310.a3310_project.R;
import edu.cuhk.csci3310.a3310_project.database.TodoListRepository;
import edu.cuhk.csci3310.a3310_project.models.TodoList;

public class EditListFragment extends Fragment {

    private TodoListRepository repository;
    private long listId;
    private String listTitle;
    private String listDescription;
    private TextInputEditText titleEditText;
    private TextInputEditText descriptionEditText;
    private Button saveButton;
    private Button cancelButton;
    private Button deleteButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_list, container, false);

        // Set title
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Edit List");
        }

        // Initialize repository
        repository = new TodoListRepository(getContext());

        // Get arguments passed from ListsFragment
        Bundle args = getArguments();
        if (args != null) {
            listId = args.getLong("listId", -1);
            listTitle = args.getString("listTitle", "");
            listDescription = args.getString("listDescription", "");
        }

        // Initialize UI components
        titleEditText = view.findViewById(R.id.edit_list_title);
        descriptionEditText = view.findViewById(R.id.edit_list_description);
        saveButton = view.findViewById(R.id.button_save);
        cancelButton = view.findViewById(R.id.button_cancel);
        deleteButton = view.findViewById(R.id.button_delete);

        // Set existing values
        titleEditText.setText(listTitle);
        descriptionEditText.setText(listDescription);

        // Setup save button
        saveButton.setOnClickListener(v -> saveList());

        // Setup cancel button
        cancelButton.setOnClickListener(v -> {
            // Simply go back to the previous fragment
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Setup delete button
        deleteButton.setOnClickListener(v -> confirmDelete());

        return view;
    }

    private void confirmDelete() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete List")
                .setMessage("Are you sure you want to delete this list? This will also delete all tasks in this list.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete the list (and associated tasks via cascade)
                    int rowsAffected = repository.deleteList(listId);

                    if (rowsAffected > 0) {
                        Toast.makeText(getContext(), "List deleted successfully", Toast.LENGTH_SHORT).show();

                        // Navigate back to ListsFragment
                        requireActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        Toast.makeText(getContext(), "Failed to delete list", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveList() {
        String title = titleEditText.getText() != null ? titleEditText.getText().toString() : "";
        String description = descriptionEditText.getText() != null ?
                descriptionEditText.getText().toString() : "";

        // Validate input
        if (title.trim().isEmpty()) {
            titleEditText.setError("Title cannot be empty");
            return;
        }

        // Create updated TodoList object
        TodoList updatedList = new TodoList(listId, title, 0, description);

        // Save to database
        int rowsAffected = repository.updateList(updatedList);

        if (rowsAffected > 0) {
            Toast.makeText(getContext(), "List updated successfully", Toast.LENGTH_SHORT).show();
            // Go back to the lists fragment
            requireActivity().getSupportFragmentManager().popBackStack();
        } else {
            Toast.makeText(getContext(), "Failed to update list", Toast.LENGTH_SHORT).show();
        }
    }
}