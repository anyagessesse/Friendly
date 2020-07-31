package com.example.friendly;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.example.friendly.adapters.UsersAdapter;
import com.example.friendly.objects.FriendRequest;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * activity that displays the current users friend requests
 */
public class FriendRequestsActivity extends AppCompatActivity {
    public static final String TAG = "FriendRequestsActivity";

    private RecyclerView recyclerviewFriendRequests;
    private UsersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);

        //set up recyclerview of users to search from
        recyclerviewFriendRequests = findViewById(R.id.recyclerview_friend_requests);
        adapter = new UsersAdapter(this);
        recyclerviewFriendRequests.setAdapter(adapter);
        recyclerviewFriendRequests.setLayoutManager(new LinearLayoutManager(this));
        queryRequests();
    }

    private void queryRequests() {
        // gets all current friend requests
        ParseQuery<FriendRequest> query = ParseQuery.getQuery(FriendRequest.class);
        query.whereEqualTo(FriendRequest.KEY_TO_USER, ParseUser.getCurrentUser());
        query.whereEqualTo(FriendRequest.KEY_ACCEPTED, false);
        query.include(FriendRequest.KEY_FROM_USER);
        query.findInBackground(new FindCallback<FriendRequest>() {
            @Override
            public void done(List<FriendRequest> friendRequests, ParseException e) {
                if (e != null) {
                    //query unsuccessful
                    Log.e(TAG, "issue getting requests", e);
                    return;
                }
                if (!friendRequests.isEmpty()) {
                    List<ParseUser> requests = new ArrayList<>();
                    for (int i = 0; i < friendRequests.size(); i++) {
                        requests.add(friendRequests.get(i).getParseUser(FriendRequest.KEY_FROM_USER));
                    }
                    adapter.updateUsers(requests);
                }
            }
        });
    }
}