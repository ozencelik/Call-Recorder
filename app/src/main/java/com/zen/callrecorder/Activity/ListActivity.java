package com.zen.callrecorder.Activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zen.callrecorder.Adapter.ContactRecyclerAdapter;
import com.zen.callrecorder.Constant;
import com.zen.callrecorder.Database.DatabaseHelper;
import com.zen.callrecorder.Database.Model.Audio;
import com.zen.callrecorder.Database.Model.Contact;
import com.zen.callrecorder.R;
import com.zen.callrecorder.Utils.MyDividerItemDecoration;
import com.zen.callrecorder.Utils.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.List;

import static com.zen.callrecorder.Constant.COMING_CLASS;
import static com.zen.callrecorder.Constant.FIRST_ENTER;
import static com.zen.callrecorder.Constant.IS_CONTACT_LOADING;

/**
 * Created by Ozenc Celik on 10/25/2018
 */
public class ListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{

    private static final String TAG = ListActivity.class.getSimpleName();

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    private ContactRecyclerAdapter mAdapter;
    private List<Contact> contactsList = new ArrayList<>();
    private TextView noContactsView;

    private FloatingActionButton fab;

    public static DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // Contact Permission
        if (FIRST_ENTER == 0 && !IS_CONTACT_LOADING &&
                ContextCompat.checkSelfPermission(ListActivity.this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            getDataFromSQLite();
        }

        setupActionBar();

        initComponents();

        clickListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ListActivity.this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ListActivity.this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);

        MenuItem search = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query){
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText){
        newText = newText.toLowerCase();

        ArrayList<Contact> newList = new ArrayList<>();
        for (Contact a: contactsList) {

            String name = a.getContactName().toLowerCase();
            String number = a.getContactNumber().toLowerCase();

            if(name.contains(newText)) newList.add(a);
            else if(number.contains(newText)) newList.add(a);
        }

        mAdapter.setFilter(newList);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public void initComponents(){

        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        recyclerView = findViewById(R.id.recycler_view);
        noContactsView = findViewById(R.id.empty_notes_view);

        fab = findViewById(R.id.fab);

        db = new DatabaseHelper(this);
        contactsList.addAll(db.getAllContacts(0));


        reloadTheContacts();
        //getDataFromSQLite();
        /*
        if(db.getContactsCount() == 0){
            getDataFromSQLite();
        }else {
            //db.deleteAllContact();
            getDataFromSQLite();
        }*/

        mAdapter = new ContactRecyclerAdapter(this, contactsList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);
        //recyclerView.setHasFixedSize(true);

        toggleEmptyAudios();

    }

    public void clickListener(){

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // refresh complete
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 3000);

                reloadTheContacts();
            }
        });
        // Configure the refreshing colors
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_dark,
                android.R.color.holo_red_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_green_dark);


        /**
         * On long press on RecyclerView item, open alert dialog
         * with options to choose
         * Edit and Delete
         * */
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
            }
            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(ListActivity.this, ContactRecyclerAdapter.checkedContactList.size() + " contacs saved.", Toast.LENGTH_LONG).show();
                updateContactList(ContactRecyclerAdapter.checkedContactList);
                onBackPressed();

            }
        });
    }

    @Override
    public void onBackPressed() {
        if(COMING_CLASS == 0) startActivity(new Intent(ListActivity.this, RecordActivity.class));
        else if(COMING_CLASS == 1) startActivity(new Intent(ListActivity.this, IgnoreActivity.class));
        finish();
    }

    private void setupActionBar() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void reloadTheContacts(){

        // AsyncTask is used that SQLite operation not blocks the UI Thread.
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                contactsList.clear();
                contactsList.addAll(db.getAllContacts(0));
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                mAdapter.notifyDataSetChanged();
                toggleEmptyAudios();

            }
        }.execute();

    }

    private void getDataFromSQLite() {

        IS_CONTACT_LOADING = true;
        FIRST_ENTER = 1;

        final ProgressDialog progressDialog = new ProgressDialog(ListActivity.this);
        progressDialog.setMessage("Contacts are loading...");
        progressDialog.show();

        // AsyncTask is used that SQLite operation not blocks the UI Thread.
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                getContactList();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                IS_CONTACT_LOADING = false;

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        }.execute();
    }


    private void getContactList() {
        Contact c;

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        /*
                        Log.i(TAG, "ID: " + id);
                        Log.i(TAG, "Name: " + name);
                        Log.i(TAG, "Phone Number: " + phoneNo);*/

                        //.replaceAll("\\s+","")
                        c = new Contact();
                        c.setId(Integer.parseInt(id));
                        c.setIsInContactList(1);
                        c.setContactName(name);
                        c.setContactNumber(phoneNo.replaceAll("[\\s\\-()]", ""));
                        c.setInIgnoreList(0);
                        c.setInRecordList(0);

                        db.insertContact(c);
                    }
                    pCur.close();
                }
            }
        }
        if(cur!=null){
            cur.close();
        }
    }

    //DATABASE STUFF
    private void updateContactList(List<Contact> list) {

        if(COMING_CLASS == 0){
            for(Contact c : list){
                c.setInRecordList(1);
                db.updateContact(c);
            }
        }else if(COMING_CLASS == 1){
            for(Contact c : list){
                c.setInIgnoreList(1);
                db.updateContact(c);
            }
        }
    }

    /**
     * Toggling list and empty notes view
     */
    private void toggleEmptyAudios() {
        // you can check audiosList.size() > 0
        noContactsView.setText("No contacts found!");
        if (contactsList.size() > 0) {
            noContactsView.setVisibility(View.GONE);
        } else {
            noContactsView.setVisibility(View.VISIBLE);
        }
    }
}
