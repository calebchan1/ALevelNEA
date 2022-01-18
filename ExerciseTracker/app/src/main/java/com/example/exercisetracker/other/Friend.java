package com.example.exercisetracker.other;

public class Friend {
    private int id;
    private String firstname;
    private String surname;
    private String username;

    public Friend(int id, String firstname, String surname, String username) {
        this.id = id;
        this.firstname = firstname;
        this.surname = surname;
        this.username = username;
    }


    public int getId() {
        return id;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getSurname() {
        return surname;
    }

    public String getUsername() {
        return username;
    }
}
