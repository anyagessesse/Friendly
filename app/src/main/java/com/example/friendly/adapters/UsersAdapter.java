package com.example.friendly.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.friendly.R;
import com.example.friendly.objects.FriendRequest;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * adapter of users for recyclerview in the search fragment
 */
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    public static final String TAG = "UsersAdapter";

    private View layoutView;
    private final Context context;
    private final List<ParseUser> users;
    private List<ParseUser> friends;
    private ParseUser recentlyDeletedUser;
    private int recentlyDeletedUserPosition;

    public UsersAdapter(Context context) {
        this.context = context;
        users = new ArrayList<>();
    }

    public void updateUsers(List<ParseUser> list) {
        users.clear();
        users.addAll(list);
        notifyDataSetChanged();
    }

    public List<ParseUser> getUsers() {
        return users;
    }

    // Clean all elements of the recycler
    public void clear() {
        users.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        layoutView = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.ViewHolder holder, int position) {
        //bind each user to item_user layout
        ParseUser user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void filterList(List<ParseUser> filteredUsers) {
        users.clear();
        users.addAll(filteredUsers);
        notifyDataSetChanged();
    }

    public Context getContext() {
        return context;
    }

    public void deleteItem(int position) {
        recentlyDeletedUser = users.get(position);
        recentlyDeletedUserPosition = position;

        // delete the request in Parse server
        deleteRequest(recentlyDeletedUser);

        // remove the item from the recyclerview
        users.remove(position);
        notifyItemRemoved(position);
        showUndoSnackbar();
    }

    private void deleteRequest(ParseUser recentlyDeletedUser) {
        // queries all requests to delete the selected request
        ParseQuery<FriendRequest> query = ParseQuery.getQuery(FriendRequest.class);
        query.whereEqualTo(FriendRequest.KEY_FROM_USER, recentlyDeletedUser);
        query.whereEqualTo(FriendRequest.KEY_TO_USER, ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<FriendRequest>() {
            @Override
            public void done(List<FriendRequest> objects, ParseException e) {
                if (e != null) {
                    //query unsuccessful
                    Log.e(TAG, "issue getting requests", e);
                    return;
                }
                if (objects != null) {
                    FriendRequest request = objects.get(0);
                    try {
                        request.delete();
                        request.saveInBackground();
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    private void showUndoSnackbar() {
        Snackbar snackbar = Snackbar.make(layoutView, R.string.snack_bar_text, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.snack_bar_undo, v -> undoDelete());
        snackbar.show();
    }

    private void undoDelete() {
        users.add(recentlyDeletedUserPosition, recentlyDeletedUser);
        notifyItemInserted(recentlyDeletedUserPosition);
        addRequest(recentlyDeletedUser);
    }

    private void addRequest(ParseUser recentlyDeletedUser) {
        FriendRequest request = new FriendRequest();
        request.setAccepted(false);
        request.setToUser(ParseUser.getCurrentUser());
        request.setFromUser(recentlyDeletedUser);
        request.saveInBackground();
    }

    /**
     * allows each user's details to be binded to the item_user layout
     */
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ParseUser itemUser;
        private TextView username;
        private ImageView profilePic;
        private ImageView addFriend;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.text_username);
            profilePic = itemView.findViewById(R.id.image_profile_pic);
            addFriend = itemView.findViewById(R.id.image_add_friend);

            addFriend.setOnClickListener(this);
        }

        public void bind(ParseUser user) {
            itemUser = user;
            username.setText(user.getUsername());
            if (user.getParseFile("profilePic") != null) {
                Glide.with(context).load(user.getParseFile("profilePic").getUrl()).circleCrop().into(profilePic);
            } else {
                //use placeholder image if user doesn't have a profile picture yet
                Glide.with(context).load(R.drawable.placeholder).circleCrop().into(profilePic);
            }
        }

        @Override
        public void onClick(View view) {
            // adds friend to friends list in Parse db
            friends = ParseUser.getCurrentUser().getList("friends");

            // creates a new friend request or accepts an existing request
            handleRequest(itemUser);

            // remove friend from search list
            users.remove(getPosition());
            notifyItemRemoved(getPosition());
        }
    }

    private void handleRequest(ParseUser itemUser) {
        // accept the friend request or create a new friend request
        ParseQuery<FriendRequest> query = ParseQuery.getQuery(FriendRequest.class);
        query.whereEqualTo(FriendRequest.KEY_TO_USER, ParseUser.getCurrentUser());
        query.whereEqualTo(FriendRequest.KEY_FROM_USER, itemUser);
        query.include(FriendRequest.KEY_TO_USER);
        query.findInBackground(new FindCallback<FriendRequest>() {
            @Override
            public void done(List<FriendRequest> friendRequests, ParseException e) {
                if (e != null) {
                    //query unsuccessful
                    Log.e(TAG, "issue getting requests", e);
                    return;
                }
                if (friendRequests.isEmpty()) {
                    // there is no active friend request
                    createNewFriendRequest(friendRequests, itemUser);
                } else {
                    acceptFriendRequest(friendRequests, itemUser);
                }
            }
        });
    }

    private void acceptFriendRequest(List<FriendRequest> friendRequests, ParseUser itemUser) {
        //if there is a request then the request gets accepted and the friend is added to friends list
        friendRequests.get(0).put(FriendRequest.KEY_ACCEPTED, true);
        friendRequests.get(0).saveInBackground();
        friends.add(itemUser);

        // sort new friend in list
        Collections.sort(friends, (o1, o2) -> {
            try {
                return o1.fetchIfNeeded().getUsername().compareTo(o2.fetchIfNeeded().getUsername());
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
            return 0;
        });

        ParseUser.getCurrentUser().put("friends", friends);
        ParseUser.getCurrentUser().saveInBackground();

        // add notification channel
        FirebaseMessaging.getInstance().subscribeToTopic(itemUser.getUsername());

        Toast.makeText(context, context.getString((R.string.added_friend), itemUser.getUsername()), Toast.LENGTH_SHORT).show();
    }

    private void createNewFriendRequest(List<FriendRequest> friendRequests, ParseUser itemUser) {
        //creates a new friend request if there isn't currently a request between the current user and clicked user
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setFromUser(ParseUser.getCurrentUser());
        friendRequest.setToUser(itemUser);
        friendRequest.setAccepted(false);
        friendRequest.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    //status unsuccessfully added to db
                    Log.e(TAG, "issue when saving post", e);
                    return;
                }
                // add the user to pending friend requests list for that user
                ParseUser.getCurrentUser().add("requests", itemUser);
                ParseUser.getCurrentUser().saveInBackground();
                Toast.makeText(context, context.getString((R.string.request_sent), itemUser.getUsername()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
