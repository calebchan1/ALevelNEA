package com.example.exercisetracker.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.exercisetracker.R;
import com.example.exercisetracker.activities.MainActivity;
import com.example.exercisetracker.fragments.HistoryFragment;
import com.example.exercisetracker.other.DBhelper;
import com.example.exercisetracker.other.User;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Objects;

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

        //referencing all variables
        ProgressBar progressBar = findViewById(R.id.progressBar);
        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        Button createbtn = findViewById(R.id.createaccount);
        Button loginbtn = findViewById(R.id.loginbtn);
        remember = findViewById(R.id.rememberBox);
        TextView repo = findViewById(R.id.githubRepo);
        repo.setText(Html.fromHtml("<a href='https://github.com/calebchan1/ALevelNEA'> github.com/calebchan1/ALevelNEA </a>"));
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = Objects.requireNonNull(usernameField.getEditText()).getText().toString();
                String password = Objects.requireNonNull(passwordField.getEditText()).getText().toString();
                //user validation here
                DBhelper helper = new DBhelper(LogInScreen.this);
                if (helper.login(username, password)) {
                    String[] results = helper.getResult().get(0).split(" ");
                    saveToUserClass(results, username, password);
                    if (remember.isChecked()) {
                        saveToSharedPreferences(results, username, password);
                    }
                    //user details were valid, navigating into app
                    Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent1);
                    finish();
                }
            }
        });
        createbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if user wants to create an account, navigate to RegisterUserActivity
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

    private void saveToSharedPreferences(String[] results, String username, String password) {
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

    public boolean getUserSP() {
        // getting saved user details from shared preferences
        // handles saving user details to User class from shared preferences
        SharedPreferences prefs = getSharedPreferences(shared_prefs, MODE_PRIVATE);
        Boolean checkbox = prefs.getBoolean(remember_me, false);

        if (checkbox.equals(true)) {
            try {
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
            } catch (Exception e) {
                return false;
            }

        }
        return false;
    }

//    //Async task handling logging in
//    //using async task to retrieve data from database
//    private class LogInTask extends AsyncTask<String, Integer, Boolean> {
//        protected Boolean doInBackground(String... args) {
//            String username = args[0];
//            String password = args[1];
//            //CHECKING IF NOTHING HAS BEEN ENTERED
//            if (username.isEmpty() | password.isEmpty()){
//                return false;
//            }
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    progressBar.setVisibility(View.VISIBLE);
//                }
//            });
//
//            DBhelper helper = new DBhelper(LogInScreen.this);
//            if (helper.login(username, password)) {
//                String[] results = helper.getResult().get(0).split(" ");
//                saveToUserClass(results, username, password);
//                if (remember.isChecked()) {
//                    saveToSharedPreferences(results, username, password);
//                }
//                return true;
//            }
//            return false;
//        }
//
//
//        protected void onPostExecute(Boolean queryResults) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    progressBar.setVisibility(View.GONE);
//                }
//            });
//            if (queryResults) {
//                Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
//                startActivity(intent1);
//                finish();
//            }
//            else{
//                Toast.makeText(LogInScreen.this, "Login Unsuccessful", Toast.LENGTH_SHORT).show();
//            }
//        }
//
//    }
}