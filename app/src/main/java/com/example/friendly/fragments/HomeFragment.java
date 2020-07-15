package com.example.friendly.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.friendly.R;
import com.example.friendly.StatusesAdapter;
import com.example.friendly.objects.Status;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * <p>
 * Fragment that displays active statuses of user's friends
 */
public class HomeFragment extends Fragment {
    public static final String TAG = "HomeFragment";

    private RecyclerView rvStatuses;
    private StatusesAdapter adapter;
    private List<Status> allStatuses;
    private SwipeRefreshLayout swipeContainer;
    private int skip; //TODO to be used for infinite scroll later

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                skip = 0;
                queryStatuses();
                adapter.clear();
                adapter.addAll(allStatuses);
                swipeContainer.setRefreshing(false);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.colorAccentDark);

        //set up recyclerview with all active statuses
        rvStatuses = view.findViewById(R.id.rvStatuses);
        allStatuses = new ArrayList<>();
        adapter = new StatusesAdapter(getContext(), allStatuses);
        rvStatuses.setAdapter(adapter);
        rvStatuses.setLayoutManager(new LinearLayoutManager(getContext()));
        queryStatuses();
    }

    //TODO change query statuses to only update the one new status instead of reloading all statuses
    //gets all statuses from Parse db
    private void queryStatuses() {
        ParseQuery<Status> query = ParseQuery.getQuery(Status.class);
        query.include(Status.KEY_USER);
        query.setLimit(10); //TODO change limit
        query.addDescendingOrder(Status.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Status>() {
            @Override
            public void done(List<Status> statuses, ParseException e) {
                if (e != null) {
                    //query unsuccessful
                    Log.e(TAG, "issue getting posts", e);
                    return;
                }
                //query successful
                allStatuses.addAll(statuses);
                adapter.notifyDataSetChanged();
            }
        });
    }
}