package com.zen.callrecorder.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zen.callrecorder.Adapter.RecyclerAdapter;
import com.zen.callrecorder.Database.DatabaseHelper;
import com.zen.callrecorder.Database.Model.Contact;
import com.zen.callrecorder.R;
import com.zen.callrecorder.Utils.MyDividerItemDecoration;
import com.zen.callrecorder.Utils.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.List;

import static com.zen.callrecorder.Constant.COMING_CLASS;

/**
 * Created by Ozenc Celik on 10/25/2018
 */
public class RecordActivity extends AppCompatActivity {

    private static final String TAG = RecordActivity.class.getSimpleName();

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    private RecyclerAdapter mAdapter;
    private List<Contact> recordContactsList = new ArrayList<>();
    private TextView noRecordContactsView;

    private FloatingActionButton fab;

    public static DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        setupActionBar();

        initComponents();

        clickListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        RecordActivity.this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RecordActivity.this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_others, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }else if (id == R.id.action_information) {

            new AlertDialog.Builder(RecordActivity.this)
                    .setTitle("Contacts To Record List")
                    .setMessage("Add the Contacts in this list you wish to record.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .create()
                    .show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void initComponents(){

        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        recyclerView = findViewById(R.id.recycler_view);
        noRecordContactsView = findViewById(R.id.empty_notes_view);

        fab = findViewById(R.id.fab);

        db = new DatabaseHelper(this);
        recordContactsList.addAll(db.getAllContacts(2));

        mAdapter = new RecyclerAdapter(this, recordContactsList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);
        recyclerView.setHasFixedSize(true);

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

                recordContactsList.clear();
                recordContactsList.addAll(db.getAllContacts(2));
                mAdapter.notifyDataSetChanged();
                toggleEmptyAudios();
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
                showActionsDialog(position);
            }
        }));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                COMING_CLASS = 0;
                startActivity(new Intent(RecordActivity.this, ListActivity.class));
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(RecordActivity.this, MainActivity.class));
        finish();
    }

    private void setupActionBar() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    //DATABASE STUFF

    public static void insertContact(Contact contact) {

        long id = db.insertContact(contact);

    }

    private void deleteContactFromRecordList(int position) {

        final Contact n = recordContactsList.get(position);

        n.setInRecordList(0);
        Toast.makeText(RecordActivity.this, "Contact is removed in RecordList.", Toast.LENGTH_LONG).show();
        // updating note in db
        db.updateContact(n);

        // refreshing the list
        recordContactsList.remove(position);
        mAdapter.notifyItemChanged(position);

        toggleEmptyAudios();

    }

    private void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Delete From Here"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose option");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    deleteContactFromRecordList(position);
                }
            }
        });
        builder.show();
    }


    /**
     * Toggling list and empty notes view
     */
    private void toggleEmptyAudios() {
        // you can check audiosList.size() > 0
        noRecordContactsView.setText("No contacts found!");
        if (recordContactsList.size() > 0) {
            noRecordContactsView.setVisibility(View.GONE);
        } else {
            noRecordContactsView.setVisibility(View.VISIBLE);
        }
    }
}
