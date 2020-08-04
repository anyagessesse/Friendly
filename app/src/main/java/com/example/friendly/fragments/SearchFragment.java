package com.example.friendly.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.friendly.FriendRequestsActivity;
import com.example.friendly.R;
import com.example.friendly.adapters.UsersAdapter;
import com.example.friendly.objects.FriendRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * <p>
 * Fragment that allows user to search for other users and add them as friends
 */
public class SearchFragment extends Fragment {
    public static final String TAG = "SearchFragment";

    private EditText searchBar;
    private Button searchButton; //TODO button currently does nothing, possibly remove or find use? maybe use to query users that match from database
    private String searchText;
    private FloatingActionButton friendRequestNotif;

    private RecyclerView recyclerviewUsers;
    private UsersAdapter adapter;
    private List<ParseUser> allUsers;

    private SwipeRefreshLayout swipeContainer;
    private LinearLayout progressBar;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        allUsers = new ArrayList<>();

        // set up loading screen while waiting for Parse
        progressBar = (LinearLayout) view.findViewById(R.id.layout_progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                queryUsers();
                swipeContainer.setRefreshing(false);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.colorAccentDark);

        searchBar = view.findViewById(R.id.text_search);
        searchButton = view.findViewById(R.id.button_search);
        friendRequestNotif = view.findViewById(R.id.friend_request_notif);

        //set up recyclerview of users to search from
        recyclerviewUsers = view.findViewById(R.id.recyclerview_users);
        adapter = new UsersAdapter(getContext());
        recyclerviewUsers.setAdapter(adapter);
        recyclerviewUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        queryUsers();

        //set a listener to filter out users that don't match edittext search bar
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchText = editable.toString();
                filter(searchText);
                //adapter.clear();  //TODO try being able to search through large database of users
                //queryUsers();
            }
        });

        // check for requests
        checkReceivedRequests();
        checkSentRequests();

        // goes to friend request activity when clicked on
        friendRequestNotif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), FriendRequestsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void checkSentRequests() {
        // gets all current friend requests from current user
        ParseQuery<FriendRequest> query = ParseQuery.getQuery(FriendRequest.class);
        query.whereEqualTo(FriendRequest.KEY_FROM_USER, ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<FriendRequest>() {
            @Override
            public void done(List<FriendRequest> receivedRequests, ParseException e) {
                if (e != null) {
                    //query unsuccessful
                    Log.e(TAG, "issue getting requests", e);
                    return;
                }
                ParseUser.getCurrentUser().put("requests", new ArrayList<>());
                //ParseUser.getCurrentUser().getList("requests").clear();
                ParseUser.getCurrentUser().saveInBackground();
                for (int i = 0; i < receivedRequests.size(); i++) {
                    ParseUser.getCurrentUser().getList("requests").add(receivedRequests.get(i).getToUser());
                }
                ParseUser.getCurrentUser().saveInBackground();
            }
        });
    }

    private void checkReceivedRequests() {
        // gets all current friend requests to current user
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
                    // change icon to notify user that there are requests
                    friendRequestNotif.setImageResource(R.drawable.ic_baseline_notification_important_24);
                } else {
                    // change icon to plain bell if no notification present
                    friendRequestNotif.setImageResource(R.drawable.ic_baseline_notifications_24);
                }
            }
        });
    }

    private void filter(String text) {
        //new arraylist of filtered data
        List<ParseUser> filteredUsers = new ArrayList<>();

        //add users to list if username matches
        for (ParseUser user : allUsers) {
            if (user.getUsername().toLowerCase().contains(text.toLowerCase())) {
                filteredUsers.add(user);
            }
        }
        //pass filtered list to the adapter
        adapter.filterList(filteredUsers);
    }

    private void queryUsers() {
        // gets all users that aren't friends of the current user in alphabetical order
        ParseQuery<ParseUser> query = ParseUser.getQuery();

        // get objectId for each friend
        List<ParseUser> friends = ParseUser.getCurrentUser().getList("friends");
        List<String> friendIds = new ArrayList<>();
        for (int i = 0; i < friends.size(); i++) {
            friendIds.add(friends.get(i).getObjectId());
        }

        // get the current user objectId
        friendIds.add(ParseUser.getCurrentUser().getObjectId());

        // get objectId for each user currently requested
        List<ParseUser> requestsList = ParseUser.getCurrentUser().getList("requests");
        for (int j = 0; j < requestsList.size(); j++) {
            friendIds.add(requestsList.get(j).getObjectId());
        }

        query.whereNotContainedIn("objectId", friendIds);
        query.setLimit(20);
        query.addAscendingOrder("username");  //TODO implement infinite scroll of users
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> users, ParseException e) {
                if (e != null) {
                    //query was unsuccessful
                    Log.e(TAG, "issue getting posts", e);
                    return;
                }
                // The query was successful.
                if (!users.isEmpty()) {
                    allUsers = new ArrayList<>();
                    allUsers.addAll(users);  //TODO don't add all users, instead add the users you need
                    adapter.updateUsers(allUsers);
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // refresh the notification button after going back from requests page
        checkReceivedRequests();
    }
}