package com.numad.firesidechat;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.numad.firesidechat.databinding.MessageCardBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * This adapter is used to help display the messages in the chat fragment.
 * It handles placing the message to the left or right depending upon the owner of the message —
 * who is the sender of the message.
 * <br><br>It also passes the click of a message to delete it from the database.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private MessageCardBinding binding;
    private final OnMessageLongClickListener onMessageLongClickListener;
    private List<Message> messages;
    private final String userName;

    public MessageAdapter(String userName, OnMessageLongClickListener onMessageLongClickListener) {
        this.userName = userName;
        this.onMessageLongClickListener = onMessageLongClickListener;
        messages = new ArrayList<>();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = MessageCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MessageViewHolder(binding, onMessageLongClickListener, messages);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message currentMessage = messages.get(position);

        // Define the look of the message card here
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int maxWidth = (int) (screenWidth * 0.75);

        binding.message.setMaxWidth(maxWidth);

        // Set the message text
        binding.message.setText(currentMessage.getMessage());

        // Aligning the chat bubble to the right if the sender is the current user
        ViewGroup.LayoutParams lp = binding.messageCard.getLayoutParams();
        if (lp instanceof ConstraintLayout.LayoutParams) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) lp;
            if (userName.equals(currentMessage.getSender())) {
                params.horizontalBias = 1.0f;  // 1.0f means aligned to end/right
            } else {
                params.horizontalBias = 0.0f;  // 0.0f means aligned to start/left
            }
            binding.messageCard.setLayoutParams(params);
        }
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder
            implements View.OnLongClickListener, View.OnClickListener {
        private final OnMessageLongClickListener onMessageLongClickListener;
        private final List<Message> messagesCopy;

        public MessageViewHolder(@NonNull MessageCardBinding binding,
                                 OnMessageLongClickListener onMessageLongClickListener,
                                 List<Message> m) {
            super(binding.getRoot());
            this.onMessageLongClickListener = onMessageLongClickListener;
            messagesCopy = m;
            binding.getRoot().setOnLongClickListener(this);
            binding.getRoot().setOnClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            onMessageLongClickListener.onMessageLongPress(messagesCopy.get(getAdapterPosition()));
            return false;
        }

        @Override
        public void onClick(View view) {
            onMessageLongClickListener.onMessageShortPress();
        }
    }

    public interface OnMessageLongClickListener {
        void onMessageShortPress();

        void onMessageLongPress(Message message);
    }
}
