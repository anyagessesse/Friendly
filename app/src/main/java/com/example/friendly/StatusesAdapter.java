package com.example.friendly;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.friendly.objects.Status;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * adapter of statuses for recyclerview in the home fragment
 */
public class StatusesAdapter extends RecyclerView.Adapter<StatusesAdapter.ViewHolder> {

    private final Context context;
    private final List<Status> statuses;
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    public static final String TAG = "StatusesAdapter";

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
        private TextView relativeDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.text_username);
            description = itemView.findViewById(R.id.text_description);
            profilePic = itemView.findViewById(R.id.image_profile_pic);
            relativeDate = itemView.findViewById(R.id.text_relative_date);

            itemView.setOnClickListener(this);
        }

        public void bind(Status status) {
            username.setText(status.getUser().getUsername());
            description.setText(status.getDescription());

            //load profile image
            if (status.getUser().getParseFile("profilePic") != null) {
                Glide.with(context).load(status.getUser().getParseFile("profilePic").getUrl()).circleCrop().into(profilePic);
            } else {
                //use placeholder image if user doesn't have a profile picture yet
                Glide.with(context).load(R.drawable.placeholder).circleCrop().into(profilePic);
            }

            //load relative date
            String date = getRelativeTimeAgo(status.getCreatedAt().toString());
            relativeDate.setText(date);
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

    public String getRelativeTimeAgo(String rawJsonDate) {
        String format = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(format, Locale.ENGLISH);
        sf.setLenient(true);

        try {
            long time = sf.parse(rawJsonDate).getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " m";

            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " h";
            } else {
                return diff / DAY_MILLIS + " d";
            }
        } catch (ParseException e) {
            Log.i(TAG, "getRelativeTimeAgo failed");
            e.printStackTrace();
        }

        return "";
    }
}
