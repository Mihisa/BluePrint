package com.mihisa.blueprint.provider;

/**
 * Created by insight on 22.11.16.
 */

public interface TaskQuery {
    String [] PROJECTION = {
            TaskContract.TaskEntry._ID,
            TaskContract.TaskEntry.COLUMN_TITLE,
            TaskContract.TaskEntry.COLUMN_DESCRIPTION,
            TaskContract.TaskEntry.COLUMN_TIMESTAMP,
            TaskContract.TaskEntry.COLUMN_COMPLETED
    };
    int ID = 0;
    int TITLE = 1;
    int DESCRIPTION = 2;
    int TIMESTAMP = 3;
    int COMPLETED = 4;

}
