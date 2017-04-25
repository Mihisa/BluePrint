package com.mihisa.blueprint;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.PersistableBundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.mihisa.blueprint.adapter.TaskAdapter;
import com.mihisa.blueprint.provider.TaskContract;
import com.mihisa.blueprint.provider.TaskQuery;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static com.mihisa.blueprint.R.id.cancel_action;
import static com.mihisa.blueprint.R.id.toolbar;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "========ERROR========";

    private static final String [] PERMISSIONS = {Manifest.permission.READ_CONTACTS};
    private static final int LOADER_PROFILE = 0;
    private static final int LOADER_INBOX = 1;
    private static final int LOADER_COMPLETED = 2;


    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    private TextView usernameTextView;
    private TextView useremailTextView;
    private ImageView userthumbImageView;
    private RecyclerView recyclerView;

    private TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        usernameTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.textview_username);
        useremailTextView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.textview_useremail);
        userthumbImageView = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.imageview_user);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.menu_inbox);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (ContextCompat.checkSelfPermission(this, PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED) {
            getSupportLoaderManager().initLoader(LOADER_PROFILE, null, this);
        } else {
            requestPermissions(PERMISSIONS, 0);
        }

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(this, null);
        recyclerView.setAdapter(adapter);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) return true;
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getSupportLoaderManager().initLoader(LOADER_PROFILE, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_PROFILE) {
            return new CursorLoader(this,
                    Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                            ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
                    ProfileQuery.PROJECTION,
                    ContactsContract.Contacts.Data.MIMETYPE + "=? OR "
                            + ContactsContract.Contacts.Data.MIMETYPE + "=? OR "
                            + ContactsContract.Contacts.Data.MIMETYPE + "=?",
                    new String[] {
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
                            ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                    },
                    ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
        } else if (id == LOADER_INBOX) {
            return new CursorLoader(this, TaskContract.TaskEntry.CONTENT_URI, TaskQuery.PROJECTION,
                    TaskContract.TaskEntry.COLUMN_COMPLETED + "=0", null, null);
        } else if (id == LOADER_COMPLETED) {
            return new CursorLoader(this, TaskContract.TaskEntry.CONTENT_URI, TaskQuery.PROJECTION,
                    TaskContract.TaskEntry.COLUMN_COMPLETED + "=1", null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
        if (loader.getId() == LOADER_PROFILE) {
            String name = null, email = null, photoUri = null;
            while (!data.isAfterLast()) {
                String mimeType = data.getString(ProfileQuery.MIME_TYPE);
                if (mimeType.equals(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)) {
                    photoUri = data.getString(ProfileQuery.PHOTO);
                } else if (mimeType.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
                    email = data.getString(ProfileQuery.EMAIL);
                } else if (mimeType.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {
                    name = data.getString(ProfileQuery.GIVEN_NAME) + " " + data.getString(ProfileQuery.FAMILY_NAME);
                }
                data.moveToNext();
            }

            if (name != null) usernameTextView.setText(name);
            if (email != null) useremailTextView.setText(email);
            if (photoUri != null) {
                try {
                    InputStream is = getContentResolver().openInputStream(Uri.parse(photoUri));
                    RoundedBitmapDrawable roundedBitmap = RoundedBitmapDrawableFactory.create(getResources(), is);
                    roundedBitmap.setCircular(true);
                    userthumbImageView.setImageDrawable(roundedBitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else if (loader.getId() == LOADER_INBOX || loader.getId() == LOADER_COMPLETED) {
            adapter.changeCursor(data);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.changeCursor(null);
        adapter.notifyDataSetChanged();
    }

    public void newTask(View view) {
        startActivity(new Intent(this, EditActivity.class));
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_inbox:
                getSupportLoaderManager().restartLoader(LOADER_INBOX, null, this);
                drawerLayout.closeDrawers();
                return true;
            case R.id.menu_completed:
                getSupportLoaderManager().restartLoader(LOADER_COMPLETED, null, this);
                drawerLayout.closeDrawers();
                return true;
        }
        return false;
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                ContactsContract.CommonDataKinds.Photo.PHOTO_URI,
                ContactsContract.Contacts.Data.MIMETYPE
        };

        int EMAIL = 0;
        int FAMILY_NAME = 1;
        int GIVEN_NAME = 2;
        int PHOTO = 3;
        int MIME_TYPE = 4;
    }

}