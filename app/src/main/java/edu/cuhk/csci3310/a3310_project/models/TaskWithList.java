package edu.cuhk.csci3310.a3310_project.models;

public class TaskWithList {
    private Task task;
    private String listName;

    public TaskWithList(Task task, String listName) {
        this.task = task;
        this.listName = listName;
    }

    public Task getTask() {
        return task;
    }

    public String getListName() {
        return listName;
    }
}