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

        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        createbtn = findViewById(R.id.createaccount);
        loginbtn = findViewById(R.id.loginbtn);
        remember = findViewById(R.id.rememberBox);


        //getting saved user details from shared preferences
//        SharedPreferences prefs = getSharedPreferences("checkbox",MODE_PRIVATE);
//        String checkbox = prefs.getString("remember","");
//        if (checkbox.equals("true")){
//            prefs = getSharedPreferences("userdetails",MODE_PRIVATE);
//            User.setUserID(Integer.valueOf(prefs.getString("id","")));
//            User.setName(prefs.getString("name",""));
//            User.setWeight(Float.valueOf(prefs.getString("weight","")));
//            User.setHeight(Integer.valueOf(prefs.getString("height","")));
//            java.sql.Date date = java.sql.Date.valueOf(prefs.getString("DOB",""));
//            User.setDateOfBirth(date);
//        }
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameField.getEditText().getText().toString();
                String password = passwordField.getEditText().getText().toString();
                //user validation here
                dbhelper helper = new dbhelper(LogInScreen.this);
                if (helper.login(username, password)) {
                    String[] results = helper.getResult().get(0).split(" ");
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
                    //saving to SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("userdetails", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("id", results[0]);
                    editor.putString("name", (results[1] + " " + results[2]));
                    editor.putString("DOB", results[3]);
                    editor.putString("weight", results[4]);
                    editor.putString("height", results[5]);
                    editor.apply();
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
}