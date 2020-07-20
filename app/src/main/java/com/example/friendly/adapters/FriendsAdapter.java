package com.example.friendly.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.friendly.R;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * adapter of users for recyclerview in the search fragment
 */
public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private final Context context;
    private List<ParseUser> users;

    public FriendsAdapter(Context context, List<ParseUser> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public FriendsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsAdapter.ViewHolder holder, int position) {
        //bind each user to item_user layout
        ParseUser user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * allows each user's details to be binded to the item_user layout
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        private ParseUser itemUser;
        private TextView username;
        private ImageView profilePic;
        private ImageView addFriend;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.text_username);
            profilePic = itemView.findViewById(R.id.image_profile_pic);
            addFriend = itemView.findViewById(R.id.image_add_friend);

            addFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //adds friend to friends list in Parse db
                    if (!addFriend.isActivated()) {
                        addFriend.setActivated(true);
                        List<ParseUser> friends = ParseUser.getCurrentUser().getList("friends");
                        if (friends == null) {
                            friends = new ArrayList<>();
                        }
                        friends.add(itemUser);
                        ParseUser.getCurrentUser().put("friends", friends);
                        ParseUser.getCurrentUser().saveInBackground();
                    } else {
                        // possibly remove friend here
                        //addFriend.setActivated(false);
                    }
                }
            });
        }

        public void bind(ParseUser user) {
            itemUser = user;
            try {
                username.setText(user.fetchIfNeeded().getUsername());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (user.getParseFile("profilePic") != null) {
                Glide.with(context).load(user.getParseFile("profilePic").getUrl()).circleCrop().into(profilePic);
            } else {
                //use placeholder image if user doesn't have a profile picture yet
                Glide.with(context).load(R.drawable.placeholder).circleCrop().into(profilePic);
            }
        }
    }

    // Clean all elements of the recycler
    public void clear() {
        users.clear();
        notifyDataSetChanged();
    }

    // Add a list of posts
    public void addAll(List<ParseUser> list) {
        users.addAll(list);
        notifyDataSetChanged();
    }
}
