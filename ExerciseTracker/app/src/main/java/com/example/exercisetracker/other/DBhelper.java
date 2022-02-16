package com.example.exercisetracker.other;

import android.content.Context;
import android.os.StrictMode;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

public class DBhelper {
    //freesqldatabase.com
//    private static final String url = "jdbc:mysql://sql4.freesqldatabase.com:3306/sql4456768";
//    private static final String dbuser = "sql4456768";
//    private static final String dbpassword = "gyFr8LHqQA";
    //azure database
    private static final String dbuser = "calebchanwy";
    private static final String dbpassword = "gyFr8LHqQA";
    private static final String url = String.format(Locale.getDefault(),
            "jdbc:jtds:sqlserver://trackerplus2.database.windows.net:1433;databasename=trackerplus;user=%s@trackerplus2;password=%s;"
            ,dbuser,dbpassword);
    private Context context;
    private int flag;
    private ArrayList<String> result = new ArrayList<String>();

    public DBhelper(Context context) {
        this.context = context;
    }

    /**
     * FOLLOWING METHODS DEAL WITH THE ACTUAL USER OF THE APP
     */
    public boolean registerUser(String username, String password, String forename, String surname, String DOB, String weight, String height) {
        Connection conn = null;
        if (checkSqlInjection(username,password,forename,surname,DOB,weight,height)) {
            try {
                conn = createNewConnection();
                Statement statement = conn.createStatement();
                //creating a new salt and generating hash
                String salt = createSalt();
                String hashedPw = getSecurePassword(password,salt);
                //executing SQL statement
                int resultset = statement.executeUpdate(
                        "INSERT INTO Users(username,password,firstname,surname,dateOfBirth,weight,height,salt) " +
                                String.format("VALUES ('%s','%s','%s','%s','%s','%s','%s','%s')",
                                        username, hashedPw, forename, surname, DOB, weight, height,salt)
                );
                if (resultset == 0) {
                    Toast.makeText(this.context, "Could not create an account", Toast.LENGTH_SHORT).show();
                    return false;
                }
                Toast.makeText(this.context, "Account created", Toast.LENGTH_SHORT).show();

                return true;
            } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException | NoSuchAlgorithmException|NoSuchProviderException e) {
                e.printStackTrace();
                Toast.makeText(this.context, "Could not connect to database", Toast.LENGTH_SHORT).show();
                return false;
            } finally {
                closeConnection(conn);
            }
        }
        else{
            return false;
        }
    }

    public Boolean login(String username, String password) {
        //handles login validation process
        Connection conn = null;
        if (checkSqlInjection(username,password)) {
            try {
                conn = createNewConnection();
                Statement statement = conn.createStatement();
                //getting salt from database
                String salt = getSalt(username);
                if (salt !=null) {
                    ResultSet resultset = null;
                    //executing SQL statement
                    //getting hashed password
                    String hashedPw = getSecurePassword(password,salt);
                    resultset = statement.executeQuery(
                            "SELECT UserID, firstname, surname, dateOfBirth, weight, height " +
                                    "FROM Users " +
                                    String.format("WHERE username = '%s' AND password = '%s'", username, hashedPw)
                    );

                    try {
                        addResult(resultset, 6);
                        if (this.getResult().isEmpty()) {
                            Toast.makeText(this.context, "Login Unsuccessful", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        Toast.makeText(this.context, "Login Successful", Toast.LENGTH_SHORT).show();
                        return true;
                    } catch (Exception e) {
                        Toast.makeText(this.context, "Username or Password incorrect", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
                Toast.makeText(this.context,"User does not exist",Toast.LENGTH_SHORT).show();
                return false;


            } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                //if connection throws exception, login failed and false is returned
                Toast.makeText(this.context, "Could not connect to database", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return false;
            } finally {
                closeConnection(conn);
            }
        }
        else{
            return false;
        }
    }

    public boolean updateUser(String username, String password, String forename, String surname, String DOB, String weight, String height) {
        Connection conn = null;
        if (checkSqlInjection(username,password,forename,surname,DOB,weight,height)) {
            try {
                conn = createNewConnection();
                //getting stored salt
                String salt = getSalt(username);
                String hashedPw = getSecurePassword(password,salt);
                Statement statement = conn.createStatement();
                //executing SQL statement
                int resultset = statement.executeUpdate(
                        "UPDATE Users " +
                                String.format("SET username = '%s',password = '%s',firstname = '%s',surname = '%s',dateOfBirth = '%s',weight = '%s',height = '%s' ",
                                        username, hashedPw, forename, surname,
                                        DOB, weight, height
                                ) +
                                String.format("WHERE Users.UserID = '%s'", User.getUserID().toString())
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
                closeConnection(conn);
            }
        }
        else{
            return false;
        }
    }


    public boolean deleteAccount(int userID) {
        Connection conn = null;

        try {
            conn = createNewConnection();
            Statement statement = conn.createStatement();
            //executing SQL statement
            int resultset = statement.executeUpdate(
                    String.format(Locale.getDefault(), "DELETE FROM Users WHERE UserID = '%d'", userID)
            );
            if (resultset == 0) {
                //could not delete account
                return false;
            }
            //account was deleted successfully
            return true;
        } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this.context, "Could not connect to database", Toast.LENGTH_SHORT).show();
            return false;
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * FOLLOWING METHODS DEAL WITH HANDLING ACTIVITIES
     */
    public boolean saveActivity(String exercise, String currDate, String timestarted, String duration, String calories, String steps, String distance, String reps) {
        //AS SAVING ACTIVITY does not receive direct user input
        //not requried to check for SQL injection
        Connection conn = null;
        try {
            conn = createNewConnection();
            Statement statement = conn.createStatement();
            int resultset = 0;
            //executing SQL statement
            if (reps == null) {
                //activity is either walking, running or treadmill
                resultset = statement.executeUpdate(
                        "INSERT INTO Activity (ExerciseID, UserID,Date,timeStarted,duration,calories,steps,distance) " +
                                String.format("VALUES (%s,%s,'%s','%s','%s','%s','%s','%s');",
                                        ("(SELECT Exercise.ExerciseID FROM Exercise WHERE Exercise.Name = '" + exercise + "')"),
                                        ("(SELECT Users.UserID FROM Users WHERE Users.username = '" + User.getUsername() + "')"),
                                        currDate, timestarted, duration, calories, steps, distance)
                );

            } else {
                //activity is either walking, running or treadmill
                resultset = statement.executeUpdate(
                        "INSERT INTO Activity (ExerciseID, UserID,Date,timeStarted,duration,calories,reps) " +
                                String.format("VALUES (%s,%s,'%s','%s','%s','%s','%s');",
                                        ("(SELECT Exercise.ExerciseID FROM Exercise WHERE Exercise.Name = '" + exercise + "')"),
                                        ("(SELECT Users.UserID FROM Users WHERE Users.username = '" + User.getUsername() + "')"),
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
            closeConnection(conn);
        }
    }

    public boolean readActivities() {

        Connection conn = null;
        //get all activities associated with the user's ID
        try {
            //connecting to database
            conn = createNewConnection();
            Statement statement = conn.createStatement();
            //executing SQL statement
            ResultSet resultset = statement.executeQuery(
                    "SELECT Activity.ActivityID, Exercise.Name, Activity.Date, Activity.timeStarted, Activity.duration, Activity.calories, Activity.steps, Activity.distance, Activity.reps " +
                            "FROM Exercise, Activity " +
                            String.format("WHERE Activity.UserID = (SELECT Users.UserID FROM Users WHERE Users.username = '%s') ", User.getUsername()) +
                            "AND Exercise.ExerciseID = Activity.ExerciseID " +
                            "ORDER BY Activity.Date DESC;"
            );

            try {
                //activities read in form:
                //"exercise name", "date", "time", "duration", "calories","steps","distance","reps"
                addResult(resultset, 9);
                return true;
            }
            catch (Exception e){
                e.printStackTrace();
                return false;
            }
        } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            //if connection throws exception, login failed and false is returned
            e.printStackTrace();
            return false;
        } finally {
            closeConnection(conn);
        }
    }

    public boolean deleteActivity(int ActivityID) {
        Connection conn = null;
        try {
            //connecting to database
            conn = createNewConnection();
            Statement statement = conn.createStatement();
            //executing SQL statement
            int resultset = statement.executeUpdate(
                    String.format(Locale.getDefault(), "DELETE FROM Activity WHERE ActivityID = '%d'", ActivityID)
            );
            if (resultset == 0) {
                //could not delete activity
                return false;
            }
            //activity was deleted successfully
            return true;
        } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeConnection(conn);
        }
    }

    public boolean getFriendsActivities(Integer duration, Set<Integer> friendsList) {

        //by using WHERE IN VALUES(), we can limit which activities are requested
        // in format ('id1','id2',....)
        StringBuilder sqlCondition = new StringBuilder("(");
        for (Integer id : friendsList) {
            sqlCondition.append(String.format(Locale.getDefault(), "'%d',", id));
        }
        //including user's own UserID to show where their relative position is on leaderboard
        sqlCondition.append(String.format(Locale.getDefault(), "'%d',", User.getUserID()));
        sqlCondition.deleteCharAt(sqlCondition.length() - 1);
        sqlCondition.append(") ");

        System.out.println(sqlCondition);
        Connection conn = null;
        //get request to database for all activities done for private leaderboard
        //includes userID, and first name corresponding to each activity

        try {
            //connecting to database server
            conn = createNewConnection();
            Statement statement = conn.createStatement();
            ResultSet resultset = null;
            if (duration == 0) {
                //executing SQL statement
                //requesting leaderboard of all time
                resultset = statement.executeQuery(
                        "SELECT Users.username, Activity.calories " +
                                "FROM Activity, Users " +
                                "WHERE Activity.UserID = Users.UserID AND Activity.UserID IN " + sqlCondition +
                                "ORDER BY Activity.Date DESC;"
                );
            } else if (duration == 1) {
                //executing SQL statement
                //requesting leaderboard from past day
                long millis = System.currentTimeMillis();
                Date date = new java.sql.Date(millis);
                resultset = statement.executeQuery(
                        "SELECT Users.username, Activity.calories " +
                                "FROM Activity, Users " +
                                "WHERE Activity.UserID = Users.UserID AND Activity.UserID IN " + sqlCondition + "AND " +
                                String.format("Activity.Date = '%s' ", date.toString()) +
                                "ORDER BY Activity.Date DESC;"
                );
            } else if (duration == 30) {
                //requesting leaderboard from past 30 days
                long millis = System.currentTimeMillis();
                millis = millis - 2592000000L;
                Date date = new java.sql.Date(millis);
                resultset = statement.executeQuery(
                        "SELECT Users.username, Activity.calories " +
                                "FROM Activity, Users " +
                                "WHERE Activity.UserID = Users.UserID AND Activity.UserID IN " + sqlCondition + "AND " +
                                String.format("Activity.Date >= '%s' ", date.toString()) +
                                "ORDER BY Activity.Date DESC;"
                );
            }
            try {
                //dealing with multiple rows
                addResult(resultset, 2);
                return true;
            }
            catch (Exception e){
                return false;
            }

        } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            //if connection throws exception, login failed and false is returned
            Toast.makeText(this.context, "Could not connect to database", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        } finally {
            closeConnection(conn);
        }
    }


    public boolean getAllActivities(Integer duration) {
        Connection conn = null;
        //get request to database for all activities done for public leaderboard
        //includes userID, and first name corresponding to each activity
        try {
            //connecting to database server
            conn = createNewConnection();
            Statement statement = conn.createStatement();
            ResultSet resultset = null;
            if (duration == 0) {
                //executing SQL statement
                //requesting leaderboard of all time
                resultset = statement.executeQuery(
                        "SELECT Users.username, Activity.calories " +
                                "FROM Activity, Users " +
                                "WHERE Activity.UserID = Users.UserID " +
                                "ORDER BY Activity.Date DESC;"
                );
            } else if (duration == 1) {
                //executing SQL statement
                //requesting leaderboard from past day
                long millis = System.currentTimeMillis();
                Date date = new java.sql.Date(millis);
                resultset = statement.executeQuery(
                        "SELECT Users.username, Activity.calories " +
                                "FROM Activity, Users " +
                                "WHERE Activity.UserID = Users.UserID AND " +
                                String.format("Activity.Date = '%s' ", date.toString()) +
                                "ORDER BY Activity.Date DESC;"
                );
            } else if (duration == 30) {
                //requesting leaderboard from past 30 days
                long millis = System.currentTimeMillis();
                millis = millis - 2592000000L;
                Date date = new java.sql.Date(millis);
                resultset = statement.executeQuery(
                        "SELECT Users.username, Activity.calories " +
                                "FROM Activity, Users " +
                                "WHERE Activity.UserID = Users.UserID AND " +
                                String.format("Activity.Date >= '%s' ", date.toString()) +
                                "ORDER BY Activity.Date DESC;"
                );
            }
            try {
                //dealing with multiple rows
                addResult(resultset, 2);
                return true;
            }
            catch (Exception e){
                e.printStackTrace();
                return false;
            }

        } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            //if connection throws exception, login failed and false is returned
            e.printStackTrace();
            return false;
        } finally {
            closeConnection(conn);
        }
    }


    /**
     * FOLLOWING METHODS DEAL WITH FRIENDS SYSTEM IN THE APP
     */
    public boolean getUsers(String name) {
        Connection conn = null;
        try {
            conn = createNewConnection();
            Statement statement = conn.createStatement();
            ResultSet resultset = null;
            //getting all users on the database, based on search
            //removing user which has the same userID as the user currently in the app
            //(you cannot be a friend of yourself)
            resultset = statement.executeQuery(
                    "SELECT UserID, firstname, surname, username " +
                            "FROM Users WHERE (firstname LIKE " +
                            "'%" + name + "%' " +
                            "OR surname LIKE " + "'%" + name + "%') " +
                            String.format(Locale.getDefault(), "AND UserID <> '%d' ", User.getUserID())

            );
            try {
                addResult(resultset, 4);
                return true;
            }
            catch (Exception e){
                e.printStackTrace();
                return false;
            }
        } catch (Exception e) {
            return false;
        } finally {
            closeConnection(conn);
        }
    }

    public boolean getFriends() {
        Connection conn = null;
        try {
            //checking both columns for any friendships between users
            conn = createNewConnection();
            Statement statement = conn.createStatement();
            ResultSet resultset = null;
            resultset = statement.executeQuery(
                    "SELECT Friendship.User2ID, Users.firstname, Users.surname, Users.username " +
                            "FROM Friendship, Users " +
                            String.format(Locale.getDefault(), " WHERE Friendship.USER1ID = '%d' ", User.getUserID()) +
                            "AND Users.UserID = Friendship.USER2ID"
            );
            try {
                addResult(resultset, 4);
                User.getFriendsList().clear();
                return true;
            }
            catch (Exception e){
                e.printStackTrace();
                return false;
            }

        } catch (Exception e) {
            return false;
        } finally {
            closeConnection(conn);
        }
    }

    public boolean addFriend(int user1, int user2) {
        //adding friend relationship to database
        //where user1 is user who initiated friendship
        Connection conn = null;
        try {
            conn = createNewConnection();
            Statement statement = conn.createStatement();
            int resultset = 0;
            resultset = statement.executeUpdate(
                    "INSERT INTO Friendship " +
                            String.format(Locale.getDefault(), "VALUES ('%d','%d');", user1, user2)
            );
            if (resultset == 0) {
                return false;
            }
            //adding to User class set of friends
            User.addFriendsList(user2);
            return true;
        } catch (Exception e) {
            Toast.makeText(this.context, "Could not connect to database", Toast.LENGTH_SHORT).show();
            return false;
        } finally {
            closeConnection(conn);
        }
    }

    public boolean removeFriend(int user1, int user2) {
        //adding friend relationship to database
        //where user1 is user who initiated friendship
        Connection conn = null;
        try {
            conn = createNewConnection();
            Statement statement = conn.createStatement();
            int resultset = 0;
            resultset = statement.executeUpdate(
                    "DELETE FROM Friendship " +
                            String.format(Locale.getDefault(), "WHERE User1ID = '%d' AND User2ID = '%d'", user1, user2)
            );
            if (resultset == 0) {
                return false;
            }
            //removing from User set of friends
            User.removeFriendsList(user2);
            return true;
        } catch (Exception e) {
            Toast.makeText(this.context, "Could not connect to database", Toast.LENGTH_SHORT).show();
            return false;
        } finally {
            closeConnection(conn);
        }
    }


    public ArrayList<String> getResult() {
        return result;
    }

    public void addResult(ResultSet resultset, int columns) throws SQLException {
        //dealing with multiple rows
        while (resultset.next()) {
            String row = "";
            for (int i = 1; i <= columns; i++) {
                //adding result to dbhelper
                row = row + resultset.getString(i) + " ";
            }
            //moving to next row (if there is any)
            this.result.add(row);
        }

    }

    private Connection createNewConnection() throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Connection conn = null;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
        //connecting to database server
        conn = DriverManager.getConnection(url);
        return conn;
    }

    private void closeConnection(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Boolean checkSqlInjection(String...args){
        //check for any sql commands from any parameters given in args
        for (String string : args){
            if (string.contains("SELECT") || string.contains("DROP") || string.contains("DELETE") || string.contains("INSERT") || string.contains("UPDATE") ||
            string.contains("select") || string.contains("drop") || string.contains("delete") || string.contains("insert") || string.contains("update")){
                Toast.makeText(context, "You must not enter any SQL commands!", Toast.LENGTH_SHORT).show();
                //sql command detected, returns false
                return false;
            }
        }
        //no commands return true
        return true;
    }

    public void clearResults() {
        this.result.clear();
    }


    /**
     * Following methods dealing with hashing a secure password
     * https://howtodoinjava.com/java/java-security/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/
     */

    private static String getSecurePassword(String passwordToHash, String salt) {
        String generatedPassword = null;
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            // Add password bytes to digest
            md.update(salt.getBytes());
            // Get the hash's bytes
            byte[] bytes = md.digest(passwordToHash.getBytes());
            // This bytes[] has bytes in decimal format;
            // Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16)
                        .substring(1));
            }
            // Get complete hashed password in hex format
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }


    private static String createSalt() throws NoSuchAlgorithmException, NoSuchProviderException{
        //Creating a salt for a new user
        // SecureRandom generator
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "AndroidOpenSSL");
        // Create array for salt
        byte[] salt = new byte[16];
        // Get a random salt
        sr.nextBytes(salt);
        // return salt
        return salt.toString();
    }

    private String getSalt(String username){
        //retrieving salt from db of existing user
        Connection conn = null;
        try {
            conn = createNewConnection();
            Statement statement = conn.createStatement();
            ResultSet resultset = null;
            resultset = statement.executeQuery(
                    "SELECT salt FROM Users " +
                            String.format("WHERE username = '%s'", username)
            );
            resultset.next();
            String salt = resultset.getString(1);
            if (salt.isEmpty()){
                return null;
            }
            return salt;
        }
        catch (Exception e) {
           return null;
        } finally {
            closeConnection(conn);
        }
    }
}
