package com.numad.firesidechat;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.numad.firesidechat.databinding.FragmentDashboardBinding;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This fragment is used to display the dashboard of the user.
 * From this dashboard, the user can access any chats they may have had with other users.
 * They can also search for other users to begin or continue a chat with.
 * Additionally, they can log out from the app.
 */
public class DashboardFragment extends Fragment implements RecyclerAdapter.OnNoteListener {
    private FragmentDashboardBinding binding;
    private SharedPreferences sharedPreferences;
    private RecyclerAdapter adapter;
    private String username;
    private FirebaseDatabaseManager databaseManager;
    private FragmentManager fragmentManager;
    private AlertDialog dialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);

        init();
        initDashboardWithDatabaseData();
        initListeners();

        return binding.getRoot();

    }

    /**
     * This function is used to initialise the UI, set up the shared preferences, and set up the
     * reference to the database.
     */
    private void init() {
        databaseManager = FirebaseDatabaseManager.getInstance();
        sharedPreferences = requireActivity().getSharedPreferences(LoginFragment.SHARED_PREFS_NAME, MODE_PRIVATE);
        fragmentManager = requireActivity().getSupportFragmentManager();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerAdapter(this);
        binding.recyclerView.setAdapter(adapter);
    }

    /**
     * This function is used to populate the recycler view with the data from the database.
     * It calls upon the message history object associated to the user from the Firebase database.
     * If the object is populated, the dashboard is populated with the data.
     * If the object is empty or does not exist, it means the user has not started a chat with
     * any other user and so the dashboard will be blank.
     */
    private void initDashboardWithDatabaseData() {
        username = sharedPreferences.getString(LoginFragment.NAME_PREF_NAME, "testUser");
        DatabaseReference dbRef = databaseManager.getDatabaseReference();

        // At this point, the database has a message history child.
        // We need to fetch the keys and values from the child.
        // The keys would be used to populate the recycler view.
        // The values will have some data about the chat that needs to be passed to the recycler view as well.
        dbRef.child(FirebaseDatabaseManager.MESSAGE_HISTORY_TAG).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot userMessageHistorySnapshot = task.getResult();
                if (userMessageHistorySnapshot.exists()) {
                    // We have entries for our user. We need to populate the recycler view.
                    dbRef.child(FirebaseDatabaseManager.MESSAGE_HISTORY_TAG).child(username)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    GenericTypeIndicator<HashMap<String, MessageHistoryObject>> t =
                                            new GenericTypeIndicator<>() {
                                            };
                                    HashMap<String, MessageHistoryObject> messageData = snapshot.getValue(t);
                                    if (messageData != null) {
                                        adapter.setChats(messageData);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(getContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            } else {
                Log.e("Dashboard", "Error getting database: " + task.getException());
                Toast.makeText(getContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This function is used to set up the listeners for the buttons.
     */
    private void initListeners() {
        binding.buttonNewMessage.setOnClickListener(v -> handleContactSearch());

        binding.user.setOnClickListener(v -> showLogoutDialog());
    }

    /**
     * This function is called when the user clicks on the search button to find a user.
     * If a user is found, a chat window is opened. If not, a toast is displayed to the user informing them
     * that there is no user by the name that they have searched. If a chat already exists between the 2 users,
     * the chat window is opened.
     */
    private void handleContactSearch() {
        // Login would have ensured that the database has a users child.
        // We simply need to parse through an available list of users to find a match.
        DatabaseReference dbRef = databaseManager.getDatabaseReference().child(FirebaseDatabaseManager.USERS_TAG);
        dbRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot usersSnapshot = task.getResult();
                if (usersSnapshot.exists()) {
                    // We have the fetch the data and find the keys. If the key matches with the
                    // user's query, then we can open up a chat window.
                    GenericTypeIndicator<HashMap<String, Object>> t =
                            new GenericTypeIndicator<>() {
                            };
                    HashMap<String, Object> usersData = usersSnapshot.getValue(t);
                    if (usersData != null) {
                        // Parse through the key set and identify a match.
                        Set<String> keys = usersData.keySet();
                        String recipientName = binding.editTextName.getText().toString().trim();
                        if (keys.stream().map(String::toLowerCase).collect(Collectors.toSet()).contains(recipientName.toLowerCase())) {
                            // Set up the communication objects
                            verifyExistingChat(recipientName);

                            // Move to the chat window
                            fragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container_view, ChatFragment.newInstance(username, recipientName))
                                    .addToBackStack("Chat Dashboard")
                                    .commit();
                        } else {
                            Toast.makeText(getContext(), getString(R.string.no_user_found), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Log.e("Dashboard", "Error getting database: " + task.getException());
                    Toast.makeText(getContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("Dashboard", "Error getting database: " + task.getException());
                Toast.makeText(getContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This function is used to set up the communication objects in the database if it is not already.
     * However, if it already exists, we can move ahead to the chat fragment without worry.
     */
    private void verifyExistingChat(String recipientName) {
        DatabaseReference dbRef = databaseManager.getDatabaseReference()
                .child(FirebaseDatabaseManager.MESSAGE_HISTORY_TAG);

        // We verify a 2-way communication object.
        initiateChatterRecipientDataConnection(dbRef, username, recipientName);
        initiateChatterRecipientDataConnection(dbRef, recipientName, username);

        binding.editTextName.setText("");
    }

    /**
     * This is a helper function that helps set up the communication objects in the database if it
     * is not already present. Since we choose to store the message history between 2 users for both
     * users, this function creates the object for @param name1 and @param name2.
     */
    private void initiateChatterRecipientDataConnection(DatabaseReference dbRef, String name1, String name2) {
        dbRef.child(name1).child(name2).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // We have reference to the database. We must check if the chat objects exist.
                DataSnapshot messageHistorySnapshot = task.getResult();
                if (!messageHistorySnapshot.exists()) {
                    // If the chat object does not exist, we create it.
                    MessageHistoryObject messageHistoryObject = new MessageHistoryObject();
                    dbRef.child(name1).child(name2).setValue(messageHistoryObject);
                }
                // In case the chat object exists, we can move ahead without worry.
            } else {
                Log.e("Dashboard", "Error getting database: " + task.getException());
                Toast.makeText(getContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This function triggers a dialog box to prompt the user to log out.
     */
    private void showLogoutDialog() {
        Context context = requireContext();
        if (dialog == null) {
            dialog = new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.log_out))
                    .setMessage(context.getString(R.string.log_out_message))
                    .setPositiveButton(context.getString(R.string.yes), (dialog, which) -> {
                        SharedPreferences preferences = context.getSharedPreferences(LoginFragment.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
                        preferences.edit().clear().apply();

                        fragmentManager.beginTransaction()
                                .replace(R.id.fragment_container_view, new LoginFragment())
                                .commit();
                    })
                    .setNegativeButton(context.getString(R.string.no), null)
                    .create();
        } else if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    /**
     * This function is called when a user clicks on a chat in the dashboard.
     * It calls upon the chat fragment to handle the communication.
     */
    public void onNoteClick(String recipientName) {
        // Move to the chat window
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, ChatFragment.newInstance(username, recipientName))
                .addToBackStack("Chat Dashboard")
                .commit();
    }
}
