package com.example.friendly;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.example.friendly.objects.MySingleton;
import com.example.friendly.objects.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * activity that can be used to create or update a status
 */
public class StatusUpdateActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "StatusUpdateActivity";
    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    private String serverKey;
    final private String contentType = "application/json";

    private ImageView profilePic;
    private TextView username;
    private TextView description;
    private Button postStatusButton;
    private EditText startTime;
    private EditText endTime;

    private Status newStatus;
    private Date dateStart;
    private Date dateEnd;
    private String stateName = "";
    private String cityName = "";

    private Double lat;
    private Double lon;
    private SupportMapFragment mapFragment;
    private GoogleMap map;


    String NOTIFICATION_TITLE;
    String NOTIFICATION_MESSAGE;
    String TOPIC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_update);

        serverKey = "key=" + getString(R.string.firebase_key);

        profilePic = findViewById(R.id.image_profile_pic);
        username = findViewById(R.id.text_username);
        description = findViewById(R.id.text_description);
        postStatusButton = findViewById(R.id.button_post_status);
        startTime = findViewById(R.id.text_start_time);
        endTime = findViewById(R.id.text_end_time);

        // set up map fragment to display location
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_location);
        mapFragment.getMapAsync(this::onMapReady);

        //load profile image
        if (ParseUser.getCurrentUser().getParseFile("profilePic") != null) {
            Glide.with(this).load(ParseUser.getCurrentUser().getParseFile("profilePic").getUrl()).circleCrop().into(profilePic);
        } else {
            //use placeholder image if user doesn't have a profile picture yet
            Glide.with(this).load(R.drawable.placeholder).circleCrop().into(profilePic);
        }
        // load username
        username.setText(ParseUser.getCurrentUser().getString("username"));

        //user can search for a location to set in their status
        //initializes google places api key
        String apiKey = getString(R.string.api_key);
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }
        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setHint(getString(R.string.location_text));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                //parses place to save to database
                LatLng latLng = place.getLatLng();
                lat = latLng.latitude;
                lon = latLng.longitude;
                // add marker to the map
                setMapMarker();
                Geocoder geocoder = new Geocoder(StatusUpdateActivity.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                    stateName = addresses.get(0).getAdminArea();
                    cityName = addresses.get(0).getLocality();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(@NonNull com.google.android.gms.common.api.Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        // time picker pops up to choose time when clicked
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTime(startTime, true);
            }
        });

        // time picker pops up to choose time when clicked
        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTime(endTime, false);
            }
        });

        //adds status to db and navigates back to home fragment
        postStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String descriptionText = description.getText().toString();

                if (dateEnd == null || lat == null) {
                    // launch dialog tell user to add end time and location
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setMessage(getString(R.string.post_warning))
                            .setCancelable(false)
                            .setNegativeButton(getString(R.string.ok), null);
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    saveStatus(descriptionText, ParseUser.getCurrentUser(), dateStart, dateEnd, stateName, cityName, lat, lon);

                    createNotification();

                    //go to home fragment to display statuses
                    Intent intent = new Intent(StatusUpdateActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void createNotification() {
        // Creating the notification to be sent
        TOPIC = "/topics/" + ParseUser.getCurrentUser().getUsername(); // TODO change topic after testing
        NOTIFICATION_TITLE = ParseUser.getCurrentUser().getUsername() + " posted a new status.";
        if (description.getText().toString().equals("")) {
            // set default notification message if the description is blank
            NOTIFICATION_MESSAGE = "I'm free!";
        } else {
            NOTIFICATION_MESSAGE = description.getText().toString();
        }


        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();
        try {
            notificationBody.put("title", NOTIFICATION_TITLE);
            notificationBody.put("message", NOTIFICATION_MESSAGE);

            notification.put("to", TOPIC);
            notification.put("data", notificationBody);
        } catch (JSONException e) {
            Log.e(TAG, "onCreate: " + e.getMessage());
        }
        // Send the notification
        sendNotification(notification);
    }

    private void sendNotification(JSONObject notification) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "onResponse: " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG, "onErrorResponse: Didn't work");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * time-picker pops up and time is saved in EditText
     *
     * @param time        the text field that the time will be saved in
     * @param isStartTime if true, timepicker is setting start time, if false, timepicker is setting end time
     */
    private void getTime(EditText time, Boolean isStartTime) {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(StatusUpdateActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                // parse the time
                SimpleDateFormat parserTime = new SimpleDateFormat("h:mm a");
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, selectedHour);
                cal.set(Calendar.MINUTE, selectedMinute);
                if (isStartTime) {
                    dateStart = cal.getTime();
                    time.setText(parserTime.format(dateStart));
                } else {
                    dateEnd = cal.getTime();
                    time.setText(parserTime.format(dateEnd));
                }
            }
        }, hour, minute, false);
        timePickerDialog.show();
    }

    private void setMapMarker() {
        if (lat != null) {
            LatLng location = new LatLng(lat, lon);
            map.addMarker(new MarkerOptions().position(location));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 5));
        }
    }

    //saves a status to the parse database
    private void saveStatus(String description, ParseUser currentUser, Date startDate, Date endDate, String stateName, String cityName, Double lat, Double lon) {
        newStatus = new Status();
        newStatus.setDescription(description);
        newStatus.setUser(currentUser);
        if (startDate != null) {
            newStatus.put("startTime", startDate);
        }
        if (endDate != null) {
            newStatus.put("endTime", endDate);
        }
        if (stateName != null) {
            newStatus.put("state", stateName);
        }
        if (cityName != null) {
            newStatus.put("city", cityName);
        }
        if (lat != null) {
            newStatus.put("latitude", lat);
            newStatus.put("longitude", lon);
        }

        newStatus.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {   //TODO show status on home fragment without refreshing query for all statuses
                if (e != null) {
                    //status unsuccessfully added to db
                    Log.e(TAG, "issue when saving post", e);
                    return;
                }
                //status successfully added to db
                Log.i(TAG, "status save was successful!");
                StatusUpdateActivity.this.description.setText("");
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }
}