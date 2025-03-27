package com.numad.firesidechat;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentManager;

/**
 * The MainActivity class is the entry point of the application.
 * It decides which fragment to show based on the user's login status.
 * If the user is already logged in, it shows the DashboardFragment.
 * If not, the LoginFragment is shown.
 */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        SharedPreferences sharedPreferences = getSharedPreferences(LoginFragment.SHARED_PREFS_NAME, MODE_PRIVATE);

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (sharedPreferences.contains(LoginFragment.JWT_PREF_NAME)) {
            // The user is already logged in. Take them to the dashboard.
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view, new DashboardFragment())
                    .commit();
        } else {
            // The user is not logged in. Show the login screen.
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view, new LoginFragment())
                    .commit();
        }
    }
}
