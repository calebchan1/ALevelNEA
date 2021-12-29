package com.example.exercisetracker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class RegisterUserActivity extends AppCompatActivity {

    private TextInputLayout usernameField;
    private TextInputLayout passwordField;
    private TextInputLayout forenameField;
    private TextInputLayout surnameField;
    private TextInputLayout DOBField;
    private TextInputLayout weightField;
    private TextInputLayout heightField;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //visuals
        getSupportActionBar().hide();
        getWindow().setNavigationBarColor(getResources().getColor(R.color.main_colour));
        getWindow().setStatusBarColor(getResources().getColor(R.color.main_colour));

        setContentView(R.layout.activity_registeruser);

        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        forenameField = findViewById(R.id.firstnameField);
        surnameField = findViewById(R.id.lastnameField);
        DOBField = findViewById(R.id.DOBfield);
        weightField = findViewById(R.id.weightField);
        heightField = findViewById(R.id.heightField);

        Button cancelbtn = findViewById(R.id.newUser_cancelBtn);
        cancelbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterUserActivity.this.finish();
            }
        });

        Button createbtn = findViewById(R.id.createBtn);
        createbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = Objects.requireNonNull(usernameField.getEditText()).getText().toString();
                String passsword = Objects.requireNonNull(passwordField.getEditText()).getText().toString();
                String forename = Objects.requireNonNull(forenameField.getEditText()).getText().toString();
                String surname = Objects.requireNonNull(surnameField.getEditText()).getText().toString();
                String DOB = Objects.requireNonNull(DOBField.getEditText()).getText().toString();
                String weight = weightField.getEditText().getText().toString();
                String height = heightField.getEditText().getText().toString();
                dbhelper helper = new dbhelper(RegisterUserActivity.this);
                if (helper.registerUser(username, passsword, forename, surname, DOB, weight, height)) {
                    finish();
                }
            }
        });
    }
}
