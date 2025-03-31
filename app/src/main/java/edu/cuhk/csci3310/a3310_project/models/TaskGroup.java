package edu.cuhk.csci3310.a3310_project.models;

public class TaskGroup {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_TASK = 1;

    private int type;
    private String listTitle;
    private TaskWithList taskWithList;

    // Constructor for header
    public TaskGroup(String listTitle) {
        this.type = TYPE_HEADER;
        this.listTitle = listTitle;
    }

    // Constructor for task
    public TaskGroup(TaskWithList taskWithList) {
        this.type = TYPE_TASK;
        this.taskWithList = taskWithList;
    }

    public int getType() {
        return type;
    }

    public String getListTitle() {
        return listTitle;
    }

    public TaskWithList getTaskWithList() {
        return taskWithList;
    }
}
