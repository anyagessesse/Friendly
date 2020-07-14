package com.example.friendly.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

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
    private EditText etSearch;
    private Button btnSearch; //TODO button currently does nothing, possibly remove or find use?

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

        etSearch = view.findViewById(R.id.etSearch);
        btnSearch = view.findViewById(R.id.btnSearch);

        //set up recyclerview of users to search from
        rvUsers = view.findViewById(R.id.rvUsers);
        allUsers = new ArrayList<>();
        adapter = new UsersAdapter(getContext(), allUsers);
        rvUsers.setAdapter(adapter);
        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        queryUsers();

        //set a listener to filter out users that don't match edittext search bar
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                filter(editable.toString());
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
        //gets all users in alphabetical order
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.addAscendingOrder("username");  //TODO set limit on number of users displayed and implement infinite scroll
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> users, ParseException e) {
                if (e != null) {
                    //query was unsuccessful
                    Log.e(TAG, "issue getting posts", e);
                    return;
                }
                // The query was successful.
                allUsers.addAll(users);
                adapter.notifyDataSetChanged();
            }
        });
    }
}