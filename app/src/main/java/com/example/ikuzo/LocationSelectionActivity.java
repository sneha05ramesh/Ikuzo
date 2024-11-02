package com.example.ikuzo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocationSelectionActivity extends AppCompatActivity {

    private ListView listView;
    private Button submitButton;
    private PlacesClient placesClient;
    private List<Place> placesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_selection);

        listView = findViewById(R.id.listView);
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
                Log.d("LocationSelectionActivity", "Latitude: " + latitude + ", Longitude: " + longitude);
                getNearbyPlaces(latitude, longitude, interests); // Pass interests here
            } else {
                Toast.makeText(this, "Location not found.", Toast.LENGTH_SHORT).show();
                Log.d("LocationSelectionActivity", "Location not found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Geocoding failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void getNearbyPlaces(double latitude, double longitude, String interests) {
        // Define a place filter based on interests
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG);
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        placesClient.findCurrentPlace(request).addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
            @Override
            public void onComplete(Task<FindCurrentPlaceResponse> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    FindCurrentPlaceResponse response = task.getResult();
                    placesList.clear();
                    Log.d("LocationSelectionActivity", "Fetched places: " + response.getPlaceLikelihoods().size());
                    for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                        // Here is where you filter based on interests
                        Log.d("LocationSelectionActivity", "Place: " + placeLikelihood.getPlace().getName());
                        if (shouldIncludePlace(placeLikelihood.getPlace(), interests)) {
                            placesList.add(placeLikelihood.getPlace());
                        }
                    }
                    // Limit to three locations
                    if (placesList.size() > 3) {
                        placesList = placesList.subList(0, 3);
                    }
                    updateLocationList(placesList);
                } else {
                    Toast.makeText(LocationSelectionActivity.this, "Place request failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("LocationSelectionActivity", "Place request failed: " + task.getException().getMessage());
                }
            }
        });
    }

    private void updateLocationList(List<Place> places) {
        // Implement the logic to update your ListView with the fetched places
        // For example, you can use an ArrayAdapter to display the names of the places
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, getPlaceNames(places));
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    private List<String> getPlaceNames(List<Place> places) {
        List<String> names = new ArrayList<>();
        for (Place place : places) {
            names.add(place.getName());
        }
        return names;
    }

    // Method to decide if the place should be included based on interests
    private boolean shouldIncludePlace(Place place, String interests) {
        // Example filtering logic based on interests
        List<String> interestList = Arrays.asList(interests.split(", "));
        for (String interest : interestList) {
            if (interest.equalsIgnoreCase("Sightseeing") && place.getTypes().contains(Place.Type.TOURIST_ATTRACTION)) {
                return true;
            }
            if (interest.equalsIgnoreCase("Shopping") && place.getTypes().contains(Place.Type.SHOPPING_MALL)) {
                return true;
            }
            if (interest.equalsIgnoreCase("Beach") && place.getTypes().contains(Place.Type.PARK)) {
                return true;
            }
            if (interest.equalsIgnoreCase("Partying") && place.getTypes().contains(Place.Type.NIGHT_CLUB)) {
                return true;
            }
            if (interest.equalsIgnoreCase("Museums") && place.getTypes().contains(Place.Type.MUSEUM)) {
                return true;
            }
        }
        Log.d("LocationSelectionActivity", "Checking place: " + place.getName() + " against interests: " + interests);
        return false;
    }
}
