package edu.cuhk.csci3310.a3310_project.models;

public enum Category {
    PERSONAL("Personal"),
    WORK("Work"),
    SHOPPING("Shopping"),
    HOMEWORK("Homework"),
    OTHER("Others");

    private final String name;

    Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
