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

public class IgnoreActivity extends AppCompatActivity {

    private static final String TAG = IgnoreActivity.class.getSimpleName();

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    private RecyclerAdapter mAdapter;
    private List<Contact> ignoreContactsList = new ArrayList<>();
    private TextView noIgnoreContactsView;

    private FloatingActionButton fab;

    public static DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ignore);

        setupActionBar();

        initComponents();

        clickListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        IgnoreActivity.this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        IgnoreActivity.this.finish();
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

            new AlertDialog.Builder(IgnoreActivity.this)
                    .setTitle("Contacts To Ignore List")
                    .setMessage("The contacts in this list will not be recorded in calling state.")
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
        noIgnoreContactsView = findViewById(R.id.empty_notes_view);

        fab = findViewById(R.id.fab);

        db = new DatabaseHelper(this);
        ignoreContactsList.addAll(db.getAllContacts(1));

        mAdapter = new RecyclerAdapter(this, ignoreContactsList);
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

                ignoreContactsList.clear();
                ignoreContactsList.addAll(db.getAllContacts(1));
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
                COMING_CLASS = 1;
                startActivity(new Intent(IgnoreActivity.this, ListActivity.class));
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(IgnoreActivity.this, MainActivity.class));
        finish();
    }

    private void setupActionBar() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    //DATABASE THINKS
    private void deleteContactFromIgnoreList(int position) {

        final Contact n = ignoreContactsList.get(position);

        n.setInIgnoreList(0);
        Toast.makeText(IgnoreActivity.this, "Contact is removed in IgnoreList.", Toast.LENGTH_LONG).show();
        // updating note in db
        db.updateContact(n);

        // refreshing the list
        ignoreContactsList.remove(position);
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
                    deleteContactFromIgnoreList(position);
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
        noIgnoreContactsView.setText("No contacts found!");
        if (ignoreContactsList.size() > 0) {
            noIgnoreContactsView.setVisibility(View.GONE);
        } else {
            noIgnoreContactsView.setVisibility(View.VISIBLE);
        }
    }
}
