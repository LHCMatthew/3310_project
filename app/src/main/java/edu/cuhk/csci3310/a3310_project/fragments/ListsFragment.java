package edu.cuhk.csci3310.a3310_project.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.cuhk.csci3310.a3310_project.R;
import edu.cuhk.csci3310.a3310_project.adapters.ListAdapter;
import edu.cuhk.csci3310.a3310_project.database.TodoListRepository;
import edu.cuhk.csci3310.a3310_project.models.TodoList;

public class ListsFragment extends Fragment implements ListAdapter.OnListClickListener {
    private RecyclerView recyclerView;
    private ListAdapter adapter;
    private List todoLists = new ArrayList<>();
    private TodoListRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lists, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_lists);

        // Initialize repository upon startup
        repository = new TodoListRepository(getContext());

        // set title
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("My list");

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ListAdapter(todoLists, this);
        recyclerView.setAdapter(adapter);

        // Load data
        loadLists();

        return view;
    }

    // Reload lists when loaded back to this fragment
    @Override
    public void onResume() {
        super.onResume();
        // Reload lists when coming back to this fragment
        loadLists();
    }

    // Load lists from database
    private void loadLists() {
        // In a real app, load from database
        // For demo, use dummy data
        todoLists.clear();
        todoLists.addAll(repository.getAllLists());
        adapter.updateLists(todoLists);
    }

    // Add a new list
    public void addNewList(String title) {
        TodoList newList = new TodoList();
        newList.setTitle(title);

        // Insert into database
        long listId = repository.insertList(newList);

        if (listId != -1) {
            // Reload lists to show the new entry
            loadLists();
        }
    }

    @Override
    public void onListClick(TodoList todoList) {
        // Navigate to TasksFragment
        Bundle args = new Bundle();
        args.putLong("listId", todoList.getId());
        args.putString("listTitle", todoList.getTitle());

        TasksFragment tasksFragment = new TasksFragment();
        tasksFragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, tasksFragment)
                .addToBackStack(null)
                .commit();
    }
}
