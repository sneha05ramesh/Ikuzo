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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Profile extends AppCompatActivity {

    private ImageView profilePicture;
    private TextView profileName, profileTag, profileEmail, profileInterests, profileFoodPreference;
    private Button editAccountButton;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef, preferencesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        profilePicture = findViewById(R.id.profile_picture);
        profileName = findViewById(R.id.profile_name);
        profileTag = findViewById(R.id.profile_tag);
        profileEmail = findViewById(R.id.profile_email);
        profileInterests = findViewById(R.id.profile_interests); // New TextView for Interests
        profileFoodPreference = findViewById(R.id.profile_food_preference); // New TextView for Food Preference
        editAccountButton = findViewById(R.id.edit_account_button);

        mAuth = FirebaseAuth.getInstance();


        // Get the current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = getIntent().getStringExtra("USER_ID"); // Check for user ID

        // If user ID is not null, fetch the corresponding user's details
        if (userId != null) {
            // Fetch details for the user whose profile was clicked
            userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
            loadUserProfile(userId);
        } else if (currentUser != null) {
            // Existing code to handle current user's profile
            userRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());
            loadUserProfile(currentUser.getUid()); // Load current user's profile
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
    // Method to load user profile
    private void loadUserProfile(String userId) {
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId); // Initialize userRef
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check if the user snapshot exists
                if (snapshot.exists()) {
                    // Populate profile details from the snapshot
                    String displayName = snapshot.child("username").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    Boolean makeContactDetailsPublic = snapshot.child("makeContactDetailsPublic").getValue(Boolean.class);

                    // Set user name on Profile
                    profileName.setText(displayName != null ? displayName : "No Name");

                    // Handle email visibility
                    if (makeContactDetailsPublic != null && makeContactDetailsPublic) {
                        profileEmail.setVisibility(View.VISIBLE);
                        profileEmail.setText(email != null ? email : "No Email");
                    } else {
                        profileEmail.setVisibility(View.GONE);
                    }

                    // Reference to preferences node for the user
                    preferencesRef = userRef.child("preferences");

                    // Load user preferences (interests, food preference, etc.)
                    loadPreferences();

                    // Load visited locations
                    loadVisitedLocations();
                } else {
                    Log.d("UserInfo", "User does not exist.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Failed to load user profile: " + error.getMessage());
            }
        });
    }
    private void loadPreferences() {
        preferencesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Retrieve the saved preferences (interests and food preference)
                String interests = snapshot.child("interests").getValue(String.class);
                String foodPreference = snapshot.child("foodPreference").getValue(String.class);

                // Set preferences to the profile UI
                if (interests != null) {
                    profileInterests.setText("Interests: " + interests);
                } else {
                    profileInterests.setText("Interests: Not set");
                }

                if (foodPreference != null) {
                    profileFoodPreference.setText("Food Preference: " + foodPreference);
                } else {
                    profileFoodPreference.setText("Food Preference: Not set");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Failed to load preferences: " + error.getMessage());
                Toast.makeText(Profile.this, "Failed to load preferences", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadVisitedLocations() {
        DatabaseReference visitedLocationsRef = userRef.child("visitedLocations");
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
