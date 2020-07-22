package com.example.friendly.services;

import android.app.Application;

import com.example.friendly.objects.Status;
import com.parse.Parse;
import com.parse.ParseObject;

/**
 * attaches the Parse database to the app
 */
public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //register parse models
        ParseObject.registerSubclass(Status.class);

        // set applicationId, and server server based on the values in the Heroku settings.
        // clientKey is not needed unless explicitly configured
        // any network interceptors must be added with the Configuration Builder given this syntax
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("anya-friendly") // should correspond to APP_ID env variable
                .clientKey("FBU2020Friendly")  // set explicitly unless clientKey is explicitly configured on Parse server
                .server("https://anya-friendly.herokuapp.com/parse/").build());
    }
}
