package com.zen.callrecorder.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import com.jaredrummler.materialspinner.MaterialSpinner;
import com.zen.callrecorder.R;


import static com.zen.callrecorder.Constant.IS_RECORDING_AUTOMATICALLY;
import static com.zen.callrecorder.Constant.IS_RECORDING_IGNORE;
import static com.zen.callrecorder.Constant.IS_RECORDING_MANUALLY;
import static com.zen.callrecorder.Constant.IS_RECORDING_RECORD;

import static com.zen.callrecorder.Constant.DELETED_DAYS;

/**
 * Created by Ozenc Celik on 10/25/2018
 */

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = SettingsActivity.class.getSimpleName();
    private static SharedPreferences preferences;
    private static SharedPreferences.Editor editor;

    private Button saveButton, cancelButton;
    private RadioButton automaticRadio, manualRadio;
    private CheckBox ignoreCheck, recordCheck;

    private MaterialSpinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupActionBar();

        initComponents();

        readSharedPreferences();

        clickListener();
    }

    @Override
    protected void onStop() {
        super.onStop();

        SettingsActivity.this.finish();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SettingsActivity.this.finish();
    }

    public void initComponents(){
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        automaticRadio = findViewById(R.id.automaticRadio);
        manualRadio = findViewById(R.id.manualRadio);


        ignoreCheck = findViewById(R.id.ignoreCheck);
        recordCheck = findViewById(R.id.recordCheck);


        spinner = findViewById(R.id.spinner);
        spinner.setItems("0", "1", "2", "3", "4", "5", "6", "7");
    }

    public void readSharedPreferences(){
        IS_RECORDING_AUTOMATICALLY = preferences.getBoolean("IS_RECORDING_AUTOMATICALLY", true);
        IS_RECORDING_MANUALLY = preferences.getBoolean("IS_RECORDING_MANUALLY", false);
        IS_RECORDING_IGNORE = preferences.getBoolean("IS_RECORDING_IGNORE", false);
        IS_RECORDING_RECORD = preferences.getBoolean("IS_RECORDING_RECORD", false);
        DELETED_DAYS = preferences.getInt("IS_RECORDING_DAYS", 0);

        automaticRadio.setChecked(IS_RECORDING_AUTOMATICALLY);
        manualRadio.setChecked(IS_RECORDING_MANUALLY);
        ignoreCheck.setChecked(IS_RECORDING_IGNORE);
        recordCheck.setChecked(IS_RECORDING_RECORD);
        spinner.setSelectedIndex(DELETED_DAYS);
    }

    public void clickListener(){

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editor = preferences.edit();

                if (automaticRadio.isChecked() || manualRadio.isChecked()){
                    Toast.makeText(SettingsActivity.this, "Settings updated.", Toast.LENGTH_LONG).show();

                    editor.putBoolean("IS_RECORDING_AUTOMATICALLY", automaticRadio.isChecked());
                    editor.putBoolean("IS_RECORDING_MANUALLY", manualRadio.isChecked());
                    editor.putBoolean("IS_RECORDING_IGNORE", ignoreCheck.isChecked());
                    editor.putBoolean("IS_RECORDING_RECORD", recordCheck.isChecked());
                }
                editor.putInt("IS_RECORDING_DAYS", spinner.getSelectedIndex());

                editor.apply();

                onBackPressed();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        automaticRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    manualRadio.setChecked(false);
                }else{
                    manualRadio.setChecked(true);
                }

            }
        });
        manualRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    automaticRadio.setChecked(false);
                }else{
                    automaticRadio.setChecked(true);
                }

            }
        });


        ignoreCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) recordCheck.setChecked(false);
                else ignoreCheck.setChecked(false);
            }
        });
        recordCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) ignoreCheck.setChecked(false);
                else recordCheck.setChecked(false);
            }
        });


        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {

                if(Integer.parseInt(item) == 0) Snackbar.make(view, "Records will not be deleted", Snackbar.LENGTH_LONG).show();
                else Snackbar.make(view, "Records will be deleted after " + item + " days", Snackbar.LENGTH_LONG).show();
            }
        });

    }

    public void enableOrDisable(LinearLayout layout, boolean check){
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(check);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SettingsActivity.this, MainActivity.class));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupActionBar() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}

