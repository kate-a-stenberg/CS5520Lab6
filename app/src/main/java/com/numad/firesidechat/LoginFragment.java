package com.numad.firesidechat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LoginFragment extends Fragment {

    private EditText usernameField, emailField;
    private Button loginButton;
    private SharedPreferences sharedPreferences;

    public LoginFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        usernameField = view.findViewById(R.id.et_username);
        emailField = view.findViewById(R.id.et_email);
        loginButton = view.findViewById(R.id.login_button);
        sharedPreferences = requireActivity().getSharedPreferences("FiresidePrefs", Context.MODE_PRIVATE);

        loginButton.setOnClickListener(v -> {
            String name = usernameField.getText().toString().trim();
            String email = emailField.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(getContext(), "Please enter name and email", Toast.LENGTH_SHORT).show();
                return;
            }

            String jwt = generateMockJWT(name, email);
            sharedPreferences.edit().putString("jwt_token", jwt).apply();

            // Firebase DB write
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("fireside_chat").child("users").child(name);
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", email);
            userData.put("jwt", jwt);
            dbRef.updateChildren(userData);

            Toast.makeText(getContext(), "Login Success", Toast.LENGTH_SHORT).show();

            // TODO: Navigate to next screen/fragment
        });
    }

    //basic function to generate JWT using name email and timestamp, then encode base64
    private String generateMockJWT(String name, String email) {
        String payload = name + ":" + email + ":" + System.currentTimeMillis();
        return Base64.encodeToString(payload.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
    }
}
