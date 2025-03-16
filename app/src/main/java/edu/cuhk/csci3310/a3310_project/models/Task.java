package edu.cuhk.csci3310.a3310_project.models;

public class Task {
    private long id;
    private String title;
    private String description;
    private long listId;
    private long dueDate;
    private int priority; // 0: Low, 1: Medium, 2: High
    private boolean isCompleted;
    private long completionDate;
    private Category category;

    // Default constructor for creating new tasks

    public Task() {
        this.priority = 0;
        this.isCompleted = false;
        this.category = Category.OTHER;
    }
    // Constructors, getters, setters

    public Task(long id_, String title_, String description_, long listId_, long dueDate_, int priority_, boolean isCompleted_, Category category_)
    {
        id = id_;
        title = title_;
        description = description_;
        listId = listId_;
        dueDate = dueDate_;
        priority = priority_;
        isCompleted = isCompleted_;
        category = category_;
    }
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getListId() {
        return listId;
    }

    public void setListId(long listId) {
        this.listId = listId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getDueDate() {
        return dueDate;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean isChecked) {
        isCompleted = isChecked;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public long getCompletionDate() { return completionDate; }

    public void setCompletionDate(long completionDate) { this.completionDate = completionDate; }
}
