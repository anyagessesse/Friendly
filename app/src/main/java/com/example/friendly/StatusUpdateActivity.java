package com.example.friendly;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.friendly.objects.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * activity that can be used to create or update a status
 */
public class StatusUpdateActivity extends AppCompatActivity {
    private static final String TAG = "StatusUpdateActivity";

    private EditText description;
    private Button postStatusButton;
    private Status newStatus;
    private TimePicker timePicker;
    private String stateName = "";
    private String cityName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_update);

        description = findViewById(R.id.text_description);
        postStatusButton = findViewById(R.id.button_post_status);
        timePicker = (TimePicker) findViewById(R.id.time_picker);

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
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                //parses place to save to database
                LatLng latLng = place.getLatLng();
                double MyLat = latLng.latitude;
                double MyLong = latLng.longitude;
                Geocoder geocoder = new Geocoder(StatusUpdateActivity.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(MyLat, MyLong, 1);
                    stateName = addresses.get(0).getAdminArea();
                    cityName = addresses.get(0).getLocality();     //TODO include country or go back to add international places?
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(@NonNull com.google.android.gms.common.api.Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        //adds status to db and navigates back to home fragment
        postStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String descriptionText = description.getText().toString();

                // parse the date to save in database
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                cal.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                Date date = cal.getTime();
                saveStatus(descriptionText, ParseUser.getCurrentUser(), date, stateName, cityName);

                //go to home fragment to display statuses
                Intent intent = new Intent(StatusUpdateActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    //saves a status to the parse database
    private void saveStatus(String description, ParseUser currentUser, Date date, String stateName, String cityName) {
        newStatus = new Status();
        newStatus.setDescription(description);
        newStatus.setUser(currentUser);
        newStatus.put("endTime", date);
        newStatus.put("state",stateName);
        newStatus.put("city",cityName);

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
}