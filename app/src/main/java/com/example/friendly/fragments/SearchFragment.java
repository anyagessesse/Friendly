package com.example.friendly.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.friendly.R;
import com.example.friendly.UsersAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {
    public static final String TAG = "SearchFragment";

    private RecyclerView rvUsers;
    private UsersAdapter adapter;
    private List<ParseUser> allUsers;

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

        rvUsers = view.findViewById(R.id.rvUsers);
        allUsers = new ArrayList<>();
        adapter = new UsersAdapter(getContext(), allUsers);
        rvUsers.setAdapter(adapter);
        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));

        queryUsers();

    }

    private void queryUsers() {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.addAscendingOrder("username");
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> users, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "issue getting posts", e);
                    return;
                }
                // The query was successful.
                for (ParseUser user : users) {
                    Log.i(TAG, "username: " + user.getUsername().toString());
                }
                allUsers.addAll(users);
                adapter.notifyDataSetChanged();
            }
        });
    }


}