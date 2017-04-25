package com.mihisa.blueprint;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Settings;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.mihisa.blueprint.provider.TaskContract;
import com.mihisa.blueprint.provider.TaskQuery;

import java.util.Calendar;

public class EditActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private Uri taskUri;
    private long timestamp;
    private String title;
    private String description;
    private boolean completed;

    private TextView dateTextView;
    private TextView timeTextView;
    private EditText titleEditText;
    private EditText descriptionEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        taskUri = getIntent().getData();

        if (taskUri == null) {
            title = "";
            description = "";
            timestamp = System.currentTimeMillis();
            ContentValues values = new ContentValues();
            values.put(TaskContract.TaskEntry.COLUMN_TITLE, title);
            values.put(TaskContract.TaskEntry.COLUMN_TIMESTAMP, timestamp);
            values.put(TaskContract.TaskEntry.COLUMN_COMPLETED, 0);
            taskUri = getContentResolver().insert(TaskContract.TaskEntry.CONTENT_URI, values);

        } else {
            Cursor cursor = getContentResolver().query(taskUri, TaskQuery.PROJECTION, null, null, null);
            cursor.moveToFirst();
            title = cursor.getString(TaskQuery.TITLE);
            description = cursor.getString(TaskQuery.DESCRIPTION);
            timestamp = cursor.getLong(TaskQuery.TIMESTAMP) * 1000L;
            completed = cursor.getInt(TaskQuery.COMPLETED) == 1;
            cursor.close();
        }

        dateTextView = (TextView) findViewById(R.id.textview_task_date);
        timeTextView = (TextView) findViewById(R.id.textview_task_time);
        titleEditText = (EditText) findViewById(R.id.edittext_task_description);
        descriptionEditText = (EditText) findViewById(R.id.edittext_title);

        dateTextView.setText(DateFormat.format(ViewActivity.DATE_FORMAT_STRING, timestamp));
        timeTextView.setText(DateFormat.format(ViewActivity.TIME_FORMAT_STRING, timestamp));
        descriptionEditText.setText(description);
        titleEditText.setText(title);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                saveData();
                navigateUp();
                return true;
            case R.id.menu_cancel:
                navigateUp();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        saveData();
        navigateUp();
    }

    public void onDateTimeClick(View view) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        if (view.getId() == R.id.textview_task_date) {
            DatePickerDialog datePickerDialog = new DatePickerDialog (this, this,
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        } else if (view.getId() == R.id.textview_task_time) {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, this,
                    cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false);
            timePickerDialog.show();
        }
    }
    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(year, month, day);
        timestamp = cal.getTimeInMillis();

        dateTextView.setText(DateFormat.format(ViewActivity.DATE_FORMAT_STRING, timestamp));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        timestamp = cal.getTimeInMillis();

        timeTextView.setText(DateFormat.format(ViewActivity.TIME_FORMAT_STRING, timestamp));
    }
    private void navigateUp() {
        Intent intent = new Intent(this, ViewActivity.class);
        intent.setData(taskUri);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void saveData() {
        title = titleEditText.getText().toString();
        description = descriptionEditText.getText().toString();

        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COLUMN_TITLE, title);
        values.put(TaskContract.TaskEntry.COLUMN_DESCRIPTION, description);
        values.put(TaskContract.TaskEntry.COLUMN_TIMESTAMP, timestamp / 1000L);
        values.put(TaskContract.TaskEntry.COLUMN_COMPLETED, completed);

        getContentResolver().update(taskUri, values, null, null);
    }
}
