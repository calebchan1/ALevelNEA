package com.example.exercisetracker.other;

import android.content.Context;
import android.os.StrictMode;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DBhelper {
    private static final String url = "jdbc:mysql://sql4.freesqldatabase.com:3306/sql4456768";
    private static final String dbuser = "sql4456768";
    private static final String dbpassword = "gyFr8LHqQA";
    private Context context;
    private int flag;
    private ArrayList<String> result = new ArrayList<String>();

    public DBhelper(Context context) {
        this.context = context;
    }

    public boolean registerUser(String username, String password, String forename, String surname, String DOB, String weight, String height) {
        Connection conn = null;
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connecting to database server
            conn = DriverManager.getConnection(url, DBhelper.dbuser, DBhelper.dbpassword);
            Statement statement = conn.createStatement();
            //executing SQL statement
            int resultset = statement.executeUpdate(
                    "INSERT INTO User(username,password,firstname,surname,dateOfBirth,weight,height) " +
                            String.format("VALUES ('%s','%s','%s','%s','2004-12-02','%s','%s')",
                                    username, password, forename, surname, weight, height)
            );
            if (resultset == 0) {
                Toast.makeText(this.context, "Could not create an account", Toast.LENGTH_SHORT).show();
                return false;
            }
            Toast.makeText(this.context, "Account created", Toast.LENGTH_SHORT).show();

            return true;
        } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this.context, "Could not connect to database", Toast.LENGTH_SHORT).show();
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

    public Boolean login(String username, String password) {
        //handles login validation process
        Connection conn = null;
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connecting to database server
            conn = DriverManager.getConnection(url, DBhelper.dbuser, DBhelper.dbpassword);
            Statement statement = conn.createStatement();
            //executing SQL statement
            ResultSet resultset = statement.executeQuery(
                    "SELECT UserID, firstname, surname, dateOfBirth, weight, height " +
                            "FROM User " +
                            String.format("WHERE username = '%s' AND password = '%s'", username, password)
            );

            if (!resultset.next()) {
                Toast.makeText(this.context, "Username or Password incorrect", Toast.LENGTH_SHORT).show();
                return false;
            }
            resultset.beforeFirst();
            while (resultset.next()) {
                String row = "";
                for (int i = 1; i <= 6; i++) {
                    row = row + resultset.getString(i) + " ";
                }
                addResult(row);
            }
            Toast.makeText(this.context, "Login Successful", Toast.LENGTH_SHORT).show();

            return true;

        } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            //if connection throws exception, login failed and false is returned
            Toast.makeText(this.context, "Could not connect to database", Toast.LENGTH_SHORT).show();
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

    public boolean updateUser() {
        Connection conn = null;
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connecting to database server
            conn = DriverManager.getConnection(DBhelper.url, DBhelper.dbuser, DBhelper.dbpassword);
            Statement statement = conn.createStatement();
            //executing SQL statement
            Date dob = User.getDateOfBirth();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String strdob = df.format(dob);
            int resultset = statement.executeUpdate(
                    "UPDATE User " +
                            String.format("SET username = '%s',password = '%s',firstname = '%s',surname = '%s',dateOfBirth = '%s',weight = '%s',height = '%s' ",
                                    User.getUsername(), User.getPassword(), User.getForename(), User.getSurname(),
                                    strdob, User.getWeight().toString(), User.getHeight().toString()
                            ) +
                            String.format("WHERE User.UserID = '%s'", User.getUserID().toString())
            );
            if (resultset == 0) {
                //could not save activity
                return false;
            }
            //activity was saved successfully
            return true;
        } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this.context, "Could not connect to database", Toast.LENGTH_SHORT).show();
            return false;
        } finally {
            try {
                if (conn != null) {
                    //closing the connection
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public boolean saveActivity(String exercise, String currDate, String timestarted, String duration, String calories, String steps, String distance, String reps) {
        Connection conn = null;
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connecting to database server
            conn = DriverManager.getConnection(DBhelper.url, DBhelper.dbuser, DBhelper.dbpassword);
            Statement statement = conn.createStatement();
            int resultset = 0;
            //executing SQL statement
            if (reps==null) {
                //activity is either walking, running or treadmill
                resultset = statement.executeUpdate(
                        "INSERT INTO Activity (ExerciseID, UserID,Date,timeStarted,duration,calories,steps,distance) " +
                                String.format("VALUES (%s,%s,'%s','%s','%s','%s','%s','%s');",
                                        ("(SELECT Exercise.ExerciseID FROM Exercise WHERE Exercise.Name = '" + exercise + "')"),
                                        ("(SELECT User.UserID FROM User WHERE User.username = '" + User.getUsername() + "')"),
                                        currDate, timestarted, duration, calories, steps, distance)
                );

            }
            else{
                //activity is either walking, running or treadmill
                resultset = statement.executeUpdate(
                        "INSERT INTO Activity (ExerciseID, UserID,Date,timeStarted,duration,calories,reps) " +
                                String.format("VALUES (%s,%s,'%s','%s','%s','%s','%s');",
                                        ("(SELECT Exercise.ExerciseID FROM Exercise WHERE Exercise.Name = '" + exercise + "')"),
                                        ("(SELECT User.UserID FROM User WHERE User.username = '" + User.getUsername() + "')"),
                                        currDate, timestarted, duration, calories, reps)
                );
            }
            if (resultset == 0) {
                //could not save activity
                return false;
            }

            //activity was saved successfully
            return true;
        } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this.context, "Could not connect to database", Toast.LENGTH_SHORT).show();
            return false;
        } finally {
            try {
                if (conn != null) {
                    //closing the connection
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean readActivities() {
        Connection conn = null;
        //get all activities associated with the user's ID
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connecting to database server
            conn = DriverManager.getConnection(url, DBhelper.dbuser, DBhelper.dbpassword);
            Statement statement = conn.createStatement();
            //executing SQL statement
            ResultSet resultset = statement.executeQuery(
                    "SELECT Activity.ActivityID, Exercise.Name, Activity.Date, Activity.timeStarted, Activity.duration, Activity.calories, Activity.steps, Activity.distance " +
                            "FROM Exercise, Activity " +
                            String.format("WHERE Activity.UserID = (SELECT User.UserID FROM User WHERE User.username = '%s') ", User.getUsername()) +
                            "AND Exercise.ExerciseID = Activity.ExerciseID " +
                            "ORDER BY Activity.Date DESC;"
            );

            if (!resultset.next()) {
                Toast.makeText(this.context, "No Activities Stored", Toast.LENGTH_SHORT).show();
                return false;
            }
            resultset.beforeFirst();
            //dealing with multiple rows
            while (resultset.next()) {
                String row = "";
                for (int i = 1; i <= 8; i++) {
                    //adding result to dbhelper
                    row = row + resultset.getString(i) + " ";
                }
                //moving to next row (if there is any)
                addResult(row);
            }
            //activities read in form:
            //"exercise name", "date", "time", "duration", "calories","steps","distance"
            return true;

        } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            //if connection throws exception, login failed and false is returned
            Toast.makeText(this.context, "Could not connect to database", Toast.LENGTH_SHORT).show();
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

    public boolean deleteActivity(int ActivityID) {
        Connection conn = null;
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connecting to database server
            conn = DriverManager.getConnection(DBhelper.url, DBhelper.dbuser, DBhelper.dbpassword);
            Statement statement = conn.createStatement();
            //executing SQL statement
            int resultset = statement.executeUpdate(
                    String.format("DELETE FROM Activity WHERE ActivityID = '%d'", ActivityID)
            );
            if (resultset == 0) {
                //could not delete activity
                return false;
            }
            //activity was deleted successfully
            return true;
        } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this.context, "Could not connect to database", Toast.LENGTH_SHORT).show();
            return false;
        } finally {
            try {
                if (conn != null) {
                    //closing the connection
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean getAllActivities(){
        Connection conn = null;
        //get request to database for all activities done for public leaderboard
        //includes userID, and first name corresponding to each activity
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //connecting to database server
            conn = DriverManager.getConnection(url, DBhelper.dbuser, DBhelper.dbpassword);
            Statement statement = conn.createStatement();
            //executing SQL statement
            ResultSet resultset = statement.executeQuery(
                    "SELECT User.firstname, Activity.calories " +
                            "FROM Activity, User " +
                            "WHERE Activity.UserID = User.UserID " +
                            "ORDER BY Activity.Date DESC;"
            );

            if (!resultset.next()) {
                Toast.makeText(this.context, "No Activities Stored", Toast.LENGTH_SHORT).show();
                return false;
            }
            resultset.beforeFirst();
            //dealing with multiple rows
            while (resultset.next()) {
                String row = "";
                for (int i = 1; i <= 2; i++) {
                    //adding result to dbhelper
                    row = row + resultset.getString(i) + " ";
                }
                //moving to next row (if there is any)
                addResult(row);
            }
            return true;

        } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            //if connection throws exception, login failed and false is returned
            Toast.makeText(this.context, "Could not connect to database", Toast.LENGTH_SHORT).show();
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


    public ArrayList<String> getResult() {
        return result;
    }

    public void addResult(String result) {
        this.result.add(result);
    }
}
