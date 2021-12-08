package com.example.exercisetracker;

import android.content.Context;
import android.os.AsyncTask;

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
import java.sql.Statement;

public class handlelogin extends AsyncTask {
    private Context context;
    private int flag;
    private String result;


    public handlelogin(Context context, int flag){
        this.context = context;
        this.flag = flag;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        if (flag == 0){
            // 0 means requesting login details from server
            try{
                String records = "";
                Connection connection  = DriverManager.getConnection("jdbc:mysql://sql4.freesqldatabase.com:3306/sql4456768","sql4456768","gyFr8LHqQA");
                Statement statement = connection.createStatement();
                ResultSet resultset = statement.executeQuery("SELECT * FROM User");
                while (resultset.next()){
                    records += resultset.getString(1)+ " " + resultset.getString(2) + "\n";
                }
                this.result = records;
            }
            catch(Exception e){
                this.result = new String("Exception");
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {

        super.onPostExecute(o);
    }

    public String getResult() {
        return result;
    }
}
