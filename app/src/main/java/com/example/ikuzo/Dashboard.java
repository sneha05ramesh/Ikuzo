package com.example.ikuzo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Dashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private TextView userInfo;
    private FloatingActionButton fabAddTrip;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Get the current user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Initialize views
        userInfo = findViewById(R.id.userInfo);
        fabAddTrip = findViewById(R.id.fab_add_trip);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Retrieve the profile name TextView from the header
        View headerView = navigationView.getHeaderView(0); // 0 refers to the first header view
        TextView profileNameTextView = headerView.findViewById(R.id.nav_user_name);

        // Set up the Navigation Drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        // Floating Action Button - Navigate to Questionnaire
        fabAddTrip.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, Questionnaire_DurationPlace.class);
            startActivity(intent);
        });

        // Profile Icon click listener - Navigate to ProfileActivity
        ImageView profileIcon = headerView.findViewById(R.id.profile_icon);
        profileIcon.setOnClickListener(v -> {
            // Navigate to ProfileActivity when profile icon is clicked
            Intent intent = new Intent(Dashboard.this, Profile.class);
            startActivity(intent);
        });

        // If user is signed in, display the user's name in the Navigation Drawer header
        if (currentUser != null) {
            // User is signed in
            String displayName = currentUser.getDisplayName();
            userInfo.setText(displayName); // Set user info on Dashboard
            profileNameTextView.setText(displayName); // Set the name in the Navigation Drawer header
        } else {
            // Log if no user is signed in
            Log.d("UserInfo", "No user is currently logged in.");
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            // Log out the user and navigate to Login activity
            mAuth.signOut();
            Intent intent = new Intent(Dashboard.this, Login.class);
            startActivity(intent);
            finish();
        }

        // Close the drawer after an item is selected
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        // Close the drawer if it's open; otherwise, navigate normally
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
