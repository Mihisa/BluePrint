package com.mihisa.blueprint.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.Settings;

/**
 * Created by insight on 15.11.16.
 */

public class TaskDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "task.db";

    public TaskDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_TASK_TABLE = "CREATE TABLE " + TaskContract.TaskEntry.TABLE_NAME + "(" +
                TaskContract.TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TaskContract.TaskEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                TaskContract.TaskEntry.COLUMN_DESCRIPTION + " TEXT, " +
                TaskContract.TaskEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
                TaskContract.TaskEntry.COLUMN_COMPLETED + " INTEGER NOT NULL" +
                ");";

        final String SQL_TEST_DATA = "INSERT INTO task(title, description, timestamp, completed) VALUES(\'Test\'," +
                "\'This is a really long description that I am using so that we can get on with development.\'," +
                System.currentTimeMillis() / 1000L + ", 0);";

        sqLiteDatabase.execSQL(SQL_CREATE_TASK_TABLE);
        sqLiteDatabase.execSQL(SQL_TEST_DATA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TaskContract.TaskEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}