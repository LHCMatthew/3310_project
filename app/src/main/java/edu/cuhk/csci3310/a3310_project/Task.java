package edu.cuhk.csci3310.a3310_project;

public class Task {
    private long id;
    private String title;
    private String description;
    private long listId;
    private long dueDate;
    private int priority; // 0: Low, 1: Medium, 2: High
    private boolean isCompleted;

    // Constructors, getters, setters
    public Task(long id_, String title_, String description_, long listId_, long dueDate_, int priority_, boolean isCompleted_)
    {
        id = id_;
        title = title_;
        description = description_;
        listId = listId_;
        dueDate = dueDate_;
        priority = priority_;
        isCompleted = isCompleted_;
    }

    public long getId() {
        return id;
    }

    public long getListId() {
        return listId;
    }

    public String getTitle() {
        return title;
    }

    public void setCompleted(boolean isChecked) {
        isCompleted = isChecked;
    }

    public int getPriority() {
        return priority;
    }

    public long getDueDate() {
        return dueDate;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return isCompleted;
    }
}
