package com.example.friendly;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friendly.objects.Status;

import java.util.List;

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
        Status status = statuses.get(position);
        holder.bind(status);
    }

    @Override
    public int getItemCount() {
        return statuses.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvUser;
        private TextView tvDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUser = itemView.findViewById(R.id.tvUser);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }

        public void bind(Status status) {
            tvUser.setText(status.getUser().getUsername());
            tvDescription.setText(status.getDescription());
        }
    }
}
