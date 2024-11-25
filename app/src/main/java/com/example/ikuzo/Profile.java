package com.example.ikuzo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Profile extends AppCompatActivity {

    private ImageView profilePicture;
    private TextView profileName, profileTag, profileEmail; // Added profileEmail
    private Button editAccountButton;
    private FirebaseAuth mAuth;
    private DatabaseReference visitedLocationsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        profilePicture = findViewById(R.id.profile_picture);
        profileName = findViewById(R.id.profile_name);
        profileTag = findViewById(R.id.profile_tag);
        profileEmail = findViewById(R.id.profile_email); // Initialize email TextView
        editAccountButton = findViewById(R.id.edit_account_button);

        mAuth = FirebaseAuth.getInstance();

        // Get the current user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // User is signed in
            String displayName = currentUser.getDisplayName();
            profileName.setText(displayName); // Set user name on Profile

            // Firebase reference for user data
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(currentUser.getUid());

            // Fetch user settings
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Get the makeContactDetailsPublic value
                    Boolean makeContactDetailsPublic = snapshot.child("makeContactDetailsPublic").getValue(Boolean.class);
                    String email = currentUser.getEmail();

                    if (makeContactDetailsPublic != null && makeContactDetailsPublic) {
                        // Show email if the setting is enabled
                        profileEmail.setVisibility(View.VISIBLE);
                        profileEmail.setText(email);
                    } else {
                        // Hide email if the setting is disabled
                        profileEmail.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FirebaseError", "Failed to load user settings: " + error.getMessage());
                }
            });

            // Firebase reference for visited locations
            visitedLocationsRef = userRef.child("visitedLocations");

            // Load visited locations
            loadVisitedLocations();
        } else {
            // Log if no user is signed in
            Log.d("UserInfo", "No user is currently logged in.");
        }

        // Set default profile data (can be replaced with Firebase data later)
        profileTag.setText("Nature lover");

        // Edit account button click listener
        editAccountButton.setOnClickListener(v -> {
            // Launch EditProfileActivity
            Intent intent = new Intent(Profile.this, EditProfileActivity.class);
            startActivity(intent);
        });
    }


    // Method to load visited locations from Firebase
    private void loadVisitedLocations() {
        visitedLocationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> visitedLocationsList = new ArrayList<>();

                // Retrieve the visited locations from Firebase
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String destination = snapshot.child("destination").getValue(String.class);
                    if (destination != null) {
                        visitedLocationsList.add(destination);
                    }
                }

                updateVisitedLocationsUI(visitedLocationsList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Failed to load visited locations: " + databaseError.getMessage());
                Toast.makeText(Profile.this, "Failed to load visited locations", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to update the UI with visited locations
    private void updateVisitedLocationsUI(List<String> visitedLocationsList) {
        LinearLayout visitedLocationsLayout = findViewById(R.id.visited_locations_layout);
        visitedLocationsLayout.removeAllViews(); // Clear any previous views

        // Check if there are no visited locations
        if (visitedLocationsList.isEmpty()) {
            TextView emptyMessage = new TextView(this);
            emptyMessage.setText("No visited locations yet.");
            emptyMessage.setGravity(Gravity.CENTER);
            visitedLocationsLayout.addView(emptyMessage);
        } else {
            // Loop through the visited locations list and add each location as a TextView
            for (String location : visitedLocationsList) {
                // Create a TextView for each location
                TextView locationView = new TextView(this);
                locationView.setText(location);
                locationView.setTextSize(16);
                locationView.setTextColor(getResources().getColor(android.R.color.black)); // Set text color
                locationView.setPadding(20, 10, 20, 10); // Add some padding for spacing

                // Optionally, set an icon (you can create an appropriate icon in drawable)
                locationView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_location, 0, 0, 0); // Add location icon
                locationView.setCompoundDrawablePadding(16); // Add padding between icon and text

                // Add the TextView to the layout
                visitedLocationsLayout.addView(locationView);
            }
        }
    }
}
