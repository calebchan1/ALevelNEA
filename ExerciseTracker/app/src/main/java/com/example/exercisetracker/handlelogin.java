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

public class handlelogin extends AsyncTask {
    private Context context;
    private int flag;

    public handlelogin(Context context, int flag){
        this.context = context;
        this.flag = flag;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        if (flag == 0){
            // 0 means requesting login details from server
            try{
                String username = (String) objects[0];
                String password = (String) objects[1];
                String link = "http://myphpmysqlweb.hostei.com/login.php?username="+username+"& password="+password;

                URL url = new URL(link);
                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet();
                request.setURI(new URI(link));
                HttpResponse response = client.execute(request);
                BufferedReader in = new BufferedReader(new
                        InputStreamReader(response.getEntity().getContent()));

                StringBuffer sb = new StringBuffer("");
                String line="";

                while ((line = in.readLine()) != null) {
                    sb.append(line);
                    break;
                }

                in.close();
                return sb.toString();
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }
        }



        return null;
    }



}
