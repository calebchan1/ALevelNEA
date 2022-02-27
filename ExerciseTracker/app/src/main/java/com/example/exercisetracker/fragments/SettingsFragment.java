package com.example.exercisetracker.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class SettingsFragment extends Fragment implements View.OnClickListener {
    private TextInputLayout usernameField;
    private TextInputLayout passwordField;
    private TextInputLayout weightField;
    private TextInputLayout DOBField;
    private TextInputLayout forenameField;
    private TextInputLayout surnameField;
    private TextInputLayout heightField;
    private SharedPreferences sp;
    private ProgressBar progressBar;
    private android.app.Activity mcontext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //saving the attached activity to preserve lifecycle of fragment
        //ensures that UI thread runs on an instance of an activity
        if (context instanceof android.app.Activity) {
            mcontext = (android.app.Activity) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        sp = mcontext.getSharedPreferences("userprefs", Context.MODE_PRIVATE);
        usernameField = view.findViewById(R.id.settings_usernameField);
        passwordField = view.findViewById(R.id.settings_passwordField);
        weightField = view.findViewById(R.id.weightField);
        forenameField = view.findViewById(R.id.forenameField);
        surnameField = view.findViewById(R.id.surnameField);
        DOBField = view.findViewById(R.id.DOBfield);
        heightField = view.findViewById(R.id.heightField);
        progressBar = view.findViewById(R.id.progressBar);
        //handling update and logout buttons
        Button updateButton = view.findViewById(R.id.UpdateButton);
        updateButton.setOnClickListener(this);
        Button logoutBtn = view.findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(this);
        Button deleteBtn = view.findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(this);
        new GetSettings().execute(true);
        return view;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.UpdateButton:
                //saving user details to User class and updating database
                //show dialogue to user to confirm if they want to delete account
                MaterialAlertDialogBuilder builder3 = createDialogBuilder("Update My Details?", "Are you sure you want to update your account?");
                builder3.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            // use of .replaceAll to sanatise inputs given by user, to remove any whitespaces
                            String username = String.valueOf(Objects.requireNonNull(usernameField.getEditText()).getText());
                            String password = String.valueOf(Objects.requireNonNull(passwordField.getEditText()).getText());
                            Float weight = Float.parseFloat(String.valueOf(Objects.requireNonNull(weightField.getEditText()).getText()));
                            Integer height = Integer.valueOf(String.valueOf(Objects.requireNonNull(heightField.getEditText()).getText()));
                            String forename = String.valueOf(Objects.requireNonNull(forenameField.getEditText()).getText());
                            String surname = String.valueOf(Objects.requireNonNull(surnameField.getEditText()).getText());
                            String DOB = Objects.requireNonNull(DOBField.getEditText()).getText().toString();

                            //username and password must be at least 8 characters
                            //max length of username is 16 characters
                            boolean requirements = username.length() >= 8 && password.length() >= 8 && username.length() <= 16;
                            //if one field is empty, cannot update account
                            boolean isEmpty = username.isEmpty() || password.isEmpty() || forename.isEmpty() || surname.isEmpty() || DOB.isEmpty();
                            boolean containsSpace = username.contains(" ") || password.contains(" ") || forename.contains(" ") || surname.contains(" ");
                            if (requirements) {
                                if (!isEmpty) {
                                    if (!containsSpace) {
                                        DBhelper helper = new DBhelper(mcontext);
                                        if (helper.updateUser(username, password, forename, surname, DOB, weight.toString(), height.toString())) {
                                            //if update on database was successful
                                            Toast.makeText(mcontext, "Save successful", Toast.LENGTH_SHORT).show();

                                            //saving to SharedPreferences if user had checked remember me
                                            SharedPreferences prefs = mcontext.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
                                            Boolean checkbox = prefs.getBoolean("remember", false);
                                            if (checkbox) {
                                                // results received in format ID, forename, surname, DOB, weight, height
                                                SharedPreferences.Editor editor = prefs.edit();
                                                editor.putString("username", username);
                                                editor.putString("password", password);
                                                editor.putString("forename", forename);
                                                editor.putString("surname", surname);
                                                editor.putString("DOB", DOBField.getEditText().getText().toString());
                                                editor.putString("weight", weight.toString());
                                                editor.putString("height", height.toString());
                                                editor.apply();
                                                //saving to user class
                                                User.saveUser(username, password, forename, surname, DOB, weight, height);
                                            }
                                        }
                                    } else {
                                        Toast.makeText(mcontext, "No fields can contain spaces", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else{
                                    Toast.makeText(mcontext, "No fields can be left empty", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(mcontext, "You have not met the requirements", Toast.LENGTH_SHORT).show();
                                Toast.makeText(mcontext, "Username and password must be between 8-16 characters", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(mcontext, "Save unsuccessful", Toast.LENGTH_SHORT).show();
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
                        User.logout(mcontext);
                        Intent intent1 = new Intent(mcontext, LogInScreen.class);
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
                MaterialAlertDialogBuilder builder2 = createDialogBuilder("Delete My Account?", "Are you sure you want to delete your account?");
                builder2.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DBhelper helper = new DBhelper(mcontext);
                        if (helper.deleteAccount(User.getUserID())) {
                            Toast.makeText(mcontext, "Account Deleted", Toast.LENGTH_SHORT).show();
                            User.logout(mcontext);
                            Intent intent1 = new Intent(mcontext, LogInScreen.class);
                            startActivity(intent1);
                            getActivity().finish();
                        } else {
                            Toast.makeText(mcontext, "Error", Toast.LENGTH_SHORT).show();
                        }
                        dialog.cancel();
                    }
                });
                AlertDialog dialog2 = builder2.create();
                dialog2.show();
                break;
        }
    }

    private MaterialAlertDialogBuilder createDialogBuilder(String title, String message) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(mcontext);
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

    //using async task to load user details
    private class GetSettings extends AsyncTask<Boolean, Integer, ArrayList<String>> {
        protected ArrayList<String> doInBackground(Boolean... isPublic) {
            ArrayList<String> arr = new ArrayList<>();
            //loading user data presets
            arr.add(User.getUsername());
            arr.add(User.getPassword());
            arr.add(User.getWeight().toString());
            arr.add(User.getHeight().toString());
            arr.add(User.getForename());
            arr.add(User.getSurname());
            Date dob = User.getDateOfBirth();
            //date of birth, date picker
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String strdob = df.format(dob);
            arr.add(strdob);
            return arr;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(ArrayList<String> queryResults) {

            mcontext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //loading user data presets
                    Objects.requireNonNull(usernameField.getEditText()).setText(queryResults.get(0));
                    Objects.requireNonNull(passwordField.getEditText()).setText(queryResults.get(1));
                    Objects.requireNonNull(weightField.getEditText()).setText(queryResults.get(2));
                    Objects.requireNonNull(heightField.getEditText()).setText(queryResults.get(3));
                    Objects.requireNonNull(forenameField.getEditText()).setText(queryResults.get(4));
                    Objects.requireNonNull(surnameField.getEditText()).setText(queryResults.get(5));
                    String strdob = queryResults.get(6);
                    //hiding progress bar
                    progressBar.setVisibility(View.GONE);

                    //handling date of birth
                    EditText dobText = DOBField.getEditText();
                    dobText.setText(strdob);
                    dobText.setInputType(InputType.TYPE_NULL);
                    dobText.setKeyListener(null);

                    //date of birth picker constraints, must be at least 10 yrs old to register account
                    CalendarConstraints.Builder constraints = new CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.before(MaterialDatePicker.todayInUtcMilliseconds() - 315569260000L));
                    MaterialDatePicker.Builder<Long> datepickerBuilder = MaterialDatePicker.Builder.datePicker();
                    //by default starts picker on min age
                    datepickerBuilder.setCalendarConstraints(constraints.build()).setSelection(MaterialDatePicker.todayInUtcMilliseconds() - 315569260000L);
                    MaterialDatePicker datepicker = datepickerBuilder.build();

                    //when date of birth is touched
                    dobText.setOnTouchListener(new View.OnTouchListener() {
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
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
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

                }
            });
        }
    }

}
