package com.example.exercisetracker;

import android.content.Context;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;

public class HistoryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history,container,false);
        dbhelper helper = new dbhelper(getContext());
        if (helper.readActivities()){
            //if activities was read successfully from database
            ArrayList<String> queryResults = helper.getResult();
            RecyclerView historyRV = view.findViewById(R.id.HistoryRV);


            //RecyclerView allows us to dynamically produce card views as a list
            // Arraylist for storing data
            ArrayList<Activity> activityArr = new ArrayList<>();
            for (String query : queryResults){
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

        }
        else{
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
            Activity activity = ActivityArr.get(position);
            holder.exerciseNameTV.setText(activity.getName());
            holder.exerciseDescTV.setText(activity.getDesc());
            holder.exerciseIV.setImageResource(activity.getImg());
            holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //handling when delete button is pressed (deleting activity record on database)
                    dbhelper helper = new dbhelper(context.getApplicationContext());
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
                    Toast.makeText(context.getApplicationContext(), "More Details", Toast.LENGTH_SHORT).show();
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
    }
}

