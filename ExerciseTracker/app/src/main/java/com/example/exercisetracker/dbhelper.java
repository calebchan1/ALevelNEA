package com.example.exercisetracker;

import android.content.Context;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class dbhelper {
    private Context context;
    private int flag;
    private String result = "";
    private static final String url = "jdbc:mysql://sql4.freesqldatabase.com:3306/sql4456768";
    private static final String dbuser = "sql4456768";
    private static final String dbpassword = "gyFr8LHqQA";

    public dbhelper(Context context) {
        this.context = context;
    }

    public boolean registerUser(String username, String password, String forename, String surname, String DOB, String weight, String height){
        Connection conn = null;
        try{
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connecting to database server
            conn = DriverManager.getConnection(url, dbhelper.dbuser, dbhelper.dbpassword);
            Statement statement = conn.createStatement();
            //executing SQL statement
            int resultset = statement.executeUpdate(
                    "INSERT INTO User(username,password,firstname,surname,dateOfBirth,weight,height) " +
                            String.format("VALUES ('%s','%s','%s','%s','2004-12-02','%s','%s')",
                                    username, password,forename,surname,weight,height)
            );
            if (resultset==0){
                Toast.makeText(this.context, "Could not create an account", Toast.LENGTH_SHORT).show();
                return false;
            }
            Toast.makeText(this.context, "Account created", Toast.LENGTH_SHORT).show();

            return true;
        }
        catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException e){
            e.printStackTrace();
            Toast.makeText(this.context, "Could not connect to database", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public Boolean login(String username, String password) {
        //handles login validation process
        Connection conn = null;
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connecting to database server
            conn = DriverManager.getConnection(url, dbhelper.dbuser, dbhelper.dbpassword);
            Statement statement = conn.createStatement();
            //executing SQL statement
            ResultSet resultset = statement.executeQuery(
                    "SELECT UserID, firstname, surname, dateOfBirth, weight, height " +
                            "FROM User " +
                            String.format("WHERE username = '%s' AND password = '%s'", username, password)
            );

            if (!resultset.next()){
                Toast.makeText(this.context, "Username or Password incorrect", Toast.LENGTH_SHORT).show();
                return false;
            }
            resultset.beforeFirst();
            while (resultset.next()) {
                for (int i = 1; i <= 6; i++) {
                    setResult(getResult()+resultset.getString(i) + " ");
                }
            }
            Toast.makeText(this.context, "Login Successful", Toast.LENGTH_SHORT).show();

            return true;

        } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            //if connection throws exception, login failed and false is returned
            Toast.makeText(this.context, "Could not connect to server. Have you switched on the device's internet?", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean saveActivity(String exericse, String currDate, String timestarted, String duration, String calories){
        Connection conn = null;
        try{
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connecting to database server
            conn = DriverManager.getConnection(url, dbhelper.dbuser, dbhelper.dbpassword);
            Statement statement = conn.createStatement();
            //executing SQL statement
            int resultset = statement.executeUpdate(
                    "INSERT INTO Activity(UserID,Date,timeStarted,duration,calories) " +
                            String.format("VALUES ('%s','%s','%s','%s','%s')",
                                    exericse, currDate,timestarted,duration ,calories)
            );
            if (resultset==0){
                Toast.makeText(this.context, "Could not create an account", Toast.LENGTH_SHORT).show();
                return false;
            }
            Toast.makeText(this.context, "Account created", Toast.LENGTH_SHORT).show();

            return true;
        }
        catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException e){
            e.printStackTrace();
            Toast.makeText(this.context, "Could not connect to database", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
