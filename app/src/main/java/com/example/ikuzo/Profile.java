package com.example.ikuzo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Profile extends AppCompatActivity {

    private ImageView profilePicture;
    private TextView profileName, profileTag;
    private Button editAccountButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        profilePicture = findViewById(R.id.profile_picture);
        profileName = findViewById(R.id.profile_name);
        profileTag = findViewById(R.id.profile_tag);
        editAccountButton = findViewById(R.id.edit_account_button);

        mAuth = FirebaseAuth.getInstance();

        // Get the current user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // User is signed in
            String displayName = currentUser.getDisplayName();
            profileName.setText(displayName); // Set user info on Dashboard
        } else {
            // Log if no user is signed in
            Log.d("UserInfo", "No user is currently logged in.");
        }

        // Set default profile data (can be replaced with Firebase data later)
        profileTag.setText("Tag goes here");

        // Edit account button click listener
        editAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, EditProfileActivity.class);
            startActivity(intent);
        });
    }
}
