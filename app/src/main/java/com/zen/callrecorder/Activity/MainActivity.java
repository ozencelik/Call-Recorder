package com.zen.callrecorder.Activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zen.callrecorder.Adapter.AudioRecyclerAdapter;
import com.zen.callrecorder.Constant;
import com.zen.callrecorder.Database.DatabaseHelper;
import com.zen.callrecorder.Database.Model.Audio;
import com.zen.callrecorder.R;
import com.zen.callrecorder.Utils.MyDividerItemDecoration;
import com.zen.callrecorder.Utils.RecyclerTouchListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.PROCESS_OUTGOING_CALLS;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_CONTACTS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import static com.zen.callrecorder.Constant.AUDIO_FILE_PATH;
import static com.zen.callrecorder.Constant.DELETED_DAYS;
import static com.zen.callrecorder.Constant.ORDER_LIST_STATE;


import static com.zen.callrecorder.Constant.IS_RECORDING_AUTOMATICALLY;
import static com.zen.callrecorder.Constant.IS_RECORDING_IGNORE;
import static com.zen.callrecorder.Constant.IS_RECORDING_MANUALLY;
import static com.zen.callrecorder.Constant.IS_RECORDING_RECORD;
import static com.zen.callrecorder.Constant.PERMISSION_REQUEST_CODE;

public class MainActivity extends AppCompatActivity implements Runnable, SearchView.OnQueryTextListener{

    private static SharedPreferences preferences;
    private static SharedPreferences.Editor editor;

    //Listing Audios
    private SwipeRefreshLayout swipeRefreshLayout;
    private File file;
    private RecyclerView recyclerView;
    private List<String> audiosFilePath;

    //Media Player Variables
    private static MediaPlayer mediaPlayer = new MediaPlayer();
    private SeekBar seekBar;
    private Button buttonPlayPause, dismissButton;
    private TextView txt;
    private boolean wasPlaying = false;

    private AudioRecyclerAdapter mAdapter;
    private List<Audio> audiosList = new ArrayList<>();
    private TextView noAudiosView;

    public static DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initComponents();

        readSharedPreferences();

        compareDates();

        if(!checkPermission()) requestPermission();
        else listAudios();

    }

    @Override
    protected void onStop() {
        super.onStop();
        MainActivity.this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearMediaPlayer();
        MainActivity.this.finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    // Settings Acticity inflate magnify_glass on action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

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

        ArrayList<Audio> newList = new ArrayList<>();
        for (Audio a: audiosList) {

            String name = a.getContactName().toLowerCase();
            String number = a.getContactNumber().toLowerCase();
            String note;
            if(a.getNote() != null)note = a.getNote().toLowerCase();
            else note = "";

            if(name.contains(newText)) newList.add(a);
            else if(number.contains(newText)) newList.add(a);
            else if(note.contains(newText)) newList.add(a);
        }

        mAdapter.setFilter(newList);
        return true;
    }

    // Menu Options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // launch settings activity
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            finish();
            return true;
        }else if(id == R.id.action_ignore){
            // launch ignore activity
            startActivity(new Intent(MainActivity.this, IgnoreActivity.class));
            finish();
            return true;
        }else if(id == R.id.action_record){
            // launch record activity
            startActivity(new Intent(MainActivity.this, RecordActivity.class));
            finish();
            return true;
        }else if(id == R.id.action_favorite){
            // launch favorite activity
            startActivity(new Intent(MainActivity.this, FavoriteActivity.class));
            finish();
            return true;
        }else if(id == R.id.action_search){
            return true;
        }else if(id == R.id.action_order){

            ORDER_LIST_STATE = (ORDER_LIST_STATE + 1) % 3;

            audiosList.clear();
            audiosList.addAll(db.getAllAudios(ORDER_LIST_STATE));
            mAdapter.notifyDataSetChanged();
            toggleEmptyAudios();

            if(ORDER_LIST_STATE == 0){
                item.setIcon(R.drawable.calendar);
                Toast.makeText(MainActivity.this, "Audios are ordered by date", Toast.LENGTH_SHORT).show();
            }
            else if(ORDER_LIST_STATE == 1){
                item.setIcon(R.drawable.sort_by_alphabet);
                Toast.makeText(MainActivity.this, "Audios are ordered by name", Toast.LENGTH_SHORT).show();
            }
            else if(ORDER_LIST_STATE == 2){
                item.setIcon(R.drawable.clock);
                Toast.makeText(MainActivity.this, "Audios are ordered by duration of audio", Toast.LENGTH_SHORT).show();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //////////////////////////////////////////////////////////////////////////////////////////


    //Initialize Components
    public void initComponents(){
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        recyclerView = findViewById(R.id.recycler_view);
        noAudiosView = findViewById(R.id.empty_notes_view);

        db = new DatabaseHelper(this);
        audiosList.addAll(db.getAllAudios(0));

        mAdapter = new AudioRecyclerAdapter(this, audiosList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);
        recyclerView.setHasFixedSize(true);

        toggleEmptyAudios();

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

                audiosList.clear();
                audiosList.addAll(db.getAllAudios(0));
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

                final String filePath = file.getAbsolutePath() + "/" + audiosList.get(position).getFilePath();
                //final String filePath = audiosList.get(position).getFilePath();

                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.popup_audios);
                dialog.setTitle("Title...");
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                buttonPlayPause = dialog.findViewById(R.id.playButton);
                dismissButton = dialog.findViewById(R.id.dismissButton);
                seekBar = dialog.findViewById(R.id.seekbar);
                txt = dialog.findViewById(R.id.audio_name);
                txt.setText(audiosList.get(position).getFilePath());
                final TextView seekBarHint = dialog.findViewById(R.id.textView);

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                        seekBarHint.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                        seekBarHint.setVisibility(View.VISIBLE);
                        int x = (int) Math.ceil(progress / 1000f);

                        if (x < 10)
                            seekBarHint.setText("0:0" + x);
                        else
                            seekBarHint.setText("0:" + x);

                        double percent = progress / (double) seekBar.getMax();
                        int offset = seekBar.getThumbOffset();
                        int seekWidth = seekBar.getWidth();
                        int val = (int) Math.round(percent * (seekWidth - 2 * offset));
                        int labelWidth = seekBarHint.getWidth();
                        seekBarHint.setX(offset + seekBar.getX() + val
                                - Math.round(percent * offset)
                                - Math.round(percent * labelWidth / 2));

                        if (progress > 0 && mediaPlayer != null && !mediaPlayer.isPlaying()) {
                            clearMediaPlayer();
                            buttonPlayPause.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.play));
                            MainActivity.this.seekBar.setProgress(0);
                        }

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {


                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            mediaPlayer.seekTo(seekBar.getProgress());
                        }
                    }
                });

                dismissButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        clearMediaPlayer();
                    }
                });
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        clearMediaPlayer();
                    }
                });
                playSong(filePath);
                buttonPlayPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playSong(filePath);
                    }
                });

                dialog.show();

            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));
    }

    public void readSharedPreferences(){
        IS_RECORDING_AUTOMATICALLY = preferences.getBoolean("IS_RECORDING_AUTOMATICALLY", true);
        IS_RECORDING_MANUALLY = preferences.getBoolean("IS_RECORDING_MANUALLY", false);
        IS_RECORDING_IGNORE = preferences.getBoolean("IS_RECORDING_IGNORE", false);
        IS_RECORDING_RECORD = preferences.getBoolean("IS_RECORDING_RECORD", false);
        DELETED_DAYS = preferences.getInt("IS_RECORDING_DAYS", 0);
    }

    //To delete the audios after days which is determined in settings.
    public void compareDates(){
        Calendar c = Calendar.getInstance();

        for (Audio a : audiosList){

            String str = a.getTimestamp();
            int year = Integer.parseInt(str.substring(0,4));
            int month = Integer.parseInt(str.substring(5, 7));
            int day = Integer.parseInt(str.substring(8,10));


            int Cyear = c.get(Calendar.YEAR);
            int Cmonth = c.get(Calendar.MONTH) + 1;
            int Cday = c.get(Calendar.DAY_OF_MONTH);

            if(Cyear == year){
                if(Cmonth == month){
                    if((Cday - day) > DELETED_DAYS){
                        deleteAudio(a);
                    }
                }else if(Cmonth > month){
                    if((Cday + 31 - day) >= DELETED_DAYS){
                        deleteAudio(a);
                    }
                }

            }
        }

    }

    //Get audio files from Call Recorder folder
    public void listAudios(){
        audiosFilePath = new ArrayList<>();

        file = new File(AUDIO_FILE_PATH);
        file.mkdir();
        //file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS);
        File list[] = file.listFiles();

        if(list != null){
            for( int i=0; i< list.length; i++)
            {
                if(checkExtension( list[i].getName() ))
                {
                    audiosFilePath.add( list[i].getName());
                }
            }
        }

        toggleEmptyAudios();
        recyclerView.setHasFixedSize(true);
    }

    public void playSong(String filePath) {
        //Get the filePath of the audio and play it.
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                clearMediaPlayer();
                seekBar.setProgress(0);
                wasPlaying = true;
                buttonPlayPause.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.play));
            }
            if (!wasPlaying) {

                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                }

                buttonPlayPause.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.pause));

                mediaPlayer.setDataSource(filePath);

                mediaPlayer.prepare();
                //mediaPlayer.setVolume(0.5f, 0.5f);
                mediaPlayer.setVolume(100, 100);
                mediaPlayer.setLooping(false);
                seekBar.setMax(mediaPlayer.getDuration());

                mediaPlayer.start();
                new Thread(this).start();
            }

            wasPlaying = false;
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    public void run() {
        int currentPosition = mediaPlayer.getCurrentPosition();
        int total = mediaPlayer.getDuration();


        while (mediaPlayer != null && mediaPlayer.isPlaying() && currentPosition < total) {
            try {
                Thread.sleep(1000);
                currentPosition = mediaPlayer.getCurrentPosition();
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                return;
            }

            seekBar.setProgress(currentPosition);
        }
    }

    private void clearMediaPlayer() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = new MediaPlayer();
    }

    private boolean checkExtension( String fileName ) {
        String ext = getFileExtension(fileName);
        if ( ext == null) return false;
        if ( ext.equals("3gp") ) {
            return true;
        }
        return false;
    }

    public String getFileExtension(String fileName ) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i+1);
        } else
            return null;
    }



    //////////////////////////////////////////////////////////////////////////////////
    //Database Function


    /**
     * Updating note in db and updating
     * item in the list by its position
     */
    private void updateNote(String note, int position) {
        Audio n = audiosList.get(position);
        // updating note text
        n.setNote(note);

        // updating note in db
        db.updateNote(n);

        // refreshing the list
        audiosList.set(position, n);
        mAdapter.notifyItemChanged(position);

        toggleEmptyAudios();
    }

    /**
     * Deleting note from SQLite and removing the
     * item from the list by its position
     */
    private void deleteAudio(int position) {

        File deletedFile = new File(file.getAbsolutePath() + File.separator + audiosList.get(position).getFilePath());
        deletedFile.delete();
        // deleting the note from db
        db.deleteAudio(audiosList.get(position));

        // removing the note from the list
        audiosList.remove(position);
        mAdapter.notifyItemRemoved(position);

        toggleEmptyAudios();
    }

    private void deleteAudio(Audio a) {
        // deleting the note from db
        db.deleteAudio(a);

        toggleEmptyAudios();
    }

    /**
     * Opens dialog with Edit - Delete options
     * Edit - 0
     * Delete - 1
     * Add Favorite List - 2
     */
    private void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Edit Note", "Delete Record", "Add Favorite List"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose option");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showNoteDialog(true, audiosList.get(position), position);
                } else if(which == 1) {
                    deleteAudio(position);
                }else{
                    updateFavorite(position);
                }
            }
        });
        builder.show();
    }

    /**
     * Updating audio favorite in db and updating
     * item in the list by its position
     */
    private void updateFavorite(int position) {
        Audio n = audiosList.get(position);

        n.setFavorite(1);
        // updating note in db
        db.updateNote(n);

        Toast.makeText(MainActivity.this, "Audio add into favorite list.", Toast.LENGTH_LONG).show();

        // refreshing the list
        audiosList.set(position, n);
        mAdapter.notifyItemChanged(position);

        toggleEmptyAudios();
    }


    /**
     * Shows alert dialog with EditText options to enter / edit
     * a audio.
     * when shouldUpdate=true, it automatically displays old audio and changes the
     * button text to UPDATE
     */
    private void showNoteDialog(final boolean shouldUpdate, final Audio audio, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.note_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilderUserInput.setView(view);

        final EditText inputNote = view.findViewById(R.id.note);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText(!shouldUpdate ? getString(R.string.lbl_new_note_title) : getString(R.string.lbl_edit_note_title));

        if (shouldUpdate && audio != null) {
            inputNote.setText(audio.getNote());
        }
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(shouldUpdate ? "update" : "save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {



                    }
                })
                .setNegativeButton("cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show toast message when no text is entered
                if (TextUtils.isEmpty(inputNote.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Enter note!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    alertDialog.dismiss();
                }

                // check if user updating audio
                if (shouldUpdate && audio != null) {
                    // update audio by it's id
                    updateNote(inputNote.getText().toString(), position);
                } else {
                    // create new audio
                    updateNote(inputNote.getText().toString(), position);
                }
            }
        });
    }

    /**
     * Toggling list and empty notes view
     */
    private void toggleEmptyAudios() {
        // you can check audiosList.size() > 0

        if (db.getAudiosCount() > 0) {
            noAudiosView.setVisibility(View.GONE);
        } else {
            noAudiosView.setVisibility(View.VISIBLE);
        }
    }




    //PERMISSION THINGS
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), PROCESS_OUTGOING_CALLS);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_STATE);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int result3 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result4 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int result5 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_CONTACTS);
        int result6 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_CONTACTS);


        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED &&
                result2 == PackageManager.PERMISSION_GRANTED && result3 == PackageManager.PERMISSION_GRANTED &&
                result4 == PackageManager.PERMISSION_GRANTED && result5 == PackageManager.PERMISSION_GRANTED &&
                result6 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{PROCESS_OUTGOING_CALLS, READ_PHONE_STATE, RECORD_AUDIO, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, READ_CONTACTS, WRITE_CONTACTS}, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case  PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted && cameraAccepted)
                        Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    else {

                        Toast.makeText(MainActivity.this, "Permission Denied !!!", Toast.LENGTH_LONG).show();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                                showMessageOKCancel("You need to allow access to all the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{PROCESS_OUTGOING_CALLS, READ_PHONE_STATE ,RECORD_AUDIO, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, READ_CONTACTS, WRITE_CONTACTS},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }


                break;
        }
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }



}



