package com.example.friendly.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.friendly.R;
import com.example.friendly.adapters.StatusesAdapter;
import com.example.friendly.objects.Status;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * <p>
 * Fragment that displays active statuses of user's friends
 */
public class HomeFragment extends Fragment {
    public static final String TAG = "HomeFragment";

    private RecyclerView recyclerviewStatuses;
    private StatusesAdapter adapter;
    private List<Status> allStatuses;
    private SwipeRefreshLayout swipeContainer;
    ProgressDialog progressDialog;

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

        // show progress dialog while waiting for query from Parse
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle(getString(R.string.loading));
        progressDialog.setMessage(getString(R.string.wait));
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryStatuses();
                adapter.clear();
                adapter.addAll(allStatuses);
                swipeContainer.setRefreshing(false);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.colorAccentDark);

        //set up recyclerview with all active statuses
        recyclerviewStatuses = view.findViewById(R.id.recyclerview_statuses);
        allStatuses = new ArrayList<>();
        adapter = new StatusesAdapter(getContext(), allStatuses);
        recyclerviewStatuses.setAdapter(adapter);
        recyclerviewStatuses.setLayoutManager(new LinearLayoutManager(getContext()));
        queryStatuses();
    }

    //TODO change query statuses to only update the one new status instead of reloading all statuses
    //gets statuses from Parse db
    private void queryStatuses() {
        //get statuses from friends
        ParseQuery<Status> queryFriendsStatuses = ParseQuery.getQuery(Status.class);
        List<ParseUser> friends = ParseUser.getCurrentUser().getList("friends");
        queryFriendsStatuses.whereContainedIn(Status.KEY_USER, friends);

        //get statuses from current user
        ParseQuery<Status> queryUsersStatuses = ParseQuery.getQuery(Status.class);
        queryUsersStatuses.whereEqualTo(Status.KEY_USER, ParseUser.getCurrentUser());

        //combine queries
        List<ParseQuery<Status>> queries = new ArrayList<>();
        queries.add(queryFriendsStatuses);
        queries.add(queryUsersStatuses);
        ParseQuery<Status> query = ParseQuery.or(queries);

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
                allStatuses.addAll(statuses);
                adapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }
        });
    }
}