package com.example.exercisetracker.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.exercisetracker.R;
import com.example.exercisetracker.other.User;
import com.example.exercisetracker.other.dbhelper;
import com.google.android.material.textfield.TextInputLayout;

/**
 * handling log in screen
 * Extends from AppCompatActivity class
 */

public class LogInScreen extends AppCompatActivity {
    private TextInputLayout usernameField;
    private TextInputLayout passwordField;
    private CheckBox remember;
    private Button loginbtn;
    private Button createbtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //visuals
        getSupportActionBar().hide();
        getWindow().setNavigationBarColor(getResources().getColor(R.color.main_colour));
        getWindow().setStatusBarColor(getResources().getColor(R.color.main_colour));
        setContentView(R.layout.activity_loginscreen);

        getUserSP();
        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        createbtn = findViewById(R.id.createaccount);
        loginbtn = findViewById(R.id.loginbtn);
        remember = findViewById(R.id.rememberBox);
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameField.getEditText().getText().toString();
                String password = passwordField.getEditText().getText().toString();
                //user validation here
                dbhelper helper = new dbhelper(LogInScreen.this);
                if (helper.login(username, password)) {
                    String[] results = helper.getResult().get(0).split(" ");
                    saveToUserClass(results, username, password);
                    saveToSharedPreferences(results);
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
                    SharedPreferences prefs = getSharedPreferences("checkbox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("remember", "true");
                    editor.apply();
                    Toast.makeText(LogInScreen.this, "Checked", Toast.LENGTH_SHORT).show();
                } else if (!buttonView.isChecked()) {
                    SharedPreferences prefs = getSharedPreferences("checkbox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("remember", "false");
                    editor.apply();
                    Toast.makeText(LogInScreen.this, "Unchecked", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveToUserClass(String[] results, String username, String password){
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

    private void saveToSharedPreferences(String[] results){
        //saving to SharedPreferences
        // results received in format ID, forename, surname, DOB, weight, height
        SharedPreferences prefs = getSharedPreferences("userdetails", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("id", results[0]);
        editor.putString("forename", results[1]);
        editor.putString("surname",results[2]);
        editor.putString("DOB", results[3]);
        editor.putString("weight", results[4]);
        editor.putString("height", results[5]);
        editor.apply();
    }

    private void getUserSP(){
        // getting saved user details from shared preferences
        SharedPreferences prefs = getSharedPreferences("checkbox",MODE_PRIVATE);
        String checkbox = prefs.getString("remember","");
        if (checkbox.equals("true")){
            prefs = getSharedPreferences("userdetails",MODE_PRIVATE);
            String[] results = {
                    prefs.getString("id",""),
                    prefs.getString("forename",""),
                    prefs.getString("surname",""),
                    prefs.getString("DOB",""),
                    prefs.getString("weight",""),
                    prefs.getString("height","")
            };
            saveToUserClass(results,prefs.getString("username",""),prefs.getString("password",""));
        }
    }
}