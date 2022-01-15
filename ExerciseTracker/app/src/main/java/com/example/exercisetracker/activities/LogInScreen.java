package com.example.exercisetracker.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.exercisetracker.R;
import com.example.exercisetracker.other.User;
import com.example.exercisetracker.other.DBhelper;
import com.google.android.material.textfield.TextInputLayout;

/**
 * handling log in screen
 * Extends from AppCompatActivity class
 */

public class LogInScreen extends AppCompatActivity {
    //shared preferences strings
    private final static String remember_me = "remember";
    private final static String shared_prefs = "sharedPrefs";
    private TextInputLayout usernameField;
    private TextInputLayout passwordField;
    private CheckBox remember;

    public static String getRemember_me() {
        return remember_me;
    }

    public static String getShared_prefs() {
        return shared_prefs;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // if user has previously logged in, and selected "remember me"
        // user automatically logged into the app
        if (getUserSP()) {
            Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent1);
            finish();
        }
        super.onCreate(savedInstanceState);
        //visuals
        getSupportActionBar().hide();
        getWindow().setNavigationBarColor(getResources().getColor(R.color.main_colour));
        getWindow().setStatusBarColor(getResources().getColor(R.color.main_colour));
        setContentView(R.layout.activity_loginscreen);

        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        Button createbtn = findViewById(R.id.createaccount);
        Button loginbtn = findViewById(R.id.loginbtn);
        remember = findViewById(R.id.rememberBox);
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameField.getEditText().getText().toString();
                String password = passwordField.getEditText().getText().toString();
                //user validation here
                DBhelper helper = new DBhelper(LogInScreen.this);
                if (helper.login(username, password)) {
                    String[] results = helper.getResult().get(0).split(" ");
                    saveToUserClass(results, username, password);
                    if (remember.isChecked()) {
                        saveToSharedPreferences(results, username, password);
                    }
                    finish();
                    Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent1);
                }

            }
        });
        createbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), RegisterUserActivity.class);
                startActivity(intent1);
            }
        });

        remember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //using Android's SharedPreferences to store whether or not the user has requested
                //to stay logged in, even after closing the app
                if (buttonView.isChecked()) {
                    SharedPreferences prefs = getSharedPreferences(shared_prefs, MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(remember_me, true);
                    editor.apply();
                } else if (!buttonView.isChecked()) {
                    SharedPreferences prefs = getSharedPreferences(shared_prefs, MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(remember_me, false);
                    editor.apply();
                }
            }
        });
    }

    private void saveToUserClass(String[] results, String username, String password) {
        // results received in format ID, forename, surname, DOB, weight, height
        //saving to static User class
        User.setUsername(username);
        User.setPassword(password);
        User.setUserID(Integer.valueOf(results[0]));
        User.setForename(results[1]);
        User.setSurname(results[2]);
        java.sql.Date date = java.sql.Date.valueOf(results[3]);
        User.setDateOfBirth(date);
        User.setWeight(Float.valueOf(results[4]));
        User.setHeight(Integer.valueOf(results[5]));
    }

    public void saveToSharedPreferences(String[] results, String username, String password) {
        //saving to SharedPreferences
        // results received in format ID, forename, surname, DOB, weight, height
        SharedPreferences prefs = getSharedPreferences(shared_prefs, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.putString("id", results[0]);
        editor.putString("forename", results[1]);
        editor.putString("surname", results[2]);
        editor.putString("DOB", results[3]);
        editor.putString("weight", results[4]);
        editor.putString("height", results[5]);
        editor.apply();
    }

    private boolean getUserSP() {
        // getting saved user details from shared preferences
        // handles saving user details to User class from shared preferences
        SharedPreferences prefs = getSharedPreferences(shared_prefs, MODE_PRIVATE);
        Boolean checkbox = prefs.getBoolean(remember_me, false);
        if (checkbox.equals(true)) {
            prefs = getSharedPreferences(shared_prefs, MODE_PRIVATE);
            String[] results = {
                    prefs.getString("id", ""),
                    prefs.getString("forename", ""),
                    prefs.getString("surname", ""),
                    prefs.getString("DOB", ""),
                    prefs.getString("weight", ""),
                    prefs.getString("height", "")
            };
            saveToUserClass(results, prefs.getString("username", ""), prefs.getString("password", ""));
            return true;
        }
        return false;
    }
}