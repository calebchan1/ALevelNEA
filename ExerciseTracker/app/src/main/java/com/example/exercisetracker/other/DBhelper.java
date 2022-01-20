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
import java.util.Locale;
import java.util.Set;

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

    /**
     *  FOLLOWING METHODS DEAL WITH THE ACTUAL USER OF TEH APP
     */
    public boolean registerUser(String username, String password, String forename, String surname, String DOB, String weight, String height) {
        Connection conn = null;
        try {
            conn = createNewConnection();
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
            closeConnection(conn);
        }
    }

    public Boolean login(String username, String password) {
        //handles login validation process
        Connection conn = null;
        try {
            conn = createNewConnection();
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
            addResult(resultset,6);
            Toast.makeText(this.context, "Login Successful", Toast.LENGTH_SHORT).show();

            return true;

        } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            //if connection throws exception, login failed and false is returned
            Toast.makeText(this.context, "Could not connect to database", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        } finally {
            closeConnection(conn);
        }
    }

    public boolean updateUser() {
        Connection conn = null;
        try {
            conn = createNewConnection();
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
            closeConnection(conn);
        }
    }


    public boolean deleteAccount(int userID) {
        Connection conn = null;
        try {
            conn = createNewConnection();
            Statement statement = conn.createStatement();
            //executing SQL statement
            int resultset = statement.executeUpdate(
                    String.format(Locale.getDefault(),"DELETE FROM User WHERE UserID = '%d'", userID)
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
     *  FOLLOWING METHODS DEAL WITH HANDLING ACTIVITIES
     */
    public boolean saveActivity(String exercise, String currDate, String timestarted, String duration, String calories, String steps, String distance, String reps) {
        Connection conn = null;
        try {
            conn = createNewConnection();
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
                            String.format("WHERE Activity.UserID = (SELECT User.UserID FROM User WHERE User.username = '%s') ", User.getUsername()) +
                            "AND Exercise.ExerciseID = Activity.ExerciseID " +
                            "ORDER BY Activity.Date DESC;"
            );

            if (!resultset.next()) {
                Toast.makeText(this.context, "No Activities Stored", Toast.LENGTH_SHORT).show();
                return false;
            }
            resultset.beforeFirst();
            addResult(resultset,9);
            //activities read in form:
            //"exercise name", "date", "time", "duration", "calories","steps","distance","reps"
            return true;

        } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            //if connection throws exception, login failed and false is returned
            Toast.makeText(this.context, "Could not connect to database", Toast.LENGTH_SHORT).show();
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
                    String.format(Locale.getDefault(),"DELETE FROM Activity WHERE ActivityID = '%d'", ActivityID)
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
            closeConnection(conn);
        }
    }

    public boolean getFriendsActivities(Integer duration, Set<Integer> friendsList){

        //by using WHERE IN VALUES(), we can limit which activities are requested
        // in format ('id1','id2',....)
        StringBuilder sqlCondition = new StringBuilder("(");
        for (Integer id: friendsList){
            sqlCondition.append(String.format(Locale.getDefault(), "'%d',", id));
        }
        sqlCondition.deleteCharAt(sqlCondition.length()-1);
        sqlCondition.append(") ");

        Connection conn = null;
        //get request to database for all activities done for private leaderboard
        //includes userID, and first name corresponding to each activity

        try {
            //connecting to database server
            conn = createNewConnection();
            Statement statement = conn.createStatement();
            ResultSet resultset = null;
            if (duration==0) {
                //executing SQL statement
                //requesting leaderboard of all time
                resultset = statement.executeQuery(
                        "SELECT User.firstname, Activity.calories " +
                                "FROM Activity, User " +
                                "WHERE Activity.UserID IN " + sqlCondition +
                                "ORDER BY Activity.Date DESC;"
                );
            }
            else if (duration == 1){
                //executing SQL statement
                //requesting leaderboard from past day
                long millis = System.currentTimeMillis();
                Date date = new java.sql.Date(millis);
                resultset = statement.executeQuery(
                        "SELECT User.firstname, Activity.calories " +
                                "FROM Activity, User " +
                                "WHERE Activity.UserID IN " + sqlCondition + "AND " +
                                String.format("Activity.Date = '%s' ",date.toString()) +
                                "ORDER BY Activity.Date DESC;"
                );
            }
            else if (duration == 30){
                //requesting leaderboard from past 30 days
                long millis = System.currentTimeMillis();
                millis = millis - 2592000000L;
                Date date = new java.sql.Date(millis);
                resultset = statement.executeQuery(
                        "SELECT User.firstname, Activity.calories " +
                                "FROM Activity, User " +
                                "WHERE Activity.UserID IN " + sqlCondition + "AND " +
                                String.format("Activity.Date >= '%s' ",date.toString()) +
                                "ORDER BY Activity.Date DESC;"
                );
            }
            if (!resultset.next()) {
                return false;
            }
            resultset.beforeFirst();
            //dealing with multiple rows
            addResult(resultset,2);
            for (String string:this.result) {
                Toast.makeText(this.context, string, Toast.LENGTH_SHORT).show();
            }
            return true;

        } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            //if connection throws exception, login failed and false is returned
            Toast.makeText(this.context, "Could not connect to database", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        } finally {
            closeConnection(conn);
        }
    }


    public boolean getAllActivities(Integer duration){
        Connection conn = null;
        //get request to database for all activities done for public leaderboard
        //includes userID, and first name corresponding to each activity
        try {
            //connecting to database server
            conn = createNewConnection();
            Statement statement = conn.createStatement();
            ResultSet resultset = null;
            if (duration==0) {
                //executing SQL statement
                //requesting leaderboard of all time
                resultset = statement.executeQuery(
                        "SELECT User.firstname, Activity.calories " +
                                "FROM Activity, User " +
                                "WHERE Activity.UserID = User.UserID " +
                                "ORDER BY Activity.Date DESC;"
                );
            }
            else if (duration == 1){
                //executing SQL statement
                //requesting leaderboard from past day
                long millis = System.currentTimeMillis();
                Date date = new java.sql.Date(millis);
                resultset = statement.executeQuery(
                        "SELECT User.firstname, Activity.calories " +
                                "FROM Activity, User " +
                                "WHERE Activity.UserID = User.UserID AND " +
                                String.format("Activity.Date = '%s' ",date.toString()) +
                                "ORDER BY Activity.Date DESC;"
                );
            }
            else if (duration == 30){
                //requesting leaderboard from past 30 days
                long millis = System.currentTimeMillis();
                millis = millis - 2592000000L;
                Date date = new java.sql.Date(millis);
                resultset = statement.executeQuery(
                        "SELECT User.firstname, Activity.calories " +
                                "FROM Activity, User " +
                                "WHERE Activity.UserID = User.UserID AND " +
                                String.format("Activity.Date >= '%s' ",date.toString()) +
                                "ORDER BY Activity.Date DESC;"
                );
            }
            if (!resultset.next()) {
                return false;
            }
            resultset.beforeFirst();
            //dealing with multiple rows
            addResult(resultset,2);
            return true;

        } catch (SQLException | IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            //if connection throws exception, login failed and false is returned
            Toast.makeText(this.context, "Could not connect to database", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return false;
        } finally {
            closeConnection(conn);
        }
    }


    /**
     *  FOLLOWING METHODS DEAL WITH FRIENDS SYSTEM IN THE APP
     */
    public boolean getUsers(String name){
        Connection conn = null;
        try{
            conn = createNewConnection();
            Statement statement = conn.createStatement();
            ResultSet resultset = null;
            //getting all users on the database, based on search
            //removing user which has the same userID as the user currently in the app
            //(you cannot be a friend of yourself)
            resultset = statement.executeQuery(
                    "SELECT UserID, firstname, surname, username " +
                            "FROM User WHERE (firstname LIKE " +
                            "'%"+name +"%' " +
                            "OR surname LIKE "+   "'%"+name +"%') " +
                            String.format(Locale.getDefault(),"AND UserID <> '%d' ",User.getUserID())

            );
            if (!resultset.next()) {
                return false;
            }
            resultset.beforeFirst();
            addResult(resultset,4);
            return true;
        }
        catch(Exception e){
            return  false;
        }
        finally {
            closeConnection(conn);
        }
    }

    public boolean getFriends(){
        Connection conn = null;
        try{
            //checking both columns for any friendships between users
            conn = createNewConnection();
            Statement statement = conn.createStatement();
            ResultSet resultset = null;
            resultset = statement.executeQuery(
                    "SELECT Friends.User2ID, User.firstname, User.surname, User.username " +
                            "FROM Friends, User "+
                            String.format(Locale.getDefault()," WHERE Friends.USER1ID = '%d' ",User.getUserID()) +
                            "AND User.UserID = Friends.USER2ID"
            );
            if (!resultset.next()) {
                return false;
            }
            User.getFriendsList().clear();
            resultset.beforeFirst();
            addResult(resultset,4);
            return true;
        }
        catch(Exception e){
            return  false;
        }
        finally {
            closeConnection(conn);
        }
    }

    public boolean addFriend(int user1, int user2){
        //adding friend relationship to database
        //where user1 is user who initiated friendship
        Connection conn = null;
        try{
            conn = createNewConnection();
            Statement statement = conn.createStatement();
            int resultset = 0;
            resultset = statement.executeUpdate(
                    "INSERT INTO Friends " +
                            String.format(Locale.getDefault(),"VALUES ('%d','%d');",user1,user2)
            );
            if (resultset == 0) {
                return false;
            }
            //adding to User class set of friends
            User.addFriendsList(user2);
            return true;
        }
        catch(Exception e){
            Toast.makeText(this.context, "Could not connect to database", Toast.LENGTH_SHORT).show();
            return  false;
        }
        finally {
            closeConnection(conn);
        }
    }

    public boolean removeFriend(int user1, int user2){
        //adding friend relationship to database
        //where user1 is user who initiated friendship
        Connection conn = null;
        try{
            conn = createNewConnection();
            Statement statement = conn.createStatement();
            int resultset = 0;
            resultset = statement.executeUpdate(
                    "DELETE FROM Friends " +
                            String.format(Locale.getDefault(),"WHERE User1ID = '%d' AND User2ID = '%d'",user1,user2)
            );
            if (resultset == 0) {
                return false;
            }
            //adding to User class set of friends
            User.removeFriendsList(user2);
            return true;
        }
        catch(Exception e){
            Toast.makeText(this.context, "Could not connect to database", Toast.LENGTH_SHORT).show();
            return  false;
        }
        finally {
            closeConnection(conn);
        }
    }


    public ArrayList<String> getResult() {
        return result;
    }

    public void addResult(ResultSet resultset,int columns) throws SQLException {
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
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        //connecting to database server
        conn = DriverManager.getConnection(url, DBhelper.dbuser, DBhelper.dbpassword);
        return conn;
    }

    private void closeConnection(Connection conn){
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearResults(){
        this.result.clear();
    }
}
