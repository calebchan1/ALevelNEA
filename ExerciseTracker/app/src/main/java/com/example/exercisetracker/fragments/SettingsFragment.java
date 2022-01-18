package com.example.exercisetracker.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.exercisetracker.R;
import com.example.exercisetracker.login.LogInScreen;
import com.example.exercisetracker.other.DBhelper;
import com.example.exercisetracker.other.User;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SettingsFragment extends Fragment implements View.OnClickListener {
    private TextInputLayout usernameField;
    private TextInputLayout passwordField;
    private TextInputLayout weightField;
    private TextInputLayout DOBField;
    private TextInputLayout forenameField;
    private TextInputLayout surnameField;
    private TextInputLayout heightField;
    private SharedPreferences sp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        sp = getActivity().getSharedPreferences("userprefs", Context.MODE_PRIVATE);
        usernameField = view.findViewById(R.id.settings_usernameField);
        passwordField = view.findViewById(R.id.settings_passwordField);
        weightField = view.findViewById(R.id.weightField);
        forenameField = view.findViewById(R.id.forenameField);
        surnameField = view.findViewById(R.id.surnameField);
        DOBField = view.findViewById(R.id.DOBfield);
        heightField = view.findViewById(R.id.heightField);


        //loading user data presets
        usernameField.getEditText().setText(User.getUsername());
        passwordField.getEditText().setText(User.getPassword());
        weightField.getEditText().setText(User.getWeight().toString());
        heightField.getEditText().setText(User.getHeight().toString());
        forenameField.getEditText().setText(User.getForename());
        surnameField.getEditText().setText(User.getSurname());

        //handling date of birth
        Date dob = User.getDateOfBirth();
        EditText dobText = DOBField.getEditText();

        //date of birth, date picker
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String strdob = df.format(dob);
        dobText.setText(strdob);
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
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    datepicker.show(getActivity().getSupportFragmentManager(), "Date Picker");
                }
                return false;
            }
        });

        datepicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Object selection) {
                //saving the entered date and formatting date
                dobText.setText(df.format(datepicker.getSelection()));
                java.sql.Date date = new java.sql.Date((Long) datepicker.getSelection());
                User.setDateOfBirth(date);
                datepicker.dismiss();
            }
        });
        datepicker.addOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                datepicker.dismiss();
            }
        });

        //handling update and logout buttons
        Button updateButton = view.findViewById(R.id.UpdateButton);
        updateButton.setOnClickListener(this);
        Button logoutBtn = view.findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(this);
        Button deleteBtn = view.findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(this);

        return view;
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.UpdateButton:
                //saving user details to User class and updating database
                //show dialogue to user to confirm if they want to delete account
                MaterialAlertDialogBuilder builder3  = createDialogBuilder("Update My Details?", "Are you sure you want to update your account?");
                builder3.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            String username = String.valueOf(usernameField.getEditText().getText());
                            User.setUsername(username);
                            String password = String.valueOf(passwordField.getEditText().getText());
                            User.setPassword(password);
                            Float weight = Float.parseFloat(String.valueOf(weightField.getEditText().getText()));
                            User.setWeight(weight);
                            Integer height = Integer.valueOf(String.valueOf(heightField.getEditText().getText()));
                            User.setHeight(height);
                            String forename = String.valueOf(forenameField.getEditText().getText());
                            User.setForename(forename);
                            String surname = String.valueOf(surnameField.getEditText().getText());
                            User.setSurname(surname);

                            DBhelper helper = new DBhelper(getContext());
                            if (helper.updateUser()) {
                                //if update on database was successful
                                Toast.makeText(getContext(), "Save successful", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Save unsuccessful", Toast.LENGTH_SHORT).show();
                        }
                        dialog.cancel();
                    }
                });
                AlertDialog dialog3 = builder3.create();
                dialog3.show();

                break;
            case R.id.logoutBtn:
                //When  the user wants to logout, clearing User details
                //Clearing shared preferences
                //show dialogue to user to confirm if they want to delete account
                MaterialAlertDialogBuilder builder = createDialogBuilder("Logout?", "Are you sure you want to logout?");
                builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        User.logout(getContext());
                        Intent intent1 = new Intent(getContext(), LogInScreen.class);
                        startActivity(intent1);
                        dialog.cancel();
                        getActivity().finish();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case R.id.deleteBtn:
                //handling deleting an account
                //show dialogue to user to confirm if they want to delete account
                MaterialAlertDialogBuilder builder2  = createDialogBuilder("Delete My Account?", "Are you sure you want to delete your account?");
                builder2.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DBhelper helper = new DBhelper(getContext());
                        if (helper.deleteAccount(User.getUserID())){
                            Toast.makeText(getContext(), "Account Deleted", Toast.LENGTH_SHORT).show();
                            User.logout(getContext());
                            Intent intent1 = new Intent(getContext(), LogInScreen.class);
                            startActivity(intent1);
                            getActivity().finish();
                        }
                        else{
                            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                        }
                        dialog.cancel();
                    }
                });
                AlertDialog dialog2 = builder2.create();
                dialog2.show();
                break;
        }
    }

    private MaterialAlertDialogBuilder createDialogBuilder(String title, String message){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        })
        ;
        builder.setMessage(message)
                .setTitle(title);
        return builder;
    }

}
