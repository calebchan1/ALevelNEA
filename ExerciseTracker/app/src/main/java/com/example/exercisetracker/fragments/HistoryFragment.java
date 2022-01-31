package com.example.exercisetracker.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class HistoryFragment extends Fragment {
    private ActivityAdapter courseAdapter;
    private ArrayList<Activity> activityArr;
    private RecyclerView historyRV;
    private LinearLayoutManager linearLayoutManager;
    private TextView noHistory;
    private ProgressBar progressBar;
    private android.app.Activity mcontext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //saving the attached activity to preserve lifecycle of fragment
        //ensures that UI thread runs on an instance of an activity
        if (context instanceof android.app.Activity) {
            mcontext = (android.app.Activity) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        noHistory = view.findViewById(R.id.noExercises);
        historyRV = view.findViewById(R.id.HistoryRV);
        activityArr = new ArrayList<>();
        courseAdapter = new ActivityAdapter(mcontext, activityArr);
        linearLayoutManager = new LinearLayoutManager(mcontext, LinearLayoutManager.VERTICAL, false);
        progressBar = view.findViewById(R.id.progressBar);


        // setting layout manager and adapter to our recycler view.
        historyRV.setLayoutManager(linearLayoutManager);
        historyRV.setAdapter(courseAdapter);

        //EXECUTING ASYNC TASK to retrieve data history from database
        new GetHistory().execute(true);
        return view;
    }

    private Activity handleQuery(String query) {
        //method to handle query
        String[] arr = query.split(" ");
        int id = Integer.parseInt(arr[0]);
        String name = arr[1];
        //description consisting of date and time
        String[] desc = Arrays.copyOfRange(arr, 2, arr.length);
        String description = "";
        for (String string : desc) {
            description = description + string + " ";
        }
        int img = -1;
        //adding images to corresponding exercise
        switch (name) {
            case "running":
                img = R.drawable.running;
                name = "Running";
                break;
            case "treadmill":
                img = R.drawable.treadmill;
                name = "Treadmill";
                break;
            case "walking":
                img = R.drawable.walking;
                name = "Walking";
                break;
            case "pushup":
                img = R.drawable.pushup;
                name = "Push Up";
                break;
            case "squats":
                img = R.drawable.squat;
                name = "Squats";
                break;
        }
        if (img != -1) {
            return new Activity(name, description, img, id);
        } else {
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
            holder.exerciseDescTV.setText(activity.getDate().toString() + " " + activity.getTimeStarted());
            holder.exerciseIV.setImageResource(activity.getImg());
            holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //handling when delete button is pressed (deleting activity record on database)
                    //first show dialog to user to confirm if they really want to delete the activity from history
                    String title = "Delete Activity?";
                    String message = "Are you sure you want to delete this activity?";
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DBhelper helper = new DBhelper(context.getApplicationContext());
                            if (helper.deleteActivity(activity.getId())) {
                                Toast.makeText(context.getApplicationContext(), "Activity Deleted", Toast.LENGTH_SHORT).show();
                                ActivityArr.remove(activity);
                                notifyItemRemoved(holder.getAdapterPosition());
                                notifyItemRangeChanged(holder.getAdapterPosition(), getItemCount());

                            } else {
                                Toast.makeText(context.getApplicationContext(), "Delete Failed", Toast.LENGTH_SHORT).show();
                            }
                            dialog.cancel();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.cancel();
                        }
                    });
                    builder.setTitle(title)
                            .setMessage(message);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            });
            holder.moreDetailsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //handling when more details button is pressed
                    //displays as a dialogue the full stats of a workout
                    createDialogBuilder(holder.exerciseNameTV.getText().toString(), activity);
                }
            });
        }

        @Override
        public int getItemCount() {
            return ActivityArr.size();
        }

        private void createDialogBuilder(String title, Activity activity) {
            //creating the alert dialog to show stats to user from a previous exercise
            int hours = activity.getDuration() / 3600;
            int minutes = (activity.getDuration() % 3600) / 60;
            int secs = activity.getDuration() % 60;
            Float pace = Float.valueOf(activity.getDistance()) / Float.valueOf(activity.getDuration());
            pace = pace * 3.6f; //converting from m/s to km/hr
            DecimalFormat df = new DecimalFormat("#.##");
            String time = String.format(Locale.getDefault(), "%dh:%02dm:%02ds", hours, minutes, secs);
            String message = "";
            if (activity.getName().equals("Running") || activity.getName().equals("Walking") || activity.getName().equals("Treadmill")) {
                message =
                        String.format(Locale.getDefault(), "Duration: %s\nCalories: %d\nSteps: %d\nDistance: %dm\nAvg. Pace: %skm/hr",
                                time, activity.getCalories(), activity.getSteps(), activity.getDistance(), df.format(pace));
            } else if (activity.getName().equals("Push Up") || activity.getName().equals("Squats")) {
                message =
                        String.format(Locale.getDefault(), "Duration: %s\nCalories Burnt: %d\nReps: %d",
                                time, activity.getCalories(), activity.getReps());
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

    //using async task to retrieve data from database
    private class GetHistory extends AsyncTask<Boolean, Integer, ArrayList<String>> {
        protected ArrayList<String> doInBackground(Boolean... isPublic) {
            DBhelper helper = new DBhelper(mcontext);
            if (helper.readActivities()) {
                if (isCancelled()) return null;
                //if activities was read successfully from database
                ArrayList<String> queryResults = helper.getResult();
                return queryResults;
            } else {
                //activity was not read successfully, recycler view not created
                //show disclaimer text view on screen
                mcontext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        noHistory.setVisibility(View.VISIBLE);
                    }
                });
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(ArrayList<String> queryResults) {
            if (queryResults != null) {
                mcontext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //RecyclerView allows us to dynamically produce card views as a list
                        // Arraylist for storing data
                        activityArr = new ArrayList<>();
                        for (String query : queryResults) {
                            activityArr.add(handleQuery(query));
                        }
                        // we are initializing our adapter class and passing our arraylist to it.
                        courseAdapter = new ActivityAdapter(mcontext, activityArr);
                        //setting a layout manager for our recycler view.
                        // creating vertical list
                        linearLayoutManager = new LinearLayoutManager(mcontext, LinearLayoutManager.VERTICAL, false);
                        // setting layout manager and adapter to our recycler view.
                        historyRV.setLayoutManager(linearLayoutManager);
                        historyRV.setAdapter(courseAdapter);
                    }
                });
            }
            //hiding progress bar
            progressBar.setVisibility(View.GONE);
        }
    }


}

