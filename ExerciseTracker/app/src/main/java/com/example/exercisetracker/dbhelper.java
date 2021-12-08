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
    private String result;
    private static final String url = "jdbc:mysql://sql4.freesqldatabase.com:3306/sql4456768";
    private static final String dbuser = "sql4456768";
    private static final String dbpassword = "gyFr8LHqQA";

    public dbhelper(Context context){
        this.context = context;
        this.result = "";
    }

    public Boolean login(String username, String password){
        //handles login validation process
        Connection conn = null;
        try{
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connecting to database server
            conn  = DriverManager.getConnection(url,dbhelper.dbuser,dbhelper.dbpassword);
            Statement statement = conn.createStatement();
            //executing SQL statement
            ResultSet resultset = statement.executeQuery("SELECT * FROM User");
            Toast.makeText(this.context, "Login Successful", Toast.LENGTH_SHORT).show();
            while (resultset.next()){
                for (int i = 2;i<=8;i++){
                    //saving SQL query results
                    this.result += resultset.getString(i) + " ";
                }
            }
            Toast.makeText(this.context, this.result, Toast.LENGTH_SHORT).show();
            return true;

        } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            //if connection throws exception, login failed and false is returned
            Toast.makeText(this.context, "Login Unsuccessful", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
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
    }

    public String getResult() {
        return result;
    }
}
