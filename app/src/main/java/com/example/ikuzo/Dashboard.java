package com.example.ikuzo;

import android.app.appsearch.SearchResult;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Dashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private TextView userInfo;
    private FloatingActionButton fabAddTrip;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView recyclerView;
    private ItineraryAdapter itineraryAdapter;
    private DatabaseReference itinerariesRef;

    private androidx.appcompat.widget.SearchView searchView;
    private RecyclerView searchResultsRecyclerView;
    private SearchResultsAdapter searchResultsAdapter;
    private DatabaseReference usersRef;
    private List<SearchResult> searchResults;

    private TextView noResultsText;

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

        // Initialize no results text view
        noResultsText = findViewById(R.id.no_results_text);

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

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.rv_itineraries);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itineraryAdapter = new ItineraryAdapter(this);
        recyclerView.setAdapter(itineraryAdapter);
        if (currentUser != null) {
            itinerariesRef = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(currentUser.getUid())
                    .child("itineraries");

            loadItineraries();
        }

        // Initialize Firebase references
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");

        // Initialize search-related views
        searchView = findViewById(R.id.search_view);
        searchResultsRecyclerView = findViewById(R.id.search_results_recycler_view);
        searchResults = new ArrayList<>();
        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        if (searchIcon != null) {
            searchIcon.setColorFilter(Color.BLACK);
        }

        // Set search text color
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (searchEditText != null) {
            searchEditText.setTextColor(Color.BLACK);
            searchEditText.setHintTextColor(Color.GRAY);
        }
        // Setup RecyclerView
        searchResultsAdapter = new SearchResultsAdapter(searchResults);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchResultsRecyclerView.setAdapter(searchResultsAdapter);

        // Setup SearchView
        setupSearchView();
    }
    private void loadItineraries() {
        itinerariesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<ItineraryModel> itineraryList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ItineraryModel itinerary = snapshot.getValue(ItineraryModel.class);
                    if (itinerary != null) {
                        itineraryList.add(itinerary);
                    }
                }

                // Sort by creation date (newest first)
                Collections.sort(itineraryList, (i1, i2) ->
                        Long.compare(i2.getCreatedAt(), i1.getCreatedAt()));

                // Update the adapter with the new list
                itineraryAdapter.updateItineraries(itineraryList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Dashboard.this,
                        "Error loading itineraries: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
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
    private void showNoResultsMessage() {
        searchResultsRecyclerView.setVisibility(View.GONE);
        noResultsText.setVisibility(View.VISIBLE);
    }

    private void hideNoResultsMessage() {
        searchResultsRecyclerView.setVisibility(View.VISIBLE);
        noResultsText.setVisibility(View.GONE);
    }

    // Also, let's add method to handle initial state and clearing
    private void resetSearchUI() {
        searchResults.clear();
        searchResultsAdapter.notifyDataSetChanged();
        searchResultsRecyclerView.setVisibility(View.GONE);
        noResultsText.setVisibility(View.GONE);
    }
    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query.toLowerCase().trim());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() >= 2) {
                    performSearch(newText.toLowerCase().trim());
                } else {
                    resetSearchUI();
                }
                return true;
            }
        });

        // Add clear button listener
        searchView.setOnCloseListener(() -> {
            resetSearchUI();
            return false;
        });
    }

    private void performSearch(String query) {
        // Use a Set to ensure uniqueness based on a composite key
        Set<String> uniqueResultKeys = new LinkedHashSet<>();
        searchResults.clear();

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    // Get all required user fields
                    String userId = userSnapshot.getKey();
                    String email = userSnapshot.child("email").getValue(String.class);
                    String username = userSnapshot.child("username").getValue(String.class);

                    // Prefer username, fall back to email
                    String displayName = username != null ? username : (email != null ? email : "Unknown");

                    // Check email/username match
                    if ((email != null && email.toLowerCase().contains(query)) ||
                            (username != null && username.toLowerCase().contains(query))) {
                        String uniqueKey = userId + "_username";
                        if (!uniqueResultKeys.contains(uniqueKey)) {
                            searchResults.add(new Dashboard.SearchResult(
                                    userId,
                                    displayName,
                                    "Email/Username match",
                                    null
                            ));
                            uniqueResultKeys.add(uniqueKey);
                        }
                    }

                    // Check visited locations
                    DataSnapshot locationsSnapshot = userSnapshot.child("visitedLocations");
                    if (locationsSnapshot.exists() && locationsSnapshot.hasChildren()) {
                        for (DataSnapshot locationSnapshot : locationsSnapshot.getChildren()) {
                            String destination = locationSnapshot.child("destination").getValue(String.class);
                            if (destination != null && destination.toLowerCase().contains(query)) {
                                String uniqueKey = userId + "_" + destination;
                                if (!uniqueResultKeys.contains(uniqueKey)) {
                                    searchResults.add(new Dashboard.SearchResult(
                                            userId,
                                            displayName,
                                            "Location match",
                                            destination
                                    ));
                                    uniqueResultKeys.add(uniqueKey);
                                }
                            }
                        }
                    }
                }

                searchResultsAdapter.notifyDataSetChanged();

                // Show/hide no results message
                if (searchResults.isEmpty()) {
                    showNoResultsMessage();
                } else {
                    hideNoResultsMessage();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Dashboard.this, "Search failed: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    private static class SearchResult {
        String userId;
        String displayName;  // Changed from email
        String matchType;
        String matchedLocation;

        SearchResult(String userId, String displayName, String matchType, String matchedLocation) {
            this.userId = userId;
            this.displayName = displayName;
            this.matchType = matchType;
            this.matchedLocation = matchedLocation;
        }
    }
    // Adapter for search results
    private class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {
        private List<Dashboard.SearchResult> results;

        SearchResultsAdapter(List<Dashboard.SearchResult> results) {
            this.results = results;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.search_result_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Dashboard.SearchResult result = results.get(position);
            holder.emailText.setText(result.displayName);  // Use displayName instead of email

            if (result.matchType.equals("Location match")) {
                holder.matchTypeText.setText("Visited: " + result.matchedLocation);
            } else {
                holder.matchTypeText.setText("Email/Username match");
            }

            // Handle click on search result
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(Dashboard.this, Profile.class);
                intent.putExtra("USER_ID", result.userId);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return results.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView emailText;
            TextView matchTypeText;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                emailText = itemView.findViewById(R.id.result_email);
                matchTypeText = itemView.findViewById(R.id.result_match_type);
            }
        }
    }
}