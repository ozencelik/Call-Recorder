package com.zen.callrecorder.Activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.zen.callrecorder.Database.DatabaseHelper;
import com.zen.callrecorder.Database.Model.Audio;
import com.zen.callrecorder.R;
import com.zen.callrecorder.Utils.MyDividerItemDecoration;
import com.zen.callrecorder.Utils.RecyclerTouchListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.zen.callrecorder.Constant.AUDIO_FILE_PATH;

/**
 * Created by Ozenc Celik on 10/25/2018
 */
public class FavoriteActivity extends AppCompatActivity implements Runnable {

    private static final String TAG = FavoriteActivity.class.getSimpleName();

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    private AudioRecyclerAdapter mAdapter;
    private List<Audio> favoriteAudiosList = new ArrayList<>();
    private TextView noFavoriteAudiosView;

    public static DatabaseHelper db;

    //Media Player Variables
    private static MediaPlayer mediaPlayer = new MediaPlayer();
    private SeekBar seekBar;
    private Button buttonPlayPause, dismissButton;
    private TextView txt;
    private boolean wasPlaying = false;
    private File file;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        setupActionBar();

        initComponents();

        clickListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        FavoriteActivity.this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearMediaPlayer();
        FavoriteActivity.this.finish();
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

            new AlertDialog.Builder(FavoriteActivity.this)
                    .setTitle("Favorited Record List")
                    .setMessage("These are the recordings you have favorited.\n" +
                            "Deleting them from here will delete them from favorite list not from the SD Card.")
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

    //Initialize Components
    public void initComponents(){
        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        recyclerView = findViewById(R.id.recycler_view);
        noFavoriteAudiosView = findViewById(R.id.empty_notes_view);

        db = new DatabaseHelper(this);
        favoriteAudiosList.addAll(db.getAllAudios(3));

        mAdapter = new AudioRecyclerAdapter(this, favoriteAudiosList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);

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

                favoriteAudiosList.clear();
                favoriteAudiosList.addAll(db.getAllAudios(3));
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

                file = new File(AUDIO_FILE_PATH);

                final String filePath = file.getAbsolutePath() + "/" + favoriteAudiosList.get(position).getFilePath();
                //final String filePath = audiosList.get(position).getFilePath();

                final Dialog dialog = new Dialog(FavoriteActivity.this);
                dialog.setContentView(R.layout.popup_audios);
                dialog.setTitle("Title...");
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                buttonPlayPause = dialog.findViewById(R.id.playButton);
                dismissButton = dialog.findViewById(R.id.dismissButton);
                seekBar = dialog.findViewById(R.id.seekbar);
                txt = dialog.findViewById(R.id.audio_name);
                txt.setText(favoriteAudiosList.get(position).getFilePath());
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
                            buttonPlayPause.setBackground(ContextCompat.getDrawable(FavoriteActivity.this, R.drawable.play));
                            FavoriteActivity.this.seekBar.setProgress(0);
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



    public void playSong(String filePath) {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                clearMediaPlayer();
                seekBar.setProgress(0);
                wasPlaying = true;
                buttonPlayPause.setBackground(ContextCompat.getDrawable(FavoriteActivity.this, R.drawable.play));
            }
            if (!wasPlaying) {

                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                }

                buttonPlayPause.setBackground(ContextCompat.getDrawable(FavoriteActivity.this, R.drawable.pause));

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


    @Override
    public void onBackPressed() {
        startActivity(new Intent(FavoriteActivity.this, MainActivity.class));
        finish();
    }

    private void setupActionBar() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    //DATABASE THINKS

    private void deleteFavorite(int position) {
        Audio n = favoriteAudiosList.get(position);

        n.setFavorite(0);
        // updating note in db
        db.updateNote(n);

        // refreshing the list
        favoriteAudiosList.remove(position);
        mAdapter.notifyItemChanged(position);

        toggleEmptyAudios();
    }

    private void updateNote(String note, int position) {
        Audio n = favoriteAudiosList.get(position);
        // updating note text
        n.setNote(note);

        // updating note in db
        db.updateNote(n);

        // refreshing the list
        favoriteAudiosList.set(position, n);
        mAdapter.notifyItemChanged(position);

        toggleEmptyAudios();
    }

    private void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Edit Note", "Delete From Here"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose option");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showNoteDialog(true, favoriteAudiosList.get(position), position);
                } else if(which == 1) {
                    deleteFavorite(position);
                }
            }
        });
        builder.show();
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

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(FavoriteActivity.this);
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
                    Toast.makeText(FavoriteActivity.this, "Enter audio!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    alertDialog.dismiss();
                }

                // check if user updating audio
                if (shouldUpdate && audio != null) {
                    // update audio by it's id
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
        noFavoriteAudiosView.setText("No favorite audio found!");
        if (favoriteAudiosList.size() > 0) {
            noFavoriteAudiosView.setVisibility(View.GONE);
        } else {
            noFavoriteAudiosView.setVisibility(View.VISIBLE);
        }
    }
}
