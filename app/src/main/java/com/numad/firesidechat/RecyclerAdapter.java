package com.numad.firesidechat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.numad.firesidechat.databinding.ContactCardviewBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This adapter is used to help display the chats on the dashboard. It is fairly straightforward.
 * It takes in a list of message history objects and displays them in the recycler view.
 * */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private ContactCardviewBinding binding;
    private HashMap<String, MessageHistoryObject> chats;
    private final OnNoteListener onNoteListener;
    private List<String> keys;
    private List<MessageHistoryObject> values;

    public RecyclerAdapter(OnNoteListener onNoteListener) {
        this.onNoteListener = onNoteListener;
        chats = new HashMap<>();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setChats(HashMap<String, MessageHistoryObject> chats) {
        this.chats = chats;
        keys = new ArrayList<>(chats.keySet());
        values = new ArrayList<>(chats.values());
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        OnNoteListener onNoteListener;
        private final List<String> chats;

        public ViewHolder(@NonNull ContactCardviewBinding binding, OnNoteListener onNoteListener, List<String> chats) {
            super(binding.getRoot());
            this.chats = chats;
            this.onNoteListener = onNoteListener;
            binding.main.setOnClickListener(this);
        }

        public void onClick(View v) {
            onNoteListener.onNoteClick(chats.get(getAdapterPosition()));
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ContactCardviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding, this.onNoteListener, keys);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        binding.textViewName.setText(keys.get(position));
        Context context = binding.getRoot().getContext();
        if (!values.get(position).getMessagesSent().isEmpty()) {
            if (values.get(position).getNotificationTracker().isRead()) {
                binding.textViewStatus.setText(context.getString(R.string.read));
            } else {
                int count = values.get(position).getNotificationTracker().getCount();
                binding.textViewStatus.setText(context.getString(R.string.x_new_messages, count));
            }
        } else {
            binding.textViewStatus.setText(context.getString(R.string.start_a_conversation));
        }

    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public interface OnNoteListener {
        void onNoteClick(String recipientName);
    }
}
