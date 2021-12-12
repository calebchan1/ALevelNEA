package com.example.exercisetracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HistoryFragment extends Fragment {

    private String queryResults;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        dbhelper helper = new dbhelper(getContext());
        if (helper.readActivities()){
            Toast.makeText(getContext(), "Activity History Read", Toast.LENGTH_SHORT).show();
            queryResults = helper.getResult();
            Toast.makeText(getContext(), queryResults, Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getContext(), "Activity History not read", Toast.LENGTH_SHORT).show();
        }


        return inflater.inflate(R.layout.fragment_history,container,false);
    }
}
