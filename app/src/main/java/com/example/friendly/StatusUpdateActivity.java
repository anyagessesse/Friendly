package com.example.friendly;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.friendly.objects.Status;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * activity that can be used to create or update a status
 */
public class StatusUpdateActivity extends AppCompatActivity {
    private static final String TAG = "StatusUpdateActivity";

    private EditText etDescription;
    private Button btnPostStatus;
    private EditText text_timeStart;
    private Status newStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_update);

        etDescription = findViewById(R.id.etDescription);
        btnPostStatus = findViewById(R.id.btnPostStatus);
        text_timeStart = findViewById(R.id.text_timeStart);

        //adds status to db and navigates back to home fragment
        btnPostStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String description = etDescription.getText().toString();

                //parse the date to save in database
                String text = text_timeStart.getText().toString();
                DateFormat formatter = new SimpleDateFormat("hh:mm");
                try {
                    Date date = formatter.parse(text);
                    saveStatus(description, ParseUser.getCurrentUser(),date);
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                    Toast.makeText(StatusUpdateActivity.this, "you entered the time incorrectly :(", Toast.LENGTH_SHORT).show();  //TODO have a better message here or an easier way to enter time
                }

                //go to home fragment to display statuses
                Intent intent = new Intent(StatusUpdateActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    //saves a status to the parse database
    private void saveStatus(String description, ParseUser currentUser, Date date) {
        newStatus = new Status();
        newStatus.setDescription(description);
        newStatus.setUser(currentUser);
        newStatus.put("startTime",date);

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
                etDescription.setText("");
            }
        });
    }
}