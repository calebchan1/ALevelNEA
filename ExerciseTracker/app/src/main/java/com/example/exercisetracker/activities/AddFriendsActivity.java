package com.example.exercisetracker.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exercisetracker.R;
import com.example.exercisetracker.fragments.HistoryFragment;
import com.example.exercisetracker.other.DBhelper;
import com.example.exercisetracker.other.Friend;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;

public class AddFriendsActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText searchEditText;

    //recycler view
    private AddFriendsActivity.FriendAdapter courseAdapter;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private ArrayList<Friend> friendArr;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //visuals
        setContentView(R.layout.activity_addfriends);
        getSupportActionBar().hide();
        getWindow().setNavigationBarColor(getResources().getColor(R.color.main_colour));

        //setting on click listener to class
        findViewById(R.id.goBackBtn).setOnClickListener(this);
        findViewById(R.id.searchBtn).setOnClickListener(this);
        TextInputLayout input = findViewById(R.id.searchView);
        searchEditText = input.getEditText();

        //recycler views
        recyclerView = findViewById(R.id.friendsRV);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        friendArr = new ArrayList<>();
        // we are initializing our adapter class and passing our arraylist to it.
        courseAdapter = new AddFriendsActivity.FriendAdapter(getApplicationContext(), friendArr);
        recyclerView.setAdapter(courseAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goBackBtn:
                //when user wants to go back to main app
                finish();
                break;
            case R.id.searchBtn:
                //when user wants to search for other users
                if (!searchEditText.getText().toString().equals("")) {
                    DBhelper helper = new DBhelper(this);
                    if (helper.getUsers(searchEditText.getText().toString())){
                        //list of users passed to recycler view
                        //resetting friendArr for new query
                        friendArr.clear();
                        courseAdapter.notifyDataSetChanged();
                        for (String row : helper.getResult()){
                            friendArr.add(handleQuery(row));
                            courseAdapter.notifyItemInserted(courseAdapter.getItemCount()-1);
                            // setting layout manager and adapter to our recycler view.

                        }

                    }
                    else{
                        friendArr.clear();
                        courseAdapter.notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(), "No Users Found", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    //user has searched empty query
                    friendArr.clear();
                    courseAdapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "You have not entered anything", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    private Friend handleQuery(String query){
        String[] arr = query.split(" ");
        int id = Integer.parseInt(arr[0]);
        String firstname = arr[1];
        String surname = arr[2];
        String username = arr[3];
        return new Friend(id,firstname,surname,username);
    }


    //adapter class to exchange information between card views created and friend details
    public static class FriendAdapter extends RecyclerView.Adapter<AddFriendsActivity.FriendAdapter.Viewholder> {
        private Context context;
        private ArrayList<Friend> friendsArr;

        public FriendAdapter(Context context, ArrayList<Friend> friendsArr) {
            this.context = context;
            this.friendsArr = friendsArr;

        }

        @NonNull
        @Override
        public AddFriendsActivity.FriendAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //inflate the layout for each item of recycler view.
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friends_card_layout, parent, false);
            return new AddFriendsActivity.FriendAdapter.Viewholder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AddFriendsActivity.FriendAdapter.Viewholder holder, int position) {
            //setting the instance holder data from the friend object at that position in the array
            Friend friend = friendsArr.get(position);
            holder.realNameTV.setText(friend.getFirstname() + " " +friend.getSurname());
            holder.usernameIDTV.setText("username: "+ friend.getUsername());
            holder.addFriendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //handling when user clicks on add friend on a specific user
                    Toast.makeText(context.getApplicationContext(), "Friend Added", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return friendsArr.size();
        }

        // View holder class for initializing of views such as TextView and Imageview.
        public static class Viewholder extends RecyclerView.ViewHolder {
            private final TextView realNameTV;
            private final TextView usernameIDTV;
            private final Button addFriendBtn;

            public Viewholder(@NonNull View itemView) {
                super(itemView);
                //binding views to instance of Viewholder
                realNameTV = itemView.findViewById(R.id.userRealName);
                usernameIDTV = itemView.findViewById(R.id.usernameID);
                addFriendBtn = itemView.findViewById(R.id.addFriendBtn);
            }
        }
    }
}


