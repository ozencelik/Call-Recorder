package com.zen.callrecorder.Receiver;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.zen.callrecorder.Database.DatabaseHelper;
import com.zen.callrecorder.Database.Model.Audio;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.zen.callrecorder.Constant.AUDIO_FILE_PATH;
import static com.zen.callrecorder.Constant.IS_RECORDING_AUTOMATICALLY;
import static com.zen.callrecorder.Constant.IS_RECORDING_IGNORE;
import static com.zen.callrecorder.Constant.IS_RECORDING_MANUALLY;
import static com.zen.callrecorder.Constant.IS_RECORDING_RECORD;

import static com.zen.callrecorder.Constant.DATE_FORMAT;
import static com.zen.callrecorder.Constant.IS_RECORD_STARTED;
/**
 * Created by Ozenc Celik on 10/25/2018
 */

public class CallReceiver extends PhoneCallReceiver {

    @SuppressLint("SimpleDateFormat")
    private DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

    private static MediaRecorder mediaRecorder;
    private File file;

    private static Audio audio;
    public static DatabaseHelper db;

    //To record the duration
    private long startHTime = 0L;
    private Handler customHandler = new Handler();
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    static String lastDuration = "";
    static String FILE_PATH = "";

    private Runnable updateTimerThread = new Runnable() {

        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - startHTime;

            updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            lastDuration  = String.format("%02d", mins) + ":" +  String.format("%02d", secs);

            customHandler.postDelayed(this, 0);
        }

    };

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start) {
        Toast.makeText(ctx, "Incoming Call Received", Toast.LENGTH_LONG).show();

        readSharedPreferencesData(ctx);

        if(IS_RECORDING_AUTOMATICALLY){
            if(IS_RECORDING_IGNORE){
                if(!db.isIgnoreContactExist(number)){
                    initializeRecording(ctx, number);
                }
            }else if(IS_RECORDING_RECORD){
                if(db.isRecordContactExist(number)){
                    initializeRecording(ctx, number);
                }
            }else{
                initializeRecording(ctx, number);
            }
        }else if(IS_RECORDING_MANUALLY){
            if(IS_RECORDING_IGNORE){
                if(!db.isIgnoreContactExist(number)){
                    initializeRecording(ctx, number);
                }
            }else if(IS_RECORDING_RECORD){
                if(db.isRecordContactExist(number)){
                    initializeRecording(ctx, number);
                }
            }else{
                initializeRecording(ctx, number);
            }
        }

    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
        Toast.makeText(ctx, "Incoming Call Answered", Toast.LENGTH_LONG).show();
        readSharedPreferencesData(ctx);

        if(IS_RECORDING_AUTOMATICALLY || IS_RECORDING_MANUALLY) {
            startRecording();
        }

    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        Toast.makeText(ctx, "Incoming Call Ended", Toast.LENGTH_LONG).show();
        readSharedPreferencesData(ctx);

        if(IS_RECORDING_AUTOMATICALLY || IS_RECORDING_MANUALLY) stopRecording(ctx);


    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        Toast.makeText(ctx, "Outgoing Call Started", Toast.LENGTH_LONG).show();
        readSharedPreferencesData(ctx);

        if(IS_RECORDING_AUTOMATICALLY){
            if(IS_RECORDING_IGNORE){
                if(!db.isIgnoreContactExist(number)){
                    initializeRecording(ctx, number);
                    startRecording();
                }
            }else if(IS_RECORDING_RECORD){
                if(db.isRecordContactExist(number)){
                    initializeRecording(ctx, number);
                    startRecording();
                }
            }else{
                initializeRecording(ctx, number);
                startRecording();
            }
        }else if(IS_RECORDING_MANUALLY){
            if(IS_RECORDING_IGNORE){
                if(!db.isIgnoreContactExist(number)){
                    initializeRecording(ctx, number);
                    startRecording();
                }
            }else if(IS_RECORDING_RECORD){
                if(db.isRecordContactExist(number)){
                    initializeRecording(ctx, number);
                    startRecording();
                }
            }else{
                initializeRecording(ctx, number);
                startRecording();
            }
        }

    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        Toast.makeText(ctx, "Outgoing Call Ended", Toast.LENGTH_LONG).show();
        readSharedPreferencesData(ctx);

        if(IS_RECORDING_AUTOMATICALLY || IS_RECORDING_MANUALLY) stopRecording(ctx);

    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        Toast.makeText(ctx, "Missed Call !!!", Toast.LENGTH_LONG).show();
        readSharedPreferencesData(ctx);

        File deletedFile = new File(file.getAbsolutePath() + File.separator + audio.getFilePath());
        deletedFile.delete();
    }

    public String getContactName(Context ctx, String phoneNumber) {
        ArrayList<String> phones = new ArrayList<>();
        ContentResolver m = ctx.getContentResolver();
        Cursor cursor = m.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.NUMBER +" = ?",
                new String[]{""+phoneNumber}, null);

        while (cursor.moveToNext())
        {
            phones.add(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
        }

        cursor.close();

        if(phones.size() > 0) return phones.get(0);
        return "No contact name";
    }


    public void readSharedPreferencesData(Context ctx){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        db = new DatabaseHelper(ctx);

        IS_RECORDING_AUTOMATICALLY = preferences.getBoolean("IS_RECORDING_AUTOMATICALLY", false);
        IS_RECORDING_MANUALLY = preferences.getBoolean("IS_RECORDING_MANUALLY", false);
        IS_RECORDING_IGNORE = preferences.getBoolean("IS_RECORDING_IGNORE", false);
        IS_RECORDING_RECORD = preferences.getBoolean("IS_RECORDING_RECORD", false);
    }

    private void initializeRecording(Context ctx, String contactNumber){

        //Recording folder for saving the audio
        file = new File(AUDIO_FILE_PATH);
        //file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS);
        Date date = new Date();
        String currentDate = dateFormat.format(date);

        FILE_PATH = file.getAbsolutePath() + "/" + currentDate + ".3gp";

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); //Önceden VOICE_CALL
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); // Önceden THREE_GPP
        mediaRecorder.setOutputFile(FILE_PATH);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        audio = new Audio();
        audio.setContactName(getContactName(ctx, contactNumber));
        audio.setContactNumber(contactNumber);
        audio.setTimestamp(currentDate);
        audio.setFavorite(0);
        audio.setFilePath(currentDate + ".3gp");
    }

    private void startRecording() {

        if(mediaRecorder != null) {

            IS_RECORD_STARTED = true;

            try {  mediaRecorder.prepare(); }
            catch (IOException e) { Log.e("MediaRecorder", "prepare() failed"); }

            mediaRecorder.start();

            startHTime = SystemClock.uptimeMillis();
            customHandler.postDelayed(updateTimerThread, 0);
        }

    }
    private void stopRecording(Context ctx){

        if(mediaRecorder != null && IS_RECORD_STARTED){
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;

            timeSwapBuff += timeInMilliseconds;
            customHandler.removeCallbacks(updateTimerThread);

            if(IS_RECORDING_AUTOMATICALLY){

                audio.setDuration(lastDuration);
                db.insertAudio(audio);

            }else if(IS_RECORDING_MANUALLY){

                Intent i = new Intent();
                i.setClassName("com.zen.callrecorder", "com.zen.callrecorder.Activity.AlertDialogActivity");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("audio", audio);
                i.putExtra("duration", lastDuration);
                i.putExtra("path", FILE_PATH);
                ctx.startActivity(i);
            }

            IS_RECORD_STARTED = false;

        }

    }


}
