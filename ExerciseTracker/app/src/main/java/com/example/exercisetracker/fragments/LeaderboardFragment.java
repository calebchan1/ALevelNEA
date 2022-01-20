package com.example.exercisetracker.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.exercisetracker.R;
import com.example.exercisetracker.activities.Activity;
import com.example.exercisetracker.activities.AddFriendsActivity;
import com.example.exercisetracker.activities.RunningActivity;
import com.example.exercisetracker.other.DBhelper;
import com.example.exercisetracker.other.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardFragment extends Fragment implements View.OnClickListener {
    private TableLayout table;
    private Integer timeframe;
    private Map<String, Integer> userScores;
    private Boolean isPublic;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        view.findViewById(R.id.navigateToFriendsActivity).setOnClickListener(this);
        table = view.findViewById(R.id.table_main);
        //by default, leaderboard set to public leaderboard at 24Hr
        timeframe = 1;
        userScores = getPublicLeaderboard();
        isPublic = true;
        if (userScores!=null) {
            createTable(userScores);
        }
        RadioGroup publicPrivateRG = view.findViewById(R.id.leaderboard_publicPrivateRG);
        publicPrivateRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.friendsBtn) {
                    isPublic = false;
                    //when user selects friends radio button, private leaderboard is shown
                    updateTable(getPrivateLeaderboard());
                } else if (checkedId == R.id.allUsersBtn) {
                    isPublic = true;
                    //when user selects all users radio button, public leaderboard is shown
                    updateTable(getPublicLeaderboard());
                }
            }
        });

        RadioGroup periodRG = view.findViewById(R.id.period_radiogroup);
        periodRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //when user changes the time period of the leaderboard
                switch(checkedId){
                    case R.id.oneDay:
                        timeframe = 1;
                        if (isPublic) {
                            updateTable(getPublicLeaderboard());
                        }
                        else{
                            updateTable(getPrivateLeaderboard());
                        }
                        //period of the last 24 hrs
                        break;
                    case R.id.oneMonth:
                        timeframe = 30;
                        if (isPublic) {
                            updateTable(getPublicLeaderboard());
                        }
                        else{
                            updateTable(getPrivateLeaderboard());
                        }
                        //period of the last 30 days
                        break;
                    case R.id.allTime:
                        timeframe = 0;
                        if (isPublic) {
                            updateTable(getPublicLeaderboard());
                        }
                        else{
                            updateTable(getPrivateLeaderboard());
                        }
                        //period of all time
                        break;

                }
            }
        });

        return view;
    }

    private void updateTable(Map<String, Integer> newScores){
        table.removeAllViews();
        if (userScores!= null) {
            userScores.clear();
        }
        userScores = newScores;
        if (newScores!=null) {
            createTable(newScores);
        }
        else{
            Toast.makeText(getContext(), "No Activities stored", Toast.LENGTH_SHORT).show();
        }
    }

    private Map<String, Integer> getPublicLeaderboard() {
        //getting all activities from database, and performing calculations
        //for top most active users
        DBhelper helper = new DBhelper(getContext());
        LinkedHashMap<String, Integer> userScoresHashMap = new LinkedHashMap<>();
        if (helper.getAllActivities(timeframe)) {
            for (String string : helper.getResult()) {
                String[] row = string.split(" ");
                if (userScoresHashMap.get(row[0]) != null) {
                    //if the user exists on the hash map
                    //previous total added on top
                    userScoresHashMap.put(row[0], Integer.parseInt(row[1]) + userScoresHashMap.get(row[0]));

                } else {
                    //if user does not yet exist on hash map
                    userScoresHashMap.put(row[0], Integer.parseInt(row[1]));
                }
            }

            //converting hashmap to arraylist
            ArrayList<String> listOfKeys = new ArrayList<>();
            ArrayList<Integer> listOfValues = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : userScoresHashMap.entrySet()) {
                String name = entry.getKey();
                Integer calories = entry.getValue();
                //parsing to arraylists
                listOfKeys.add(name);
                listOfValues.add(calories);
            }
            //returning the sorted hash map rather than converting to array then sorting
            return sortHashMapByValues(userScoresHashMap);
        }
        else{
            return null;
        }

    }
    private Map<String, Integer> getPrivateLeaderboard() {
        //getting all activities from database, and performing calculations
        //for top most active users
        DBhelper helper = new DBhelper(getContext());
        if (helper.getFriends()){
            for (String friend : helper.getResult()){
                //as query returns multiple columns, must slice string
                //first column is always the id of the friend
                String[] arr = friend.split(" ");
                User.addFriendsList(Integer.parseInt(arr[0]));
            }
            helper.clearResults();
        }
        else{
            Toast.makeText(getContext(), "Could not retrieve your friends", Toast.LENGTH_SHORT).show();
        }
        LinkedHashMap<String, Integer> userScoresHashMap = new LinkedHashMap<>();
        if (helper.getFriendsActivities(timeframe, User.getFriendsList())) {
            for (String string : helper.getResult()) {
                String[] row = string.split(" ");
                if (userScoresHashMap.get(row[0]) != null) {
                    //if the user exists on the hash map
                    //previous total added on top
                    userScoresHashMap.put(row[0], Integer.parseInt(row[1]) + userScoresHashMap.get(row[0]));

                } else {
                    //if user does not yet exist on hash map
                    userScoresHashMap.put(row[0], Integer.parseInt(row[1]));
                }
            }

            //converting hashmap to arraylist
            ArrayList<String> listOfKeys = new ArrayList<>();
            ArrayList<Integer> listOfValues = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : userScoresHashMap.entrySet()) {
                String name = entry.getKey();
                Integer calories = entry.getValue();
                //parsing to arraylists
                listOfKeys.add(name);
                listOfValues.add(calories);
            }
            //returning the sorted hash map rather than converting to array then sorting
            return sortHashMapByValues(userScoresHashMap);
        }
        else{
            return null;
        }
    }

    private Map<String, Integer> sortHashMapByValues(HashMap<String , Integer> passedMap) {
        //lists of values and keys in separate lists
        List<String> mapKeys = new ArrayList<>(passedMap.keySet());
        List<Integer> mapValues = new ArrayList<>(passedMap.values());
        //sort in descending order
        Collections.sort(mapValues, Collections.reverseOrder());
        Collections.sort(mapKeys, Collections.reverseOrder());

        LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();

        Iterator<Integer> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Integer val = valueIt.next();
            Iterator<String> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                String key = keyIt.next();
                Integer comp1 = passedMap.get(key);
                Integer comp2 = val;
                if (comp1.equals(comp2)) {
                    //if two key pair values have the same value
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }

    private void createTable(Map<String, Integer> hashMap){
        //method to create TableLayout view graphic, to display leaderboard
        int pos = 1;
        //creating table headers
        String[] arr = {"Pos. ", "User ","Score"};
        TableRow row = new TableRow(getContext());
        for (String string : arr){
            TextView tv = new TextView(getContext());
            handleViews(tv, string, true, 30);
            row.addView(tv);
        }
        table.addView(row);
        for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
            //getting values from sorted hash map
            Integer fontsize = 20;
            String name = entry.getKey();
            Integer score = entry.getValue();
            //creating a row
            row= new TableRow(getContext());
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);
            TextView posTV = new TextView(getContext());
            handleViews(posTV, Integer.toString(pos) + ". ",false, fontsize);
            TextView nameTV = new TextView(getContext());
            handleViews(nameTV, name,false, fontsize);
            TextView scoreTV = new TextView(getContext());
            handleViews(scoreTV, score.toString(),false, fontsize);
            row.addView(posTV);
            row.addView(nameTV);
            row.addView(scoreTV);
            table.addView(row);
            pos++;
        }
    }

    private void handleViews(TextView view, String text, Boolean bold, Integer fontsize){
        if (bold) {
            Typeface face = ResourcesCompat.getFont(getContext(), R.font.gothicbb);
            view.setTypeface(face);
            view.setText(text + " ");
            view.setTextSize(fontsize);
        }
        else{
            Typeface face = ResourcesCompat.getFont(getContext(), R.font.gothic);
            view.setTypeface(face);
            view.setText(text + " ");
            view.setTextSize(fontsize);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.navigateToFriendsActivity){
            Intent intent1 = new Intent(getContext(), AddFriendsActivity.class);
            startActivity(intent1);
        }
    }
}