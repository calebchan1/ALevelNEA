package com.example.exercisetracker.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exercisetracker.R;
import com.example.exercisetracker.activities.Activity;
import com.example.exercisetracker.other.DBhelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class HistoryFragment extends Fragment {



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history,container,false);
        RecyclerView historyRV = view.findViewById(R.id.HistoryRV);

        DBhelper helper = new DBhelper(getContext());
        if (helper.readActivities()) {
            //if activities was read successfully from database
            ArrayList<String> queryResults = helper.getResult();

            //RecyclerView allows us to dynamically produce card views as a list
            // Arraylist for storing data
            ArrayList<Activity> activityArr = new ArrayList<>();
            for (String query : queryResults) {
                activityArr.add(handleQuery(query));
            }
            // we are initializing our adapter class and passing our arraylist to it.
            ActivityAdapter courseAdapter = new ActivityAdapter(getContext(), activityArr);
            //setting a layout manager for our recycler view.
            // creating vertical list
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            // setting layout manager and adapter to our recycler view.
            historyRV.setLayoutManager(linearLayoutManager);
            historyRV.setAdapter(courseAdapter);

        } else {
            //activity was not read successfully, recycler view not created
            Toast.makeText(getContext(), "Activity History not read", Toast.LENGTH_SHORT).show();
        }

        return view;

    }

    private Activity handleQuery(String query){
        String[] arr = query.split(" ");
        int id = Integer.parseInt(arr[0]);
        String name = arr[1];
        //description consisting of date and time
        String[] desc = Arrays.copyOfRange(arr, 2, arr.length);
        String description = "";
        for (String string : desc){
            description = description + string + " ";
        }
        int img = -1;
        switch (name) {
            case "running":
                img = R.drawable.runningman;
                name = "Running";
                break;
            case "treadmill":
                img = R.drawable.treadmill;
                name = "Treadmill";
                break;
            case "walking":
                img = R.drawable.walkingimg;
                name = "Walking";
                break;
            case "pushup":
                img = R.drawable.pushupimg;
                name = "Push Up";
                break;
        }
        if (img!=-1){return new Activity(name,description,img,id);}
        else{
            return null;
        }

    }



    //handling dynamic card production using recycler views
    public static class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.Viewholder> {

        private final Context context;
        private final ArrayList<Activity> ActivityArr;

        public ActivityAdapter(Context context, ArrayList<Activity> courseModelArrayList) {
            this.context = context;
            this.ActivityArr = courseModelArrayList;
        }

        @NonNull
        @Override
        public ActivityAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //inflate the layout for each item of recycler view.
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_card_layout, parent, false);
            return new Viewholder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Viewholder holder, int position) {
            //set data to textview and imageview of each card layout
            //get details from activity array holding items of activity class
            //providing details for the holder views
            Activity activity = ActivityArr.get(position);
            holder.exerciseNameTV.setText(activity.getName());
            holder.exerciseDescTV.setText(activity.getDate().toString() + " " +activity.getTimeStarted());
            holder.exerciseIV.setImageResource(activity.getImg());
            holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //handling when delete button is pressed (deleting activity record on database)
                    DBhelper helper = new DBhelper(context.getApplicationContext());
                    if (helper.deleteActivity(activity.getId())) {
                        Toast.makeText(context.getApplicationContext(), "Activity Deleted", Toast.LENGTH_SHORT).show();
                        ActivityArr.remove(activity);
                        notifyItemRemoved(holder.getAdapterPosition());
                        notifyItemRangeChanged(holder.getAdapterPosition(),getItemCount());

                    }
                    else{
                        Toast.makeText(context.getApplicationContext(), "Delete Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            holder.moreDetailsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //handling when more details button is pressed
                    //displays as a dialogue the full stats of a workout
                    createDialogBuilder(holder.exerciseNameTV.getText().toString(),activity);
                }
            });
        }

        @Override
        public int getItemCount() {
            return ActivityArr.size();
        }

        // View holder class for initializing of views such as TextView and Imageview.
        public static class Viewholder extends RecyclerView.ViewHolder {
            private final ImageView exerciseIV;
            private final TextView exerciseNameTV;
            private final TextView exerciseDescTV;
            private final Button deleteBtn;
            private final Button moreDetailsBtn;

            public Viewholder(@NonNull View itemView) {
                super(itemView);
                //binding views to instance of Viewholder
                exerciseIV = itemView.findViewById(R.id.ExeciseImg);
                exerciseNameTV = itemView.findViewById(R.id.ExerciseName);
                exerciseDescTV = itemView.findViewById(R.id.ExerciseDesc);
                deleteBtn = itemView.findViewById(R.id.ExerciseDeleteBtn);
                moreDetailsBtn = itemView.findViewById(R.id.ExerciseDetailsBtn);
            }
        }

        private void createDialogBuilder(String title, Activity activity){
            //creating the alert dialog to show stats to user from a previous exercise
            int hours = activity.getDuration() / 3600;
            int minutes = (activity.getDuration() % 3600) / 60;
            int secs = activity.getDuration() % 60;
            String time = String.format(Locale.getDefault(), "%dh:%02dm:%02ds", hours, minutes, secs);
            String message = "";
            if (activity.getName().equals("Running") || activity.getName().equals("Walking") || activity.getName().equals("Treadmill")){
                message =
                        String.format(Locale.getDefault(),"Duration: %s\nCalories: %d\nSteps: %d\nDistance: %dm",
                                time,activity.getCalories(),activity.getSteps(),activity.getDistance());
            }
            else  if (activity.getName().equals("Push Up")){
                message =
                        String.format(Locale.getDefault(),"Duration: %s\nCalories Burnt: %d\nReps: %d",
                                time,activity.getCalories(),activity.getReps());
            }

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this.context);
            builder.setNegativeButton("dismiss", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            })
            ;
            builder.setTitle(title)
            .setMessage(message)
            ;
            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }
}

