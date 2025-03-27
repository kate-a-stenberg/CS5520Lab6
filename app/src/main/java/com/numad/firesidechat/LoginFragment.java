package com.numad.firesidechat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.numad.firesidechat.databinding.FragmentLoginBinding;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * This fragment is used to log the user in.
 * The user can enter their name and email and will be logged in.
 * The database is queried to check if a users object exists. In case it does not,
 * a users object is created, the user is signed up, information stored within the users object — all
 * of it being stored in the Firebase database.
 * <br><br>However, if a users object already exists, it is queried to check if a user with the same
 * information exists, and if it does, the user is logged in. If it does not, a new user is created.
 */
public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private SharedPreferences sharedPreferences;
    private FirebaseDatabaseManager databaseManager;
    public static final String SHARED_PREFS_NAME = "FiresidePrefs";
    public static final String NAME_PREF_NAME = "username";
    public static final String JWT_PREF_NAME = "jwt_token";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init();
        initListeners();
    }

    /**
     * Function to initialize the shared preferences and database manager.
     */
    private void init() {
        sharedPreferences = requireActivity().getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        databaseManager = FirebaseDatabaseManager.getInstance();
    }

    /**
     * Function to initialise the onClick listeners for the login button.
     */
    private void initListeners() {
        binding.loginButton.setOnClickListener(v -> {
            String name = binding.etUsername.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(
                                getContext(),
                                getString(R.string.login_email_or_name_error),
                                Toast.LENGTH_SHORT)
                        .show();
                return;
            }

            String jwt = generateMockJWT(name, email);
            validateUserAndLogin(name, email, jwt);
        });
    }

    /**
     * This function is used to validate the user and login.
     * It fetches a reference to the "users" child of the database.
     * If the users child does not exist, it creates it.
     * <br><br>
     * Within that, it searches for users with the given name.
     * When it identifies a user with the same name, it logs the user in and stores their JWT.
     * <br><br>
     * In case the user does not exist or the email is different, a new user is created and logged in.
     */
    private void validateUserAndLogin(String name, String email, String jwt) {
        DatabaseReference dbRef = databaseManager.getDatabaseReference().child(FirebaseDatabaseManager.USERS_TAG);

        // Verify if the database has a users child.
        dbRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot usersSnapshot = task.getResult();
                Map<String, Object> userData = new HashMap<>();
                userData.put(FirebaseDatabaseManager.EMAIL_TAG, email);
                userData.put(FirebaseDatabaseManager.JWT_TAG, jwt);

                if (!usersSnapshot.exists()) {
                    // Create Users child and sign user up.

                    dbRef.child(name).setValue(userData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), getString(R.string.signup_success), Toast.LENGTH_SHORT).show();
                                sharedPreferences.edit().putString(NAME_PREF_NAME, name).apply();
                                sharedPreferences.edit().putString(JWT_PREF_NAME, jwt).apply();
                                moveToDashboard();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Login", "Error logging in: " + e.getMessage());
                                Toast.makeText(getContext(), getString(R.string.login_failure), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    // The child named users exists in the database.
                    // We can proceed with the login process.
                    DataSnapshot dataSnapshot = task.getResult();
                    if (dataSnapshot.exists()) {
                        GenericTypeIndicator<HashMap<String, HashMap<String, String>>> t = new GenericTypeIndicator<>() {
                        };
                        HashMap<String, HashMap<String, String>> usersData = dataSnapshot.getValue(t);
                        if (usersData != null && usersData.containsKey(name)) {
                            // User exists
                            dbRef.child(name).setValue(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                                        sharedPreferences.edit().putString(NAME_PREF_NAME, name).apply();
                                        sharedPreferences.edit().putString(JWT_PREF_NAME, jwt).apply();
                                        moveToDashboard();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Login", "Error logging in: " + e.getMessage());
                                        Toast.makeText(getContext(), getString(R.string.login_failure), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            // If the user does not exist or a user exists with the same name but email is different,
                            // we could create a new user with the same name but different email.

                            dbRef.child(name).setValue(userData)
                                    .addOnCompleteListener(createTask -> {
                                        if (createTask.isSuccessful()) {
                                            Toast.makeText(getContext(), getString(R.string.signup_success), Toast.LENGTH_SHORT).show();
                                            sharedPreferences.edit().putString(NAME_PREF_NAME, name).apply();
                                            sharedPreferences.edit().putString(JWT_PREF_NAME, jwt).apply();
                                            moveToDashboard();
                                        } else {
                                            Log.e("Login", "Error creating user: " + createTask.getException());
                                            Toast.makeText(getContext(), getString(R.string.login_failure), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Log.e("Login", "Error getting database: " + task.getException());
                        Toast.makeText(getContext(), getString(R.string.login_failure), Toast.LENGTH_SHORT).show();
                    }
                }
                // The child object exists, and we can proceed.
            } else {
                Log.e("Login", "Error getting database: " + task.getException());
                Toast.makeText(getContext(), getString(R.string.login_failure), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Using this function to move to the dashboard.
     */
    private void moveToDashboard() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_view, new DashboardFragment())
                .commit();
    }

    /**
     * Basic function to generate JWT using name email and timestamp, then encode base64
     */
    private String generateMockJWT(String name, String email) {
        String payload = name + ":" + email + ":" + System.currentTimeMillis();
        return Base64.encodeToString(payload.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
    }
}
