package com.example.friendly;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.friendly.objects.Status;
import com.parse.ParseException;
import com.parse.ParseUser;

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
    private TextView timeRange;
    private TextView location;
    private Button deleteStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_detail);

        profilePic = findViewById(R.id.image_profile_pic);
        username = findViewById(R.id.text_username);
        description = findViewById(R.id.text_description);
        date = findViewById(R.id.text_date);
        timeRange = findViewById(R.id.text_time_range);
        location = findViewById(R.id.text_location);
        deleteStatus = findViewById(R.id.delete_status);

        //get status clicked on
        status = (Status) getIntent().getParcelableExtra("status");

        //fill detail view with data from status
        username.setText(status.getUser().getUsername());
        description.setText(status.getDescription());
        if (status.getUser().getParseFile("profilePic") != null) {
            Glide.with(this).load(status.getUser().getParseFile("profilePic").getUrl()).circleCrop().into(profilePic);
        }
        //change format of date created
        SimpleDateFormat parser = new SimpleDateFormat("MMM d, yyyy"); //TODO change time format to something better looking
        Date statusDate = status.getCreatedAt();
        String formattedDate = parser.format(statusDate);
        this.date.setText(formattedDate);

        // add time range if end time is specified
        if (status.getDate("endTime") != null) {
            SimpleDateFormat parserEndTime = new SimpleDateFormat("h:mm a");
            Date endTime = status.getDate("endTime");
            String formattedEndTime = parserEndTime.format(endTime);
            timeRange.setVisibility(View.VISIBLE);
            timeRange.setText(getString(R.string.time_range, formattedEndTime));
        }

        // add location if specified
        if (status.getString("city") != null) {
            String city = status.getString("city");
            String state = status.getString("state");
            location.setVisibility(View.VISIBLE);
            location.setText(getString(R.string.location, city, state));
        }

        // show delete button if the status is of the logged in user
        if (status.getUser().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
            deleteStatus.setVisibility(View.VISIBLE);
            deleteStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // launch dialog to confirm deleting the status
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setMessage(getString(R.string.confirm_delete))
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // deleted the status and navigate back to main
                                    try {
                                        status.delete();
                                        status.saveInBackground();
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    Intent intent = new Intent(StatusDetailActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        }
    }
}