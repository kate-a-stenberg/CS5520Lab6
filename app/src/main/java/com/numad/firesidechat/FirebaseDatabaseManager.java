package com.numad.firesidechat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * We use a singleton object to gain access to the Firebase Database.
 * We realised that we have to call this database multiple times. It did not make sense to
 * have multiple references to the same database.
 */
public class FirebaseDatabaseManager {

    private static FirebaseDatabaseManager instance;
    private final DatabaseReference databaseReference;
    private static final String REFERENCE_TAG = "fireside_chat";
    public static final String USERS_TAG = "users";
    public static final String EMAIL_TAG = "email";
    public static final String JWT_TAG = "jwt";
    public static final String MESSAGE_HISTORY_TAG = "messageHistory";
    public static final String MESSAGES_TAG = "messagesSent";

    /**
     * The constructor will create a reference to the Firebase Database.
     * This reference can be accessed via {@link #getDatabaseReference()} getDatabaseReference().
     */
    private FirebaseDatabaseManager() {
        databaseReference = FirebaseDatabase.getInstance().getReference(REFERENCE_TAG);
    }

    /**
     * This synchronized function will create an instance of the FirebaseDatabaseManager class.
     * It ensures that only one instance is created. The synchronized keyword ensures that only
     * one instance is created even if multiple threads are trying to access it.
     * <br><br>
     * We have seen this structure with the Room Database as well.
     */
    public static synchronized FirebaseDatabaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseDatabaseManager();
        }
        return instance;
    }

    /**
     * This function will return the reference to the Firebase Database.
     */
    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }
}
