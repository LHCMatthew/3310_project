package edu.cuhk.csci3310.a3310_project.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.cuhk.csci3310.a3310_project.R;
import edu.cuhk.csci3310.a3310_project.adapters.ListAdapter;
import edu.cuhk.csci3310.a3310_project.models.TodoList;

public class ListsFragment extends Fragment implements ListAdapter.OnListClickListener {
    private RecyclerView recyclerView;
    private ListAdapter adapter;
    private List todoLists = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lists, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_lists);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ListAdapter(todoLists, this);
        recyclerView.setAdapter(adapter);

        // Load data
        loadLists();

        return view;
    }

    private void loadLists() {
        // In a real app, load from database
        // For demo, use dummy data
        todoLists.clear();
        todoLists.add(new TodoList(1, "Work", 4));
        todoLists.add(new TodoList(2, "Personal", 2));
        todoLists.add(new TodoList(3, "Shopping", 5));
        todoLists.add(new TodoList(4, "Homework", 3));
        adapter.notifyDataSetChanged();
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
