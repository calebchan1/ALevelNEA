package com.example.exercisetracker;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

public class User {
    private static String name;
    private static String email;
    private static Float weight;
    private static Date dateOfBirth;
    private static Integer height;

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        User.name = name;
    }

    public static String getEmail() {
        return email;
    }

    public static void setEmail(String email) {
        User.email = email;
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

    public static Date getDateOfBirth() {
        return dateOfBirth;
    }

    public static void setDateOfBirth(Date dateOfBirth) {
        User.dateOfBirth = dateOfBirth;
    }

    public static void saveUserDetails(){
    }

    public static void readUserDetails(){
    }
}
