package com.example.friendly;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

/**
 * login page activity, allows user to login with a previously created account, or sign up for a new account
 */
public class LoginActivity extends AppCompatActivity {
    public static final String TAG = "LoginActivity";

    private EditText username;
    private EditText password;
    private Button loginButton;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //goes directly to main activity if a user is already logged in
        if (ParseUser.getCurrentUser() != null) {
            goMainActivity();
        }

        username = findViewById(R.id.text_username);
        password = findViewById(R.id.text_password);
        loginButton = findViewById(R.id.button_login);
        signupButton = findViewById(R.id.button_signup);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = LoginActivity.this.username.getText().toString();
                String password = LoginActivity.this.password.getText().toString();
                loginUser(username, password);
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = LoginActivity.this.username.getText().toString();
                String password = LoginActivity.this.password.getText().toString();
                signupUser(username, password);
            }
        });
    }

    private void signupUser(String username, String password) {
        //creates a new ParseUser with given username and password
        final ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    //signup unsuccessful
                    Log.e(TAG, "issue signing up", e);
                    Toast.makeText(LoginActivity.this, "pick a different username or password", Toast.LENGTH_SHORT).show();
                    return;
                }
                //signup successful
                goMainActivity();
            }
        });
    }

    private void loginUser(String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    //login unsuccessful
                    Log.e(TAG, "issue logging in", e);
                    Toast.makeText(LoginActivity.this, "username or password is incorrect", Toast.LENGTH_SHORT).show();
                    return;
                }
                //login successful
                goMainActivity();
            }
        });
    }

    private void goMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}