package edu.cuhk.csci3310.a3310_project;

// TodoList.java
public class TodoList {
    private final long id;
    private final String title;
    private int taskCount; // For displaying count in UI

    // Constructors, getters, setters
    public TodoList(long id_, String title_, int taskCount_)
    {
        id = id_;
        title = title_;
        taskCount = taskCount_;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getTaskCount() {
        return taskCount;
    }
}
