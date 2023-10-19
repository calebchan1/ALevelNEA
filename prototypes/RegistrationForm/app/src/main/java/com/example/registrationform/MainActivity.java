package com.example.registrationform;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {
    private Button btnImage,btnRegister;
    private ConstraintLayout parent;
    private EditText name,email,password,repassword;
    private CheckBox agreed;
    private Spinner spinner;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnImage = findViewById(R.id.btnImage);
        btnRegister = findViewById(R.id.btnRegister);
        parent = findViewById(R.id.parent);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password=findViewById(R.id.password);
        repassword=findViewById(R.id.repassword);
        agreed = findViewById(R.id.agreed);
        spinner = findViewById(R.id.spinner);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            Snackbar snackbar = Snackbar.make(parent,"",Snackbar.LENGTH_SHORT)
                    .setAction("Dismiss", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    });
            @Override
            public void onClick(View v) {
                if (name.getText().toString().matches("") || email.getText().toString().matches("") || password.getText().toString().matches("") || repassword.getText().toString().matches("")) {
                    snackbar.setText("You have not entered all the required fields")
                            .show();
                }
                else if(!password.getText().toString().matches(repassword.getText().toString())){
                    snackbar.setText("Your passwords do not match").show();
                }
                else if(!agreed.isChecked()){
                    snackbar.setText("You have not agreed the license").show();
                }
                else{
                    snackbar.setText(
                            "Name: " + name.getText() +
                            "\nEmail: " + email.getText() +
                            "Country: " + spinner.getSelectedItem().toString()

                    ).show();
                }
            }
        });
    }
}