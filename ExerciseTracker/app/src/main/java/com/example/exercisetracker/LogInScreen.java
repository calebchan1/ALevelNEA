package com.example.exercisetracker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;

import com.mysql.jdbc.Driver;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LogInScreen extends AppCompatActivity {
    private TextInputLayout usernameField;
    private TextInputLayout passwordField;
    private Button loginbtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //visuals
        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//  set status text dark
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.main_colour));// set status background white
        setContentView(R.layout.activity_loginscreen);
        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        loginbtn = findViewById(R.id.loginbtn);

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameField.getEditText().getText().toString();
                String password = passwordField.getEditText().getText().toString();
                //user validation here
                Connection conn = null;
                try{
                    String records = "";
                    Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
                    String url = "jdbc:mysql://sql4.freesqldatabase.com:3306/sql4456768";
                    String user = "sql4456768";
                    String pass = "gyFr8LHqQA";
                    conn  = DriverManager.getConnection(url,user,pass);
                    Statement statement = conn.createStatement();
                    ResultSet resultset = statement.executeQuery("SELECT * FROM User");
                    Toast.makeText(LogInScreen.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    while (resultset.next()){
                        records += resultset.getString(1)+ " " + resultset.getString(2) + "\n";
                    }
                    finish();

                } catch (SQLException e) {
                    Toast.makeText(LogInScreen.this, "Login Unsuccessful", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try{
                        if (conn!=null){
                            conn.close();
                        }
                    }
                    catch(SQLException e){
                        e.printStackTrace();
                    }
                }
            }});


    }
}