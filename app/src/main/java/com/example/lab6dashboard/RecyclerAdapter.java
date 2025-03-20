package com.example.lab6dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private ArrayList<Person> people;
    private OnNoteListener onNoteListener;

    public RecyclerAdapter() {
        super();

        people = new ArrayList<Person>();

        people.add(new Person("Kate", "2 unread"));
        people.add(new Person("Hakshay", "1 unread"));
        people.add(new Person("Sean", "Read"));
        people.add(new Person("Dr. G", "1 unread"));
        people.add(new Person("Ryan", "Sent"));
        people.add(new Person("Obama", "Sent"));
        people.add(new Person("John Cena", "1 unread"));

    }

    public RecyclerAdapter(ArrayList<Person> people, OnNoteListener onNoteListener) {
        this.people = people;

        people.add(new Person("Kate", "2 unread"));
        people.add(new Person("Hakshay", "1 unread"));
        people.add(new Person("Sean", "Read"));
        people.add(new Person("Dr. G", "1 unread"));
        people.add(new Person("Ryan", "Sent"));
        people.add(new Person("Obama", "Sent"));
        people.add(new Person("John Cena", "1 unread"));

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
        Person person = people.get(position);
        holder.icon.setImageResource(people.get(position).getIcon());
        holder.name.setText(people.get(position).getName());
        holder.status.setText(people.get(position).getStatus());
    }

    @Override
    public int getItemCount() {
        return people.size();
    }

    public interface OnNoteListener {
        void onNoteClick(int position);
    }

}
