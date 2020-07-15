package com.example.friendly;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.friendly.objects.Status;
import com.parse.ParseUser;

import java.util.List;

/**
 * adapter of users for recyclerview in the search fragment
 */
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private final Context context;
    private List<ParseUser> users;


    public UsersAdapter(Context context, List<ParseUser> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public UsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
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
        this.users = filteredUsers;
        notifyDataSetChanged();
    }

    /**
     * allows eachs user's details to be binded to the item_user layout
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvUser;
        private ImageView ivProfilePic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUser = itemView.findViewById(R.id.tvUser);
            ivProfilePic = itemView.findViewById(R.id.ivProfilePic);
        }

        public void bind(ParseUser user) {
            tvUser.setText(user.getUsername());
            if (user.getParseFile("profilePic") != null) {
                Glide.with(context).load(user.getParseFile("profilePic").getUrl()).circleCrop().into(ivProfilePic);
            } else {
                //use placeholder image if user doesn't have a profile picture yet
                Glide.with(context).load(R.drawable.placeholder).circleCrop().into(ivProfilePic);
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
