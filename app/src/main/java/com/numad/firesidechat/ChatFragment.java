package com.numad.firesidechat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.numad.firesidechat.databinding.FragmentChatBinding;

import java.util.List;

/**
 * This fragment is used to display the chat between two users.
 * It contains a RecyclerView that displays the messages between the two users.
 * It handles fetching the chat data between 2 users and displaying it in the RecyclerView.
 * It also handles sending messages between the two users.
 */
public class ChatFragment extends Fragment implements MessageAdapter.OnMessageLongClickListener {
    private static final String CHATTER_NAME = "chatterName";
    private static final String RECIPIENT_NAME = "recipientName";

    private FragmentChatBinding binding;
    private String username;
    private String recipientName;
    private MessageAdapter messageAdapter;

    private FirebaseDatabaseManager databaseManager;

    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param chatter   This represents the username
     * @param recipient This represents the name of the person with whom the user wants to chat
     * @return A new instance of fragment ChatFragment.
     */
    public static ChatFragment newInstance(String chatter, String recipient) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(CHATTER_NAME, chatter);
        args.putString(RECIPIENT_NAME, recipient);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            username = getArguments().getString(CHATTER_NAME);
            recipientName = getArguments().getString(RECIPIENT_NAME);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
        fetchMessages();
        initListeners();
    }

    /**
     * This function is responsible for initializing the UI.
     * It also gets a reference to the database.
     * */
    private void init() {
        binding.recipientName.setText(recipientName);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerView.setLayoutManager(layoutManager);
        messageAdapter = new MessageAdapter(username, this);
        binding.recyclerView.setAdapter(messageAdapter);

        databaseManager = FirebaseDatabaseManager.getInstance();
    }

    /**
     * This function is responsible for fetching the messages between 2 users.
     * Since the messages are tracked for both users, fetching the list from one user is enough.
     * While we track the timestamp of the message, since the message is tracked for both users,
     * Firebase handles ordering the messages.
     * */
    private void fetchMessages() {
        DatabaseReference dbRef = databaseManager.getDatabaseReference().child(FirebaseDatabaseManager.MESSAGE_HISTORY_TAG);

        // Adding a Value Listener to the chat object to keep updating the latest messages
        dbRef.child(username).child(recipientName).child(FirebaseDatabaseManager.MESSAGES_TAG).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GenericTypeIndicator<List<Message>> t = new GenericTypeIndicator<>() {
                };
                List<Message> messages = snapshot.getValue(t);
                messageAdapter.setMessages(messages);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChatFragment", "Error fetching messages: " + error.getMessage());
                Toast.makeText(getContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }
        });

        // Updating the notification tracker to reflect that a new message has been read
        dbRef.child(username).child(recipientName)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot messageHistorySnapshot = task.getResult();
                        if (messageHistorySnapshot.exists()) {
                            GenericTypeIndicator<MessageHistoryObject> t = new GenericTypeIndicator<>() {
                            };
                            MessageHistoryObject messageHistoryObject = messageHistorySnapshot.getValue(t);
                            assert messageHistoryObject != null;
                            messageHistoryObject.markNotificationAsRead();
                            dbRef.child(username).child(recipientName).setValue(messageHistoryObject);
                        }
                    } else {
                        Log.e("ChatFragment", "Error getting notification tracker: " + task.getException());
                        Toast.makeText(getContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * This function sets up the listeners for the send button and back button.
     * */
    private void initListeners() {
        binding.sendBtn.setOnClickListener(v -> {
            String messageText = binding.messageInput.getText().toString();
            Message message = new Message(username, messageText, System.currentTimeMillis());
            sendMessage(message, username, recipientName, false);
            sendMessage(message, recipientName, username, true);
            binding.messageInput.setText("");
        });

        binding.back.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    /**
     * This function is responsible for sending a message between 2 users @param name1 and @param name2.
     * Since the database is designed such that the same message is tracked in the message list
     * of both users, it is important to add the message to both users.
     * <br><br>If @param markNotification is true, it will mark the notification tracker as read.
     * If user 1 sends a message to user 2, this function will modify the notificationTracker object
     * of user 2 to reflect that a new message has been sent.
     * */
    private void sendMessage(Message message, String name1, String name2, boolean markNotification) {
        DatabaseReference dbRef = databaseManager.getDatabaseReference().child(FirebaseDatabaseManager.MESSAGE_HISTORY_TAG);

        dbRef.child(name1).child(name2).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                MessageHistoryObject messageHistoryObject = getMessageHistoryObject(task);

                messageHistoryObject.addMessage(message);
                if (markNotification) {
                    messageHistoryObject.incrementNotificationCount();
                    messageHistoryObject.resetNotificationReadStatus();
                }
                dbRef.child(name1).child(name2).setValue(messageHistoryObject);
            } else {
                Log.e("ChatFragment", "Error getting messages: " + task.getException());
                Toast.makeText(getContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This is a helper function. It is used to get the message history object from the database for
     * any given user. If the message history object does not exist, it is created.
     * This object is passed back to the {@link #sendMessage} function.
     * */
    @NonNull
    private static MessageHistoryObject getMessageHistoryObject(Task<DataSnapshot> task) {
        DataSnapshot dataSnapshot = task.getResult();
        MessageHistoryObject messageHistoryObject;
        if (dataSnapshot.exists()) {
            GenericTypeIndicator<MessageHistoryObject> t = new GenericTypeIndicator<>() {
            };
            messageHistoryObject = dataSnapshot.getValue(t);
            assert messageHistoryObject != null;
        } else {
            // Create a new chat object if it doesn't exist
            messageHistoryObject = new MessageHistoryObject();
        }
        return messageHistoryObject;
    }

    /**
     * This function is responsible for deleting a message between 2 users @param name1 and @param name2.
     * Since our database tracks the same message in the list for both users, it is important to
     * remove the message from both users.
     * <br><br>
     * Since the same message object is being passed around, a simple remove operation will work.
     * */
    private void deleteMessage(Message message, String name1, String name2) {
        DatabaseReference dbRef = databaseManager.getDatabaseReference().child(FirebaseDatabaseManager.MESSAGE_HISTORY_TAG);

        dbRef.child(name1).child(name2).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot dataSnapshot = task.getResult();
                if (dataSnapshot.exists()) {
                    GenericTypeIndicator<MessageHistoryObject> t = new GenericTypeIndicator<>() {
                    };
                    MessageHistoryObject messageHistoryObject = dataSnapshot.getValue(t);
                    assert messageHistoryObject != null;
                    messageHistoryObject.findAndRemoveMessage(message);
                    dbRef.child(name1).child(name2).setValue(messageHistoryObject);
                } else {
                    Log.e("ChatFragment", "Error getting messages: " + task.getException());
                    Toast.makeText(getContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("ChatFragment", "Error getting messages: " + task.getException());
                Toast.makeText(getContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Function to handle a short click on a message.
     * This prompts a toast message to the user informing them to hold it to delete it.
     * */
    @Override
    public void onMessageShortPress() {
        Toast.makeText(getContext(), getString(R.string.hold_to_delete), Toast.LENGTH_SHORT).show();
    }

    /**
     * Function to handle a long click on a message.
     * It deletes the message from the database and the RecyclerView.
     * */
    @Override
    public void onMessageLongPress(Message message) {
        deleteMessage(message, username, recipientName);
        deleteMessage(message, recipientName, username);
        Toast.makeText(getContext(), getString(R.string.message_deleted), Toast.LENGTH_SHORT).show();
    }
}
