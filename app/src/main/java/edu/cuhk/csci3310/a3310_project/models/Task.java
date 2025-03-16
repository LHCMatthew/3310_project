package edu.cuhk.csci3310.a3310_project.models;

public class Task {
    private long id;
    private String title;
    private String description;
    private long listId;
    private long dueDate;
    private boolean allday;
    private long startTime;
    private long endTime;
    private int priority; // 0: Low, 1: Medium, 2: High
    private boolean isCompleted;
    private long completionDate;
    private Category category;

    // Default constructor for creating new tasks

    public Task() {
        this.priority = 0;
        this.isCompleted = false;
        this.category = Category.OTHER;
        this.allday = false;
    }
    // Constructors, getters, setters


    public Task(long id, String title, String description, long listId, long dueDate, boolean allday, long startTime, long endTime, int priority, boolean isCompleted, long completionDate, Category category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.listId = listId;
        this.dueDate = dueDate;
        this.allday = allday;
        this.startTime = startTime;
        this.endTime = endTime;
        this.priority = priority;
        this.isCompleted = isCompleted;
        this.completionDate = completionDate;
        this.category = category;
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

    public boolean isAllday() {
        return allday;
    }

    public void setAllday(boolean allday) {
        this.allday = allday;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
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
