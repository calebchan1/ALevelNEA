package com.example.exercisetracker;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SettingsFragment extends Fragment implements View.OnClickListener{
    private TextInputLayout weightField;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings,container,false);
        weightField = view.findViewById(R.id.weightField);
        weightField.getEditText().setText(User.getWeight().toString());
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
        }
    }
}
