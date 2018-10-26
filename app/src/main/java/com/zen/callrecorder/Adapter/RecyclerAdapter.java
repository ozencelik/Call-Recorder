package com.zen.callrecorder.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zen.callrecorder.Database.Model.Audio;
import com.zen.callrecorder.Database.Model.Contact;
import com.zen.callrecorder.R;

import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

    private Context context;
    private List<Contact> contactList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView contactName;
        public TextView contactNumber;
        private TextView dot;

        public MyViewHolder(View view) {
            super(view);
            contactName = view.findViewById(R.id.contact_name);
            contactNumber = view.findViewById(R.id.contact_number);
            dot = view.findViewById(R.id.dot);
        }
    }


    public RecyclerAdapter(Context context, List<Contact> contactList) {
        this.context = context;
        this.contactList = contactList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        final Contact contact = contactList.get(position);

        holder.contactName.setText(contact.getContactName());

        holder.contactNumber.setText(contact.getContactNumber());

        // Displaying dot from HTML character code
        holder.dot.setText(Html.fromHtml("&#8226;"));
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

}
