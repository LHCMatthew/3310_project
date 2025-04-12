package edu.cuhk.csci3310.a3310_project.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.Time;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database Info
    private static final String DATABASE_NAME = "todoDatabase";
    private static final int DATABASE_VERSION = 5;

    // Table Names
    public static final String TABLE_LISTS = "lists";
    public static final String TABLE_TASKS = "tasks";


    // Lists Table Columns
    public static final String KEY_LIST_ID = "id";
    public static final String KEY_LIST_TITLE = "title";
    public static final String TASK_COUNT = "task_count";
    public static final String KEY_LIST_DESCRIPTION = "description";

    // Tasks Table Columns
    public static final String KEY_TASK_ID = "id";
    public static final String KEY_TASK_TITLE = "title";
    public static final String KEY_TASK_DESCRIPTION = "description";
    public static final String KEY_TASK_LIST_ID = "list_id";
    public static final String KEY_TASK_DUE_DATE = "due_date";
    public static final String KEY_TASK_PRIORITY = "priority";
    public static final String KEY_TASK_COMPLETED = "completed";
    public static final String KEY_TASK_CATEGORY = "category";
    public static final String KEY_START_TIME = "start_time";
    public static final String KEY_END_TIME = "end_time";
    public static final String KEY_COMPLETION_DATE = "completion_date";
    public static final String ALL_DAY = "isallday";
    public static final String KEY_REMINDER_TIME = "reminder_time";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Lists table
        String CREATE_LISTS_TABLE = "CREATE TABLE " + TABLE_LISTS +
                "(" +
                KEY_LIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_LIST_TITLE + " TEXT," +
                KEY_LIST_DESCRIPTION + " TEXT," +
                TASK_COUNT + " DEFAULT 0" +
                ")";

        // Create Tasks table with new category column
        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS +
                "(" +
                KEY_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_TASK_TITLE + " TEXT," +
                KEY_TASK_DESCRIPTION + " TEXT," +
                KEY_TASK_LIST_ID + " INTEGER REFERENCES " + TABLE_LISTS + "," +
                KEY_TASK_DUE_DATE + " INTEGER," +
                KEY_TASK_PRIORITY + " INTEGER," +
                KEY_TASK_COMPLETED + " INTEGER," +
                KEY_TASK_CATEGORY + " TEXT," +
                KEY_START_TIME + " INTEGER," +
                KEY_END_TIME + " INTEGER," +
                KEY_COMPLETION_DATE + " INTEGER," +
                ALL_DAY + " INTEGER," +
                KEY_REMINDER_TIME + " INTEGER" +
                ")";

        db.execSQL(CREATE_LISTS_TABLE);
        db.execSQL(CREATE_TASKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Drop older tables if existed
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LISTS);
            // Create tables again
            onCreate(db);
        }
    }
}
