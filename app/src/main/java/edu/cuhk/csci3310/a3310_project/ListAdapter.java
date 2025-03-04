package edu.cuhk.csci3310.a3310_project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {
    private List lists;
    private OnListClickListener listener;

    public interface OnListClickListener {
        void onListClick(edu.cuhk.csci3310.a3310_project.TodoList todoList);
    }

    public ListAdapter(List lists, OnListClickListener listener) {
        this.lists = lists;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list, parent, false);
        return new ListViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        edu.cuhk.csci3310.a3310_project.TodoList todoList = (TodoList) lists.get(position);
        holder.titleTextView.setText(todoList.getTitle());
        holder.countTextView.setText(String.valueOf(todoList.getTaskCount()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onListClick(todoList);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    public void updateLists(List newLists) {
        this.lists = newLists;
        notifyDataSetChanged();
    }

    static class ListViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView countTextView;

        ListViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.text_list_title);
            countTextView = itemView.findViewById(R.id.text_list_count);
        }
    }

}
