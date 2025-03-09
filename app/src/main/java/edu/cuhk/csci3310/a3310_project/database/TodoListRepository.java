package edu.cuhk.csci3310.a3310_project.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.cuhk.csci3310.a3310_project.models.TodoList;

public class TodoListRepository {
    private static final String TAG = "TodoListRepository";
    private DatabaseHelper dbHelper;

    public TodoListRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Create a new list
    public long insertList(TodoList todoList) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long listId = -1;

        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_LIST_TITLE, todoList.getTitle());

            // Insert the new row, returning the primary key value
            listId = db.insert(DatabaseHelper.TABLE_LISTS, null, values);
            todoList.setId(listId);
        } catch (Exception e) {
            Log.e(TAG, "Error inserting list", e);
        } finally {
            db.close();
        }
        return listId;
    }

    // Get all todo lists
    public List<TodoList> getAllLists() {
        List<TodoList> todoLists = new ArrayList<>();

        // Select query
        String selectQuery = "SELECT l.*, (SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_TASKS +
                " t WHERE t." + DatabaseHelper.KEY_TASK_LIST_ID + " = l." +
                DatabaseHelper.KEY_LIST_ID + ") AS " + DatabaseHelper.TASK_COUNT +
                " FROM " + DatabaseHelper.TABLE_LISTS + " l";

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(selectQuery, null);

            // Loop through all rows and add to list
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_LIST_ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_LIST_TITLE));
                    int taskCount = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.TASK_COUNT));

                    TodoList todoList = new TodoList(id, title, taskCount);
                    todoLists.add(todoList);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while getting lists", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return todoLists;
    }

    // Get a single todo list by ID
    public TodoList getListById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        TodoList todoList = null;

        try {
            cursor = db.query(DatabaseHelper.TABLE_LISTS,
                    new String[]{DatabaseHelper.KEY_LIST_ID, DatabaseHelper.KEY_LIST_TITLE},
                    DatabaseHelper.KEY_LIST_ID + "=?",
                    new String[]{String.valueOf(id)}, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                todoList = new TodoList(
                        cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_LIST_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_LIST_TITLE))
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting list by ID", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return todoList;
    }

    // Update a todo list
    public int updateList(TodoList todoList) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = 0;

        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.KEY_LIST_TITLE, todoList.getTitle());

            rowsAffected = db.update(DatabaseHelper.TABLE_LISTS, values,
                    DatabaseHelper.KEY_LIST_ID + " = ?",
                    new String[]{String.valueOf(todoList.getId())});
        } catch (Exception e) {
            Log.e(TAG, "Error updating list", e);
        } finally {
            db.close();
        }

        return rowsAffected;
    }

    // Delete a todo list
    public int deleteList(long listId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = 0;

        try {
            // First delete all tasks associated with this list
            db.delete(DatabaseHelper.TABLE_TASKS,
                    DatabaseHelper.KEY_TASK_LIST_ID + " = ?",
                    new String[]{String.valueOf(listId)});

            // Then delete the list itself
            rowsAffected = db.delete(DatabaseHelper.TABLE_LISTS,
                    DatabaseHelper.KEY_LIST_ID + " = ?",
                    new String[]{String.valueOf(listId)});
        } catch (Exception e) {
            Log.e(TAG, "Error deleting list", e);
        } finally {
            db.close();
        }

        return rowsAffected;
    }
}