package com.example.ikuzo;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.CircularBounds;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.SearchNearbyRequest;
import com.google.android.libraries.places.api.net.SearchNearbyResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class LocationSelector extends AppCompatActivity {

//    private ListView listView;
    private Button submitButton;
    private PlacesClient placesClient;
    private List<Place> placesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_location_selector);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        listView = findViewById(R.id.linearLayout);
        submitButton = findViewById(R.id.button_submit);

        // Initialize Places API
        Places.initialize(getApplicationContext(), "AIzaSyAdh26AFnrQNhrwS8Xx2jJEDf4rt2-NEBQ");
        placesClient = Places.createClient(this);

        // Get location and interests from intent
        Intent intent = getIntent();
        String location = intent.getStringExtra("LOCATION");
        String interests = intent.getStringExtra("INTERESTS");

        Log.d("LocationSelectionActivity", "Location: " + location);
        Log.d("LocationSelectionActivity", "Interests: " + interests);

        fetchNearbyPlaces(location, interests); // Pass interests to the method

        submitButton.setOnClickListener(v -> {
            // Handle selections and move to the next screen
            // You can retrieve the selected places here
            // For now, let's just show a toast
            Toast.makeText(this, "Selected places submitted!", Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchNearbyPlaces(String location, String interests) {
        // Geocoding: Convert location string to LatLng
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocationName(location, 1);
            if (addresses != null && !addresses.isEmpty()) {
                double latitude = addresses.get(0).getLatitude();
                double longitude = addresses.get(0).getLongitude();
                Toast.makeText(this, "Latitude: " + latitude + ", Longitude: " + longitude, Toast.LENGTH_SHORT).show();
                getNearbyPlaces(latitude, longitude, interests); // Pass interests here
            } else {
                Toast.makeText(this, "Location not found.", Toast.LENGTH_SHORT).show();
                Log.d("LocationSelectionActivity", "Location not found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(LocationSelector.this, "Geocoding failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void getNearbyPlaces(double latitude, double longitude, String interests) {
        List<Place.Field> placeFields = Arrays.asList(
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.TYPES,
                Place.Field.LAT_LNG
        );
        LatLng center = new LatLng(latitude,longitude);
        CircularBounds circle = CircularBounds.newInstance(center, 1000);

        // Create a SearchNearbyRequest object with the provided location
        SearchNearbyRequest request = SearchNearbyRequest.builder(circle, placeFields)
                .build();

        // Use PlacesClient to find nearby places
        placesClient.searchNearby(request)
                    .addOnCompleteListener(new OnCompleteListener<SearchNearbyResponse>() {
                    @Override
                    public void onComplete(Task<SearchNearbyResponse> task) {
                        if (task.isSuccessful()) {
                            placesList.clear(); // Clear previous results
                            for (Place p:
                                 task.getResult().getPlaces()) {
                                placesList.add(p);
                            }
//
//                            // Sort places by score (descending) and limit to top 5
//                            placesList.sort((p1, p2) -> Integer.compare(p2.score, p1.score));
//                            placesList = placesList.stream()
//                                    .limit(5)
//                                    .map(placeScore -> placeScore.place)
//                                    .collect(Collectors.toList());

                            // Update UI with filtered and sorted places
                            updateLocationList(placesList);
                            populateLinearLayout(placesList);
                        } else {
                            Log.e("LocationSelectionActivity", "Error finding nearby places: " + task.getException());
                            Toast.makeText(LocationSelector.this, "Failed to find places nearby.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void updateLocationList(List<Place> placesList) {
        // Implement the logic to update your ListView with the fetched places
        // For example, you can use an ArrayAdapter to display the names of the places
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, getPlaceNames(placesList));
//        listView.setAdapter(adapter);
//        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }
    private List<String> getPlaceNames(List<Place> places) {
        List<String> names = new ArrayList<>();
        for (Place place : places) {
            names.add(place.getName());
        }
        return names;
    }
//    private void redirectToAppSettings() {
//        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//        intent.setData(Uri.parse("package:" + getPackageName()));
//        startActivity(intent);
//        Toast.makeText(this, "Please enable permissions from settings.", Toast.LENGTH_SHORT).show();
//    }
    private void populateLinearLayout(List<Place> placesList) {
        LinearLayout linearLayout = findViewById(R.id.linearLayout);
        linearLayout.removeAllViews(); // Clear previous views

        LayoutInflater inflater = LayoutInflater.from(this);
        List<Place> selectedPlaces = new ArrayList<>();

        for (Place place : placesList) {
            View card = inflater.inflate(R.layout.location_card, linearLayout, false);
            TextView placeName = card.findViewById(R.id.place_name);
            TextView placeLocation = card.findViewById(R.id.place_location);
            CheckBox checkBox = card.findViewById(R.id.place_checkbox);

            placeName.setText(place.getName());
            placeLocation.setText(place.getPlaceTypes() != null ? "" : "Unknown location");
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedPlaces.add(place);
                } else {
                    selectedPlaces.remove(place);
                }
            });
            linearLayout.addView(card);
        }

        Log.d("LocationSelectionActivity", "Cards populated: " + placesList.size());

        // Handle submit button click
        submitButton.setOnClickListener(v -> {
            if (!selectedPlaces.isEmpty()) {
                // Display the names of the selected places or pass the data to the next activity
                String selectedPlacesNames = selectedPlaces.stream()
                        .map(Place::getName)
                        .collect(Collectors.joining(", "));
                Toast.makeText(this, "Selected places: " + selectedPlacesNames, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LocationSelector.this, ItineraryList.class);
                ArrayList<LatLng> latLngList = new ArrayList<>();

                for (Place place : placesList) {
                    LatLng latLng = place.getLatLng(); // Get LatLng from Place
                    if (latLng != null) {
                        latLngList.add(latLng); // Add LatLng to the ArrayList
                    }
                }

                intent.putExtra("LOCATIONS", latLngList); // List of LatLng points
                startActivity(intent);
            } else {
                Toast.makeText(this, "No places selected!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}