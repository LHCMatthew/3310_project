package edu.cuhk.csci3310.a3310_project.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import edu.cuhk.csci3310.a3310_project.R;
import edu.cuhk.csci3310.a3310_project.database.TodoListRepository;
import edu.cuhk.csci3310.a3310_project.models.TodoList;

public class AddListFragment extends Fragment {
    private TextInputEditText editListName;
    private RadioGroup colorGroup;
    private TextInputEditText editListDescription;
    private MaterialButton btnSave;
    private MaterialButton btnCancel;
    private TodoListRepository listRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_list, container, false);

        // Set toolbar title
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Add New List");
        }

        // Initialize repository
        listRepository = new TodoListRepository(requireContext());

        // Initialize views
        editListName = view.findViewById(R.id.edit_list_name);
        editListDescription = view.findViewById(R.id.edit_list_description);
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);

        // Set up button listeners
        btnSave.setOnClickListener(v -> saveList());
        btnCancel.setOnClickListener(v -> navigateBack());

        return view;
    }

    private void saveList() {
        String listName = editListName.getText().toString().trim();

        if (listName.isEmpty()) {
            editListName.setError("List name is required");
            return;
        }

        // Create new list object
        TodoList newList = new TodoList();
        newList.setTitle(listName);

        // Save to database
        long id = listRepository.insertList(newList);

        if (id > 0) {
            Toast.makeText(getContext(), "List created successfully", Toast.LENGTH_SHORT).show();
            navigateBack();
        } else {
            Toast.makeText(getContext(), "Failed to create list", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateBack() {
        // Navigate back to lists fragment
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}