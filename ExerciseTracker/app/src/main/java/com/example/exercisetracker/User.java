package com.example.exercisetracker;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

public class User {
    private static Integer UserID;
    private static String name;
    private static Float weight;
    private static java.sql.Date dateOfBirth;
    private static Integer height;
    private static String username;
    private static String password;

    public static String getUsername() {
        return username;
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
        return name;
    }

    public static void setName(String name) {
        User.name = name;
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

    public static void saveUserDetails(){
    }

    public static void readUserDetails(){
    }
}
