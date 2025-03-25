package com.numad.firesidechat;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private ArrayList<Contact> contacts;
    private OnNoteListener onNoteListener;
    FirebaseDatabase database;
    DatabaseReference dbRef;

    public RecyclerAdapter() {
        super();

        /*
        contacts = new ArrayList<Contact>();
        contacts.add(new Contact("Kate", "2 unread"));
        contacts.add(new Contact("Hakshay", "1 unread"));
        contacts.add(new Contact("Sean", "Read"));
        contacts.add(new Contact("Dr. G", "1 unread"));
        contacts.add(new Contact("Ryan", "Sent"));
        contacts.add(new Contact("Obama", "Sent"));
        contacts.add(new Contact("John Cena", "1 unread"));
         */

    }

    public RecyclerAdapter(ArrayList<Contact> contacts, OnNoteListener onNoteListener) {
        this.contacts = contacts;

        /*
        contacts.add(new Contact("Kate", "2 unread"));
        contacts.add(new Contact("Hakshay", "1 unread"));
        contacts.add(new Contact("Sean", "Read"));
        contacts.add(new Contact("Dr. G", "1 unread"));
        contacts.add(new Contact("Ryan", "Sent"));
        contacts.add(new Contact("Obama", "Sent"));
        contacts.add(new Contact("John Cena", "1 unread"));
        */

        this.onNoteListener = onNoteListener;

    }

    public RecyclerAdapter(OnNoteListener onNoteListener) {
        this.onNoteListener = onNoteListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView icon;
        TextView name;
        TextView status;

        OnNoteListener onNoteListener;

        public ViewHolder(@NonNull View itemView, OnNoteListener onNoteListener) {
            super(itemView);

            icon = itemView.findViewById(R.id.imageViewIcon);
            name = itemView.findViewById(R.id.textViewName);
            status = itemView.findViewById(R.id.textViewStatus);

            itemView.setOnClickListener(this);
            this.onNoteListener = onNoteListener;

        }

        public void onClick(View v) {
            this.onNoteListener.onNoteClick(getAdapterPosition());
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_cardview, parent, false);
        return new ViewHolder(v, this.onNoteListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.icon.setImageResource(R.drawable.usericon);

//        holder.name.setText(dbRef.child("messageHistory").child("user0").child("user1")); // this should go through the users path. conditional?
//        if ( dbRef.child("messageHistory").child("user0").child("user1").child("notificationTracker").child("isRead") == false) {
//            holder.status.setText(dbRef.child("messageHistory").child("user0").child("user1").child("notificationTracker").child("count"));
//            holder.status.append(" new message(s)");
//            holder.status.setTypeface(holder.status.getTypeface(), Typeface.BOLD);
//        }
//        if ( dbRef.child("messageHistory").child("user0").child("user1").child("notificationTracker").child("isRead") == true) {
//            holder.status.setText("Read");
//        }

        // if there are no new messages:
            // holder.status.setText("Read");
        // if there are new messages:
            // holder.status.setText(dbRef.child("user1").child("contacts").child("status"));
        // how do we want to detect unread messages?
        // create Message class object with a read/unread attribute that toggles when the user opens the window?

    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public interface OnNoteListener {
        void onNoteClick(int position);
    }

}