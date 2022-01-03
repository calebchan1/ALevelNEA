package com.example.exercisetracker.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.exercisetracker.R;
import com.example.exercisetracker.other.dbhelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LeaderboardFragment extends Fragment {
    private TableLayout table;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        table = view.findViewById(R.id.table_main);
        //by default, leaderboard set to public leaderboard
        Map<String, Integer> userScores = getPublicLeaderboard();
        if (userScores!=null) {
            createTable(userScores);
        }
        RadioGroup radioGroup = view.findViewById(R.id.leaderboard_radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.friendsBtn) {
                    //when user selects friends radio button, private leaderboard is shown
                    table.removeAllViews();
                    Map<String, Integer> userScores = getPrivateLeaderboard();
                    if (userScores!=null) {
                        createTable(userScores);
                    }
                } else if (checkedId == R.id.allUsersBtn) {
                    //when user selects all users radio button, public leaderboard is shown
                    table.removeAllViews();
                    Map<String, Integer> userScores = getPublicLeaderboard();
                    if (userScores!=null) {
                        createTable(userScores);
                    }
                }
            }
        });
        return view;
    }

    private Map<String, Integer> getPublicLeaderboard() {
        //getting all activities from database, and performing calculations
        //for top most active users
        dbhelper helper = new dbhelper(getContext());
        LinkedHashMap<String, Integer> userScoresHashMap = new LinkedHashMap<>();
        if (helper.getAllActivities()) {
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
        return null;
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
        for (Map.Entry<String, Integer> entry : hashMap.entrySet()) {
            //getting values from sorted hash map
            String name = entry.getKey();
            Integer score = entry.getValue();
            //creating a row
            TableRow row= new TableRow(getContext());
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);
            TextView nameTV = new TextView(getContext());
            handleViews(nameTV, name);
            TextView scoreTV = new TextView(getContext());
            handleViews(scoreTV, score.toString());
            row.addView(nameTV);
            row.addView(scoreTV);
            table.addView(row);
        }
    }

    private void handleViews(TextView view, String text){
        view.setText(text);
        view.setTextSize(30);
    }
}