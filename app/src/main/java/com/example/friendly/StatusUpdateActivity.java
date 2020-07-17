package com.example.friendly;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.friendly.objects.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * activity that can be used to create or update a status
 */
public class StatusUpdateActivity extends AppCompatActivity {
    private static final String TAG = "StatusUpdateActivity";

    private EditText description;
    private Button postStatusButton;
    private Status newStatus;
    private TimePicker timePicker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_update);

        description = findViewById(R.id.text_description);
        postStatusButton = findViewById(R.id.button_post_status);
        timePicker = (TimePicker) findViewById(R.id.time_picker);

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
                saveStatus(descriptionText, ParseUser.getCurrentUser(), date);

                //go to home fragment to display statuses
                Intent intent = new Intent(StatusUpdateActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //user can search for a location to set in their status
        //initializes google places api key
        String apiKey = getString(R.string.google_api_key);
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }
        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
            }

            @Override
            public void onError(@NonNull com.google.android.gms.common.api.Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    //saves a status to the parse database
    private void saveStatus(String description, ParseUser currentUser, Date date) {
        newStatus = new Status();
        newStatus.setDescription(description);
        newStatus.setUser(currentUser);
        newStatus.put("endTime", date);

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