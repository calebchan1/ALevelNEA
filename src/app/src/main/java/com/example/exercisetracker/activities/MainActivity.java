package com.example.exercisetracker.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.exercisetracker.R;
import com.example.exercisetracker.fragments.ExerciseFragment;
import com.example.exercisetracker.fragments.HistoryFragment;
import com.example.exercisetracker.fragments.LeaderboardFragment;
import com.example.exercisetracker.fragments.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //visuals
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        getWindow().setNavigationBarColor(getResources().getColor(R.color.main_colour));

        //getting bottom menu view
        BottomNavigationView bottomMenu = findViewById(R.id.bottom_navigation);
        bottomMenu.setSelectedItemId(R.id.page_1);
        //by default fragment is on exercisefragment
        Fragment fragment = new ExerciseFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
        bottomMenu.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = new Fragment();
                switch (item.getItemId()) {
                    case (R.id.page_1):
                        //when exercise is navigated to, switch to exercise fragment
                        fragment = new ExerciseFragment();
                        break;
                    case (R.id.page_2):
                        //when history is navigated to, switch to history fragment
                        fragment = new HistoryFragment();
                        break;
                    case (R.id.page_3):
                        //when the leaderboard is navigated to, switch to leaderboard fragment
                        fragment = new LeaderboardFragment();
                        break;
                    case (R.id.page_4):
                        //when settings is navigated to, switch to settings fragment
                        fragment = new SettingsFragment();
                        break;
                }
                //replacing current fragment with new fragment
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();
                return true;
            }
        });

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}