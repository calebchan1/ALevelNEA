package com.example.exercisetracker;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

        //all input fields instantiated
        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        forenameField = findViewById(R.id.firstnameField);
        surnameField = findViewById(R.id.lastnameField);
        DOBField = findViewById(R.id.DOBfield);
        weightField = findViewById(R.id.weightField);
        heightField = findViewById(R.id.heightField);

        //buttons
        Button cancelbtn = findViewById(R.id.newUser_cancelBtn);
        cancelbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //returns back to log in screen
                RegisterUserActivity.this.finish();
            }
        });

        //material date picker
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        EditText dobText = DOBField.getEditText();
        dobText.setInputType(InputType.TYPE_NULL);
        dobText.setKeyListener(null);
        CalendarConstraints.Builder constraints = new CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.before(MaterialDatePicker.todayInUtcMilliseconds()));
        MaterialDatePicker.Builder<Long> datepickerBuilder = MaterialDatePicker.Builder.datePicker();
        datepickerBuilder.setCalendarConstraints(constraints.build());
        MaterialDatePicker datepicker = datepickerBuilder.build();

        //when date of birth is touched
        dobText.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP){
                    datepicker.show(getSupportFragmentManager(), "Date Picker");
                }
                return  false;
            }
        });

        datepicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Object selection) {
                //saving the entered date and formatting date
                dobText.setText(df.format(datepicker.getSelection()));
                datepicker.dismiss();
            }
        });
        datepicker.addOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                datepicker.dismiss();
            }
        });

        //create button
        Button createbtn = findViewById(R.id.createBtn);
        createbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //passing details to database
                String username = Objects.requireNonNull(usernameField.getEditText()).getText().toString();
                String passsword = Objects.requireNonNull(passwordField.getEditText()).getText().toString();
                String forename = Objects.requireNonNull(forenameField.getEditText()).getText().toString();
                String surname = Objects.requireNonNull(surnameField.getEditText()).getText().toString();
                String DOB = Objects.requireNonNull(DOBField.getEditText()).getText().toString();
                String weight = weightField.getEditText().getText().toString();
                String height = heightField.getEditText().getText().toString();
                dbhelper helper = new dbhelper(RegisterUserActivity.this);
                if (helper.registerUser(username, passsword, forename, surname, DOB, weight, height)) {
                    //if user was successfully added to database, enter the app
                    finish();
                }
                else{
                    //user was not successfully added to database, error shown to user
                    Toast.makeText(RegisterUserActivity.this, "Could not create account", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
