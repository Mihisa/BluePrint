package com.mihisa.blueprint.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.support.annotation.NonNull;

public class BluePrintContentProvider extends ContentProvider {

    private static final UriMatcher uriMatcher = buildUriMatcher();
    private TaskDbHelper openHelper;

    private static final int TASK = 100;
    private static final int TASK_WITH_ID = 101;

    private static final String TASK_WITH_ID_SELECTION = TaskContract.TaskEntry.TABLE_NAME + "." + TaskContract.TaskEntry._ID + "=?";

    private Cursor getTaskFromId(Uri uri, String[] projection, String sortOrder) {
        long id = TaskContract.TaskEntry.getTaskIdFromUri(uri);
        return openHelper.getReadableDatabase().query(
                TaskContract.TaskEntry.TABLE_NAME,
                projection,
                TASK_WITH_ID_SELECTION,
                new String[] { Long.toString(id) },
                null,
                null,
                sortOrder
        );
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = TaskContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, TaskContract.PATH_TASK, TASK);
        matcher.addURI(authority, TaskContract.PATH_TASK + "/#", TASK_WITH_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        openHelper = new TaskDbHelper(getContext());
        return false;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case TASK:
                return TaskContract.TaskEntry.CONTENT_TYPE;
            case TASK_WITH_ID:
                return TaskContract.TaskEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor;

        switch (uriMatcher.match(uri)) {
            case TASK:
                cursor = openHelper.getReadableDatabase().query(TaskContract.TaskEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case TASK_WITH_ID:
                cursor = getTaskFromId(uri, projection, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        Uri retUri;

        switch (uriMatcher.match(uri)) {
            case TASK:
                long _id = openHelper.getWritableDatabase().insert(TaskContract.TaskEntry.TABLE_NAME,
                        null, values);
                if (_id > 0) {
                    retUri = TaskContract.TaskEntry.buildTaskWithId(_id);
                } else {
                    throw new SQLiteException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return retUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = openHelper.getWritableDatabase();
        int rowsDeleted;

        switch (uriMatcher.match(uri)) {
            case TASK:
                rowsDeleted = db.delete(TaskContract.TaskEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TASK_WITH_ID:
                long id = TaskContract.TaskEntry.getTaskIdFromUri(uri);
                rowsDeleted = db.delete(TaskContract.TaskEntry.TABLE_NAME,
                        TaskContract.TaskEntry._ID + "=?", new String[] { Long.toString(id) });
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        final SQLiteDatabase db = openHelper.getWritableDatabase();
        int rowsUpdated;

        switch (uriMatcher.match(uri)) {
            case TASK:
                rowsUpdated = db.update(TaskContract.TaskEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case TASK_WITH_ID:
                long id = TaskContract.TaskEntry.getTaskIdFromUri(uri);
                rowsUpdated = db.update(TaskContract.TaskEntry.TABLE_NAME, values,
                        TaskContract.TaskEntry._ID + "=?", new String[] { Long.toString(id) });
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }
}