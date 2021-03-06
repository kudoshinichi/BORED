package com.projectbored.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class CreateAccount extends AppCompatActivity {
    public static final String PREFS_NAME = "UserDetails";

    public boolean loggedIn;
    private DatabaseReference mDataRef;

    private EditText usernameField;
    private EditText emailField;
    private EditText passwordField;
    private TextView emptyFieldText;

    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mDataRef = FirebaseDatabase.getInstance().getReference();

        usernameField = (EditText)findViewById(R.id.signUpUsername);
        emailField = (EditText)findViewById(R.id.signUpEmail);
        passwordField = (EditText)findViewById(R.id.signUpPassword);
        emptyFieldText = (TextView) findViewById(R.id.signUpEmptyFieldAlert);

        signUpButton = (Button)findViewById(R.id.signup_button);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent returnToMap = new Intent(this, MapsActivityCurrentPlace.class);
        returnToMap.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(returnToMap);
    }

    private void signUp() {
        if(usernameField.getText().toString().trim().isEmpty() || emailField.getText().toString().trim().isEmpty() || passwordField.getText().toString().trim().isEmpty())
        {
            emptyFieldText.setText(R.string.error_field_required);
        } else {
            final String username = usernameField.getText().toString();
            final String email = emailField.getText().toString();
            final String password = passwordField.getText().toString();

            mDataRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(username).exists()) {
                        emptyFieldText.setText(R.string.error_existing_username);
                    } else {
                        addUser(username, email, password);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        loggedIn = true;
    }

    private void addUser(String username, String email, String password) {
        User user = new User(username, email, password);
        Map<String, Object> userDetails = user.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/users/" + username, userDetails);
        mDataRef.updateChildren(childUpdates);

        storeLocalUserData(username, password);

        Intent i = new Intent(this, MapsActivityCurrentPlace.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    private void storeLocalUserData(String username, String password) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("Logged in", true);
        editor.putString("Username", username);
        editor.putString("Password", password);

        editor.apply();
    }
}
