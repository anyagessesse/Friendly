package com.example.friendly;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.friendly.objects.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    private boolean locationPermissionGranted;
    private Location lastKnownLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LatLng latLng;
    private List<Marker> markers;

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
        markers = new ArrayList<>();


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
            deleteStatus.setOnClickListener(new View.OnClickListener() {  // TODO turn this into a standalone class to make onCreate smaller
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
                            .setNegativeButton(getString(R.string.cancel), null);
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        latLng = new LatLng(status.getDouble("latitude"), status.getDouble("longitude"));
        Marker statusMarker = map.addMarker(new MarkerOptions().position(latLng));
        markers.add(statusMarker);

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device, set a marker on the map, and zoom to location
        getDeviceLocation();
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set a marker on the map of the current location
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                LatLng lastLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                                Marker userLocation = map.addMarker(new MarkerOptions().position(lastLocation).icon(bitmapDescriptorFromVector(StatusDetailActivity.this, R.drawable.ic_baseline_my_location_24)));
                                markers.add(userLocation);
                            }
                        } else {
                            Log.d(TAG, "Current location is null.");
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }

                        // zoom camera to see all markers
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (Marker marker : markers) {
                            builder.include(marker.getPosition());
                        }
                        LatLngBounds bounds = builder.build();
                        int padding = 150; // offset from edges of the map in pixels
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                        map.animateCamera(cu);
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        // turns the given vector resource into a bitmap for marker
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}