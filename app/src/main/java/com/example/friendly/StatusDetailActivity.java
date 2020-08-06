package com.example.friendly;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.friendly.objects.Status;
import com.example.friendly.services.FriendlyGoogleMapsUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * activity used to show a more detailed view of a status
 */
public class StatusDetailActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String TAG = "StatusDetailActivity";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private ImageView profilePic;
    private TextView username;
    private TextView description;
    private TextView date;
    private Status status;
    private TextView timeRange;
    private TextView location;
    private Button deleteStatus;

    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker marker;
    FriendlyGoogleMapsUtil mapsUtil;

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

        // set up map fragment to display location
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this::onMapReady);
        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //get status clicked on
        status = (Status) getIntent().getParcelableExtra("status");

        //fill detail view with data from status
        username.setText(status.getUser().getUsername());
        description.setText(status.getDescription());
        if (status.getUser().getParseFile("profilePic") != null) {
            Glide.with(this).load(status.getUser().getParseFile("profilePic").getUrl()).circleCrop().into(profilePic);
        }
        //change format of date created
        SimpleDateFormat parser = new SimpleDateFormat("MMM d, yyyy");
        Date statusDate = status.getCreatedAt();
        String formattedDate = parser.format(statusDate);
        this.date.setText(formattedDate);

        // add time range if start and end time is specified
        if (status.getDate("endTime") != null) {
            SimpleDateFormat parserTime = new SimpleDateFormat("h:mm a");
            Date endTime = status.getDate("endTime");
            String formattedEndTime = parserTime.format(endTime);
            timeRange.setVisibility(View.VISIBLE);

            if (status.getDate("startTime") != null) {
                Date startTime = status.getDate("startTime");
                String formattedStartTime = parserTime.format(startTime);
                timeRange.setText(formattedStartTime + " - " + formattedEndTime);
            } else {
                timeRange.setText(getString(R.string.time_range, formattedEndTime));
            }
        }

        // add location if specified
        if (!status.getString("city").equals("")) {
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
                    showConfirmationDialog(view.getContext());
                }
            });
        }
    }

    private void showConfirmationDialog(Context context) {
        // launch dialog to confirm deleting the status
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
                .setNegativeButton(getString(R.string.cancel), null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        LatLng latLng = new LatLng(status.getDouble("latitude"), status.getDouble("longitude"));
        Marker statusMarker = map.addMarker(new MarkerOptions().position(latLng));
        mapsUtil = new FriendlyGoogleMapsUtil(StatusDetailActivity.this, this, map, fusedLocationProviderClient, statusMarker);

        // Turn on the My Location layer and the related control on the map.
        mapsUtil.updateLocationUI();

        // Get the current location of the device, set a marker on the map, and zoom to location
        mapsUtil.getDeviceLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mapsUtil.getDeviceLocation();
        mapsUtil.updateLocationUI();
    }
}