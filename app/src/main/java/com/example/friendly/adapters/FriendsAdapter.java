package com.example.friendly.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.example.friendly.objects.FriendRemoval;
import com.google.firebase.messaging.FirebaseMessaging;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * adapter of users for recyclerview in the search fragment
 */
public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private final Context context;
    private final List<ParseUser> users;

    public FriendsAdapter(Context context) {
        this.context = context;
        users = new ArrayList<>();
    }

    public void updateUsers(List<ParseUser> list) {
        users.addAll(list);
        notifyDataSetChanged();
    }

    // Clean all elements of the recycler
    public void clear() {
        users.clear();
        notifyDataSetChanged();
    }

    public List<ParseUser> getUsers() {
        return users;
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
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ParseUser itemUser;
        private TextView username;
        private ImageView profilePic;
        private ImageView addFriend;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.text_username);
            profilePic = itemView.findViewById(R.id.image_profile_pic);
            addFriend = itemView.findViewById(R.id.image_add_friend);
            addFriend.setActivated(true);

            addFriend.setOnClickListener(this);
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

        @Override
        public void onClick(View view) {
            // confirms that the clicked on friend should be removed
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setMessage(view.getContext().getString(R.string.confirm_remove, itemUser.getUsername()))
                    .setCancelable(false)
                    .setPositiveButton(view.getContext().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // if confirm is clicked, remove the friend
                            removeFriend();
                        }
                    })
                    .setNegativeButton(view.getContext().getString(R.string.cancel), null);
            AlertDialog alert = builder.create();
            alert.show();
        }

        private void removeFriend() {
            //create new friend removal object
            FriendRemoval removal = new FriendRemoval();
            removal.setFromUser(ParseUser.getCurrentUser());
            removal.setToUser(itemUser);
            removal.saveInBackground();

            //remove friend from friends list in db
            List<ParseUser> friends = ParseUser.getCurrentUser().getList("friends");
            for (int i = 0; i < friends.size(); i++) {
                if (friends.get(i).getObjectId().equals(itemUser.getObjectId())) {
                    friends.remove(i);
                    i--;
                }
            }
            ParseUser.getCurrentUser().put("friends", friends);
            ParseUser.getCurrentUser().saveInBackground();

            // remove notification channel
            FirebaseMessaging.getInstance().unsubscribeFromTopic(itemUser.getUsername());

            // remove friend from friends list on profile page
            users.remove(getPosition());
            notifyItemRemoved(getPosition());
            Toast.makeText(context, context.getString(R.string.removed_friend, itemUser.getUsername()), Toast.LENGTH_SHORT).show();
        }
    }
}
