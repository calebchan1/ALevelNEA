package com.example.exercisetracker.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.exercisetracker.R;
import com.example.exercisetracker.fragments.ExerciseFragment;
import com.example.exercisetracker.fragments.HistoryFragment;
import com.example.exercisetracker.fragments.LeaderboardFragment;
import com.example.exercisetracker.fragments.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity{
    private BottomNavigationView bottomMenu;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //visuals
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        getWindow().setNavigationBarColor(getResources().getColor(R.color.main_colour));

        bottomMenu = findViewById(R.id.bottom_navigation);
        bottomMenu.setSelectedItemId(R.id.page_1);
        Fragment fragment = new ExerciseFragment();;
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,fragment).commit();
        bottomMenu.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Fragment fragment = new ExerciseFragment();;
                switch(item.getItemId()){
                    case(R.id.page_1):
                        fragment = new ExerciseFragment();

                        break;
                    case(R.id.page_2):
                        fragment = new HistoryFragment();
                        break;
                    case(R.id.page_3):
                        fragment = new LeaderboardFragment();
                        break;
                    case (R.id.page_4):
                        fragment = new SettingsFragment();
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,fragment).commit();
                return true;
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}