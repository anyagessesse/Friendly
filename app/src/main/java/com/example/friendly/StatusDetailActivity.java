package com.example.friendly;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.friendly.objects.Status;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * activity used to show a more detailed view of a status
 */
public class StatusDetailActivity extends AppCompatActivity {
    public static final String TAG = "StatusDetailActivity";

    private ImageView profilePic;
    private TextView username;
    private TextView description;
    private TextView date;
    private Status status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_detail);

        profilePic = findViewById(R.id.image_profile_pic);
        username = findViewById(R.id.text_username);
        description = findViewById(R.id.text_description);
        date = findViewById(R.id.text_date);

        //get status clicked on
        status = (Status) getIntent().getParcelableExtra("status");

        //fill detail view with data from status
        username.setText(status.getUser().getUsername());
        description.setText(status.getDescription());
        if (status.getUser().getParseFile("profilePic") != null) {
            Glide.with(this).load(status.getUser().getParseFile("profilePic").getUrl()).circleCrop().into(profilePic);
        }
        //change format of date created
        SimpleDateFormat parser = new SimpleDateFormat("HH:mm EEE MMM d yyyy"); //TODO change time format to something better looking
        Date statusDate = status.getCreatedAt();
        String formattedDate = parser.format(statusDate);
        this.date.setText(formattedDate);

    }
}