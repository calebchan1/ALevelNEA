package com.example.exercisetracker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;

public class RegisterUserActivity extends AppCompatActivity {
    private Button createbtn;
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
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.main_colour));// set status background white
        setContentView(R.layout.activity_registeruser);

        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        forenameField = findViewById(R.id.firstnameField);
        surnameField = findViewById(R.id.lastnameField);
        DOBField = findViewById(R.id.DOBfield);
        weightField = findViewById(R.id.weightField);
        heightField = findViewById(R.id.heightField);

        createbtn = findViewById(R.id.createBtn);
        createbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameField.getEditText().getText().toString();
                String passsword = passwordField.getEditText().getText().toString();
                String forename = forenameField.getEditText().getText().toString();
                String surname = surnameField.getEditText().getText().toString();
                String DOB = DOBField.getEditText().getText().toString();
                String weight = weightField.getEditText().getText().toString();
                String height = heightField.getEditText().getText().toString();
                dbhelper helper = new dbhelper(RegisterUserActivity.this);
                if (helper.registerUser(username,passsword,forename,surname,DOB,weight,height)){
                    finish();
                }
            }
        });
    }
}
