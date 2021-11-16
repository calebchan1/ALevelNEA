package com.example.exercisetracker;

import java.util.Date;

public class User {
    private static Float weight;
    private static Date dateOfBirth;
    private static Integer height;

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
}
