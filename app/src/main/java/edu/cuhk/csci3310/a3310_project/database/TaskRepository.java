package edu.cuhk.csci3310.a3310_project.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.cuhk.csci3310.a3310_project.models.Category;
import edu.cuhk.csci3310.a3310_project.models.Task;

public class TaskRepository {
    private static final String TAG = "TaskRepository";
    private DatabaseHelper dbHelper;

    public TaskRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Create a new task
    // Create a new task
    public long insertTask(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long taskId = -1;

        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_TASK_TITLE, task.getTitle());
            values.put(DatabaseHelper.KEY_TASK_DESCRIPTION, task.getDescription());
            values.put(DatabaseHelper.KEY_TASK_LIST_ID, task.getListId());
            values.put(DatabaseHelper.KEY_TASK_PRIORITY, task.getPriority());
            values.put(DatabaseHelper.KEY_TASK_COMPLETED, task.isCompleted() ? 1 : 0);

            // Store the category
            if (task.getCategory() != null) {
                values.put(DatabaseHelper.KEY_TASK_CATEGORY, task.getCategory().name());
            } else {
                values.put(DatabaseHelper.KEY_TASK_CATEGORY, Category.OTHER.name());
            }

            // Store the dueDate directly as long
            long dueDate = task.getDueDate();
            if (dueDate > 0) {
                values.put(DatabaseHelper.KEY_TASK_DUE_DATE, dueDate);
            } else {
                values.putNull(DatabaseHelper.KEY_TASK_DUE_DATE);
            }

            // Insert the new row
            taskId = db.insert(DatabaseHelper.TABLE_TASKS, null, values);
            task.setId(taskId);
        } catch (Exception e) {
            Log.e(TAG, "Error inserting task", e);
        } finally {
            db.close();
        }
        return taskId;
    }

    // Get all tasks for a specific list
    public List<Task> getTasksByListId(long listId) {
        List<Task> tasks = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(DatabaseHelper.TABLE_TASKS,
                    null,  // Get all columns
                    DatabaseHelper.KEY_TASK_LIST_ID + "=?",
                    new String[]{String.valueOf(listId)},
                    null, null,
                    DatabaseHelper.KEY_TASK_DUE_DATE + " ASC, " +
                            DatabaseHelper.KEY_TASK_PRIORITY + " DESC");

            if (cursor.moveToFirst()) {
                do {
                    Task task = cursorToTask(cursor);
                    tasks.add(task);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting tasks by list ID", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return tasks;
    }

    // Get a single task by ID
    public Task getTaskById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        Task task = null;

        try {
            cursor = db.query(DatabaseHelper.TABLE_TASKS,
                    null,  // Get all columns
                    DatabaseHelper.KEY_TASK_ID + "=?",
                    new String[]{String.valueOf(id)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                task = cursorToTask(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting task by ID", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return task;
    }

    // Update a task
    public int updateTask(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = 0;

        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_TASK_TITLE, task.getTitle());
            values.put(DatabaseHelper.KEY_TASK_DESCRIPTION, task.getDescription());
            values.put(DatabaseHelper.KEY_TASK_LIST_ID, task.getListId());
            values.put(DatabaseHelper.KEY_TASK_PRIORITY, task.getPriority());
            values.put(DatabaseHelper.KEY_TASK_COMPLETED, task.isCompleted() ? 1 : 0);
            values.put(DatabaseHelper.KEY_TASK_CATEGORY, task.getCategory().name());

            long dueDate = task.getDueDate();
            if (dueDate > 0) {
                values.put(DatabaseHelper.KEY_TASK_DUE_DATE, dueDate);
            } else {
                values.putNull(DatabaseHelper.KEY_TASK_DUE_DATE);
            }

            rowsAffected = db.update(DatabaseHelper.TABLE_TASKS, values,
                    DatabaseHelper.KEY_TASK_ID + " = ?",
                    new String[]{String.valueOf(task.getId())});
        } catch (Exception e) {
            Log.e(TAG, "Error updating task", e);
        } finally {
            db.close();
        }

        return rowsAffected;
    }

    // Delete a task
    public int deleteTask(long taskId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = 0;

        try {
            rowsAffected = db.delete(DatabaseHelper.TABLE_TASKS,
                    DatabaseHelper.KEY_TASK_ID + " = ?",
                    new String[]{String.valueOf(taskId)});
        } catch (Exception e) {
            Log.e(TAG, "Error deleting task", e);
        } finally {
            db.close();
        }

        return rowsAffected;
    }

    // Delete all tasks for a specific list
    public int deleteTasksByListId(long listId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = 0;

        try {
            rowsAffected = db.delete(DatabaseHelper.TABLE_TASKS,
                    DatabaseHelper.KEY_TASK_LIST_ID + " = ?",
                    new String[]{String.valueOf(listId)});
        } catch (Exception e) {
            Log.e(TAG, "Error deleting tasks by list ID", e);
        } finally {
            db.close();
        }

        return rowsAffected;
    }

    // Helper method to convert cursor to Task object
    private Task cursorToTask(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_TASK_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_TASK_TITLE));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_TASK_DESCRIPTION));
        long listId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_TASK_LIST_ID));
        int priority = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_TASK_PRIORITY));
        boolean completed = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_TASK_COMPLETED)) == 1;

        // Get dueDate directly as long
        long dueDate = 0;

        // Get the category
        Category category = Category.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_TASK_CATEGORY)));
        if (!cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_TASK_DUE_DATE))) {
            dueDate = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_TASK_DUE_DATE));
        }

        return new Task(id, title, description, listId, dueDate, priority, completed, category);
    }
}