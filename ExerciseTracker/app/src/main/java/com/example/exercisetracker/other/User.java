package com.example.exercisetracker.other;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.exercisetracker.login.LogInScreen;

public class User {
    private static Integer UserID;
    private static String forename;
    private static String surname;
    private static Float weight;
    private static java.sql.Date dateOfBirth;
    private static Integer height;
    private static String username;
    private static String password;

    public static String getUsername() {
        return username;
    }

    public static String getForename() {
        return forename;
    }

    public static void setForename(String forename) {
        User.forename = forename;
    }

    public static String getSurname() {
        return surname;
    }

    public static void setSurname(String surname) {
        User.surname = surname;
    }

    public static void setUsername(String username) {
        User.username = username;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        User.password = password;
    }

    public static Integer getUserID() {
        return UserID;
    }

    public static void setUserID(Integer userID) {
        UserID = userID;
    }

    public static String getName() {
        return getForename() + " " +getSurname();
    }

    public static Integer getHeight() {
        return height;
    }

    public static void setHeight(Integer height) {
        User.height = height;
    }

    public static Float getWeight() {
        return weight;
    }

    public static void setWeight(Float weight) {
        User.weight = weight;
    }

    public static java.sql.Date getDateOfBirth() {
        return dateOfBirth;
    }

    public static void setDateOfBirth(java.sql.Date dateOfBirth) {
        User.dateOfBirth = dateOfBirth;
    }

    public static void logout(Context context){
        //clearing User class data
        setUsername(null);
        setUserID(null);
        setHeight(null);
        setWeight(null);
        setDateOfBirth(null);
        setPassword(null);
        setForename(null);
        setSurname(null);
        //clearing shared preferences
        SharedPreferences prefs = context.getSharedPreferences(LogInScreen.getShared_prefs(),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

}
