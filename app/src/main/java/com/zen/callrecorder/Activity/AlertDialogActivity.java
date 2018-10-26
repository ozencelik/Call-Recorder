package com.zen.callrecorder.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.zen.callrecorder.Database.DatabaseHelper;
import com.zen.callrecorder.Database.Model.Audio;

import java.io.File;

/**
 * Created by Ozenc Celik on 10/25/2018
 */
public class AlertDialogActivity extends AppCompatActivity {

    public static DatabaseHelper db;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DatabaseHelper(this);

        final Audio audio = (Audio) getIntent().getSerializableExtra("audio");
        final String filePath = getIntent().getStringExtra("path");
        final String duration = getIntent().getStringExtra("duration");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to record?").setCancelable(
                false).setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        audio.setDuration(duration);
                        db.insertAudio(audio);

                        moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);

                    }
                }).setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        File fdelete = new File(filePath);
                        if (fdelete.exists()) {
                            if (fdelete.delete()) {
                                System.out.println("file Deleted :");
                            } else {
                                System.out.println("file not Deleted :");
                            }
                        }

                        dialog.cancel();
                        moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }
}