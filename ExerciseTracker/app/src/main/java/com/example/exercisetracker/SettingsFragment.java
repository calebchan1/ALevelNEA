package com.example.exercisetracker;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
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
import androidx.fragment.app.Fragment;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SettingsFragment extends Fragment implements View.OnClickListener{
    private TextInputLayout weightField;
    private TextInputLayout DOBField;
    private MaterialDatePicker datepicker = MaterialDatePicker.Builder.datePicker().build();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings,container,false);
        weightField = view.findViewById(R.id.weightField);
        DOBField = view.findViewById(R.id.DOBfield);

        //loading user data presets
        weightField.getEditText().setText(User.getWeight().toString());

        //handling date of birth
        Date dob = User.getDateOfBirth();
        EditText dobText = DOBField.getEditText();
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String strdob = df.format(dob);
        dobText.setText(strdob);
        dobText.setInputType(InputType.TYPE_NULL);
        dobText.setKeyListener(null);
        dobText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP){
                    datepicker.show(getActivity().getSupportFragmentManager(), "Date Picker");
                }
                return  false;
            }
        });

        datepicker.addOnPositiveButtonClickListener(selection -> dobText.setText(String.valueOf(datepicker.getSelection())));
        datepicker.addOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                datepicker.dismiss();
            }
        });



        //handling update button
        Button updateButton = view.findViewById(R.id.UpdateButton);
        updateButton.setOnClickListener(this);


        return view;
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.UpdateButton:
                Float weight =Float.parseFloat(String.valueOf(weightField.getEditText().getText()));
                weightField.getEditText().setCursorVisible(Boolean.FALSE);
                User.setWeight(weight);
//                Date dob = (Date) DOBField.getEditText().getText();
//                User.setDateOfBirth(dob);
        }
    }


}
