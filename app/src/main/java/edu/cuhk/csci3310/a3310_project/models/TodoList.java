package edu.cuhk.csci3310.a3310_project.models;

// TodoList.java
public class TodoList {
    private long id;
    private String title;
    private int taskCount; // For displaying count in UI
    private String description;
    // Default constructor for creating new list
    public TodoList() {
    }

    // Constructor for creating list from database
    public TodoList(long id, String title) {
        this.id = id;
        this.title = title;
        this.taskCount = 0;
    }
    public TodoList(long id, String title, int taskCount, String description) {
        this.id = id;
        this.title = title;
        this.taskCount = taskCount;
        this.description = description;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
