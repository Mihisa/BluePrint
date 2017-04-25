package com.mihisa.blueprint;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mihisa.blueprint.provider.TaskQuery;

public class ViewActivity extends AppCompatActivity {

    public static final String DATE_FORMAT_STRING = "EEEE, MMM d, yyyy";
    public static final String TIME_FORMAT_STRING = "h:mm a";

    private Uri taskUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        taskUri = getIntent().getData();
        Cursor cursor = getContentResolver().query(taskUri, TaskQuery.PROJECTION, null, null, null);
        cursor.moveToFirst();
        String title = cursor.getString(TaskQuery.TITLE);
        String description = cursor.getString(TaskQuery.DESCRIPTION);
        long timestamp = cursor.getLong(TaskQuery.TIMESTAMP) * 1000L;
        cursor.close();

        TextView dateTextView = (TextView) findViewById(R.id.textview_task_date);
        TextView timeTextView = (TextView) findViewById(R.id.textview_task_time);
        TextView descriptionTextView = (TextView) findViewById(R.id.textview_task_description);

        dateTextView.setText(DateFormat.format(DATE_FORMAT_STRING, timestamp));
        timeTextView.setText(DateFormat.format(TIME_FORMAT_STRING, timestamp));

        if (description == null || description.isEmpty()) {
            ImageView descriptionImageView = (ImageView) findViewById(R.id.imageview_description);
            descriptionImageView.setVisibility(View.GONE);
            descriptionTextView.setVisibility(View.GONE);
        } else {
            descriptionTextView.setText(description);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.menu_delete:
                getContentResolver().delete(taskUri, null, null);
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void editTask(View view) {
        Intent intent = new Intent(this, EditActivity.class);
        intent.setData(taskUri);
        startActivity(intent);
    }
}