package com.zen.callrecorder.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zen.callrecorder.Database.Model.Audio;
import com.zen.callrecorder.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AudioRecyclerAdapter extends RecyclerView.Adapter<AudioRecyclerAdapter.MyViewHolder> {

    private Context context;
    private List<Audio> audioList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView contactName;
        public TextView contactNumber;
        public TextView note;
        public TextView dot;
        public TextView timestamp;
        public TextView duration;

        public MyViewHolder(View view) {
            super(view);
            contactName = view.findViewById(R.id.contact_name);
            contactNumber = view.findViewById(R.id.contact_number);
            note = view.findViewById(R.id.note);
            dot = view.findViewById(R.id.dot);
            timestamp = view.findViewById(R.id.timestamp);
            duration = view.findViewById(R.id.duration);
        }
    }


    public AudioRecyclerAdapter(Context context, List<Audio> audioList) {
        this.context = context;
        this.audioList = audioList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.audio_recyclerview_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Audio audio = audioList.get(position);

        holder.contactName.setText(audio.getContactName());

        holder.contactNumber.setText(audio.getContactNumber());

        holder.duration.setText(audio.getDuration());

        holder.note.setText(audio.getNote());

        // Displaying dot from HTML character code
        holder.dot.setText(Html.fromHtml("&#8226;"));

        // Formatting and displaying timestamp
        holder.timestamp.setText(formatDate(audio.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return audioList.size();
    }

    public void setFilter(ArrayList<Audio> newList){

        audioList = new ArrayList<>();
        audioList.addAll(newList);
        notifyDataSetChanged();
    }

    /**
     * Formatting timestamp to `MMM d` format
     * Input: 2018-02-21 00:15:42
     * Output: Feb 21
     */
    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd hh.mm.ss");
            Date date = fmt.parse(dateStr);
            SimpleDateFormat fmtOut = new SimpleDateFormat("MMM d HH:mm");
            return fmtOut.format(date);
        } catch (ParseException e) {

        }

        return "";
    }
}