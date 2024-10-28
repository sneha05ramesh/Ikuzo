package com.example.ikuzo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Dashboard extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private TextView userInfo;
    private FloatingActionButton fabAddTrip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Get the current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // Initialize views
        userInfo = findViewById(R.id.userInfo);
        fabAddTrip = findViewById(R.id.fab_add_trip);
        // Floating Action Button - Navigate to Questionnaire
        fabAddTrip.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, Questionnaire_DurationPlace.class);
            startActivity(intent);
        });

        if (currentUser != null) {
            // User is signed in
            String userId = currentUser.getUid(); // User ID
            String email = currentUser.getEmail(); // Email
            String displayName = currentUser.getDisplayName(); // Display Name (if set)
            // Use this info as needed
            Log.d("UserInfo", "User ID: " + userId);
            Log.d("UserInfo", "Email: " + email);
            Log.d("UserInfo", "Display Name: " + displayName);
            userInfo.setText(displayName);
        } else {
            // No user is signed in
            Log.d("UserInfo", "No user is currently logged in.");
        }
    }
}