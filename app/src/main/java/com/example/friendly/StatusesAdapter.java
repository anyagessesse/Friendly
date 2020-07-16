package com.example.friendly;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.friendly.objects.Status;

import java.util.List;

/**
 * adapter of statuses for recyclerview in the home fragment
 */
public class StatusesAdapter extends RecyclerView.Adapter<StatusesAdapter.ViewHolder> {

    private final Context context;
    private final List<Status> statuses;

    public StatusesAdapter(Context context, List<Status> statuses) {
        this.context = context;
        this.statuses = statuses;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_status, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //gets each status and binds it to item_status layout
        Status status = statuses.get(position);
        holder.bind(status);
    }

    @Override
    public int getItemCount() {
        return statuses.size();
    }

    /**
     * allows eachs status' details to be binded to the item_status layout
     */
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView username;
        private TextView description;
        private ImageView profilePic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.text_username);
            description = itemView.findViewById(R.id.text_description);
            profilePic = itemView.findViewById(R.id.image_profile_pic);

            itemView.setOnClickListener(this);
        }

        public void bind(Status status) {
            username.setText(status.getUser().getUsername());
            description.setText(status.getDescription());

            if (status.getUser().getParseFile("profilePic") != null) {
                Glide.with(context).load(status.getUser().getParseFile("profilePic").getUrl()).circleCrop().into(profilePic);
            } else {
                //use placeholder image if user doesn't have a profile picture yet
                Glide.with(context).load(R.drawable.placeholder).circleCrop().into(profilePic);
            }
        }

        @Override
        public void onClick(View view) {
            //get item position and check if valid
            int pos = getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                //get status at this position
                Status status = statuses.get(pos);
                //create intent for the new activity
                Intent intent = new Intent(context, StatusDetailActivity.class);
                //send to detail view
                intent.putExtra("status", status);
                //show the activity
                context.startActivity(intent);
            }
        }
    }

    // Clean all elements of the recycler
    public void clear() {
        statuses.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Status> list) {
        statuses.addAll(list);
        notifyDataSetChanged();
    }
}
