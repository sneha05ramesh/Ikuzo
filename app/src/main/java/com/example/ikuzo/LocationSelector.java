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
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.CircularBounds;
import com.google.android.libraries.places.api.model.LocationRestriction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.SearchNearbyRequest;
import com.google.android.libraries.places.api.net.SearchNearbyResponse;
import com.google.maps.android.SphericalUtil;
import com.google.maps.model.PlaceType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class LocationSelector extends AppCompatActivity {

    //    private ListView listView;
    private Button submitButton;
    private PlacesClient placesClient;
    private List<Place> placesList = new ArrayList<>();
    private int tripDuration;
    private String location;

    // Map to store interest categories and their corresponding Place types
    private static final Map<String, List<PlaceType>> INTEREST_PLACE_TYPES = new HashMap<>();
    static {
        // Sightseeing
        INTEREST_PLACE_TYPES.put("Sightseeing", Arrays.asList(
                PlaceType.AMUSEMENT_PARK,
                PlaceType.ZOO,
                PlaceType.PARK,
                PlaceType.ESTABLISHMENT
        ));

        // Shopping
        INTEREST_PLACE_TYPES.put("Shopping", Arrays.asList(
                PlaceType.SHOPPING_MALL,
                PlaceType.STORE,
                PlaceType.CLOTHING_STORE,
                PlaceType.JEWELRY_STORE,
                PlaceType.SUPERMARKET
        ));

        // Beach and Water Activities
        INTEREST_PLACE_TYPES.put("Beach and Water Activities", Arrays.asList(
                PlaceType.AMUSEMENT_PARK,
                PlaceType.PARK,             // This can include water parks
                PlaceType.SPA,
                PlaceType.ESTABLISHMENT
        ));

        // Partying
        INTEREST_PLACE_TYPES.put("Partying", Arrays.asList(
                PlaceType.BAR,
                PlaceType.NIGHT_CLUB,
                PlaceType.CASINO,
                PlaceType.RESTAURANT,
                PlaceType.FOOD
        ));

        // Museum and Cultural sites
        INTEREST_PLACE_TYPES.put("Museum and Cultural sites", Arrays.asList(
                PlaceType.MUSEUM,
                PlaceType.ART_GALLERY,
                PlaceType.PLACE_OF_WORSHIP,
                PlaceType.ESTABLISHMENT,
                PlaceType.TOURIST_ATTRACTION
        ));
    }

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
        location = intent.getStringExtra("LOCATION");
        tripDuration = intent.getIntExtra("DURATION", 0);  // Default to 0 if not found
        String interests = intent.getStringExtra("INTERESTS");



        Log.d("LocationSelectionActivity", "Location: " + location);
        Log.d("LocationSelectionActivity", "Interests: " + interests);
        Log.d("LocationSelector", "Days: " + tripDuration);

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
                Place.Field.LAT_LNG,
                Place.Field.RATING,
                Place.Field.USER_RATINGS_TOTAL,
                Place.Field.PHOTO_METADATAS,
                Place.Field.BUSINESS_STATUS
        );

        LatLng center = new LatLng(latitude, longitude);

        // Calculate bounds for a roughly 10km radius search
        double latRadius = 0.1;  // approximately 10km in latitude
        double lngRadius = 0.1;  // approximately 10km in longitude

        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(latitude - latRadius, longitude - lngRadius),  // SW corner
                new LatLng(latitude + latRadius, longitude + lngRadius)   // NE corner
        );

        Set<PlaceType> placeTypes = getPlaceTypesForInterests(interests);

        // Create search queries based on interests and common tourist attractions
        List<String> searchQueries = new ArrayList<>();

        // Add interest-specific queries
        if (interests.contains("Sightseeing")) {
            searchQueries.addAll(Arrays.asList(
                    "tourist attraction",
                    "point of interest",
                    "landmark",
                    "sightseeing"
            ));
        }
        if (interests.contains("Shopping")) {
            searchQueries.addAll(Arrays.asList(
                    "shopping mall",
                    "shopping center",
                    "outlet mall",
                    "market"
            ));
        }
        if (interests.contains("Beach and Water Activities")) {
            searchQueries.addAll(Arrays.asList(
                    "beach",
                    "water park",
                    "aquarium",
                    "marina"
            ));
        }
        if (interests.contains("Partying")) {
            searchQueries.addAll(Arrays.asList(
                    "night club",
                    "bar",
                    "pub",
                    "disco"
            ));
        }
        if (interests.contains("Museum and Cultural sites")) {
            searchQueries.addAll(Arrays.asList(
                    "museum",
                    "art gallery",
                    "cultural center",
                    "historical landmark",
                    "temple",
                    "church",
                    "heritage site"
            ));
        }

        // Always add some general tourist attractions
        searchQueries.addAll(Arrays.asList(
                "famous",
                "popular",
                "attraction",
                "must see"
        ));

        // Create multiple search requests
        List<FindAutocompletePredictionsRequest> requests = new ArrayList<>();
        for (String query : searchQueries) {
            FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                    .setLocationBias(bounds)
                    .setQuery(query)
                    .setOrigin(center)  // Add origin for better relevance
                    .build();
            requests.add(request);
        }

        // Execute all requests and combine results
        Set<String> processedPlaceIds = new HashSet<>(); // To track unique places
        Set<Place> uniquePlaces = new HashSet<>();
        AtomicInteger completedRequests = new AtomicInteger(0);
        AtomicInteger totalPredictions = new AtomicInteger(0);

        for (FindAutocompletePredictionsRequest request : requests) {
            placesClient.findAutocompletePredictions(request)
                    .addOnSuccessListener(response -> {
                        List<AutocompletePrediction> predictions = response.getAutocompletePredictions();
                        totalPredictions.addAndGet(predictions.size());

                        for (AutocompletePrediction prediction : predictions) {
                            String placeId = prediction.getPlaceId();

                            // Skip if we've already processed this place
                            if (!processedPlaceIds.add(placeId)) {
                                continue;
                            }

                            FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(
                                    placeId,
                                    placeFields
                            );

                            placesClient.fetchPlace(placeRequest)
                                    .addOnSuccessListener(placeResponse -> {
                                        Place place = placeResponse.getPlace();
                                        uniquePlaces.add(place);

                                        // Only update UI when we have enough places or processed all requests
                                        if (uniquePlaces.size() >= 20 ||
                                                completedRequests.incrementAndGet() == totalPredictions.get()) {
                                            updateUIWithPlaces(new ArrayList<>(uniquePlaces));
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("LocationSelectionActivity", "Error fetching place: " + e.getMessage());
                                        if (completedRequests.incrementAndGet() == totalPredictions.get()) {
                                            updateUIWithPlaces(new ArrayList<>(uniquePlaces));
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("LocationSelectionActivity", "Error finding nearby places: " + e.getMessage());
                        Toast.makeText(LocationSelector.this,
                                "Failed to find places nearby: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        if (completedRequests.get() == totalPredictions.get()) {
                            updateUIWithPlaces(new ArrayList<>(uniquePlaces));
                        }
                    });
        }
    }

    // Helper method to update the UI with sorted places
    private void updateUIWithPlaces(List<Place> places) {
        places.sort((p1, p2) -> {
            // Sort by rating and number of ratings
            Double rating1 = p1.getRating() != null ? p1.getRating() : 0.0;
            Double rating2 = p2.getRating() != null ? p2.getRating() : 0.0;
            Integer ratings1 = p1.getUserRatingsTotal() != null ? p1.getUserRatingsTotal() : 0;
            Integer ratings2 = p2.getUserRatingsTotal() != null ? p2.getUserRatingsTotal() : 0;

            // Prioritize places with more ratings
            if (ratings1 > 100 && ratings2 > 100) {
                return Double.compare(rating2, rating1);
            } else {
                return ratings2.compareTo(ratings1);
            }
        });

        placesList.clear();
        placesList.addAll(places);
        updateLocationList(placesList);
        populateLinearLayout(placesList);

        Log.d("LocationSelector", "Found " + placesList.size() + " unique places matching interests");
    }

    private Set<PlaceType> getPlaceTypesForInterests(String interests) {
        Set<PlaceType> placeTypes = new HashSet<>();
        if (interests != null && !interests.isEmpty()) {
            String[] interestArray = interests.split(",");
            for (String interest : interestArray) {
                String trimmedInterest = interest.trim();
                if (INTEREST_PLACE_TYPES.containsKey(trimmedInterest)) {
                    placeTypes.addAll(INTEREST_PLACE_TYPES.get(trimmedInterest));
                }
            }
        }
        return placeTypes;
    }


    private void updateLocationList(List<Place> placesList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice,
                getPlaceNames(placesList));
    }
    private List<String> getPlaceNames(List<Place> places) {
        return places.stream()
                .map(Place::getName)
                .collect(Collectors.toList());
    }
    //    private void redirectToAppSettings() {
//        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//        intent.setData(Uri.parse("package:" + getPackageName()));
//        startActivity(intent);
//        Toast.makeText(this, "Please enable permissions from settings.", Toast.LENGTH_SHORT).show();
//    }
    private void populateLinearLayout(List<Place> placesList) {
        LinearLayout linearLayout = findViewById(R.id.linearLayout);
        linearLayout.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);
        List<Place> selectedPlaces = new ArrayList<>();

        for (Place place : placesList) {
            View card = inflater.inflate(R.layout.layout_card, linearLayout, false);
            TextView placeName = card.findViewById(R.id.place_name);
            TextView placeLocation = card.findViewById(R.id.place_location);
            TextView placeRating = card.findViewById(R.id.place_rating);
            CheckBox checkBox = card.findViewById(R.id.place_checkbox);

            // Set place name
            placeName.setText(place.getName());

            // Set rating if available
            if (place.getRating() != null) {
                placeRating.setText(String.format("%.1f (%d)",
                        place.getRating(),
                        place.getUserRatingsTotal() != null ? place.getUserRatingsTotal() : 0));
            } else {
                placeRating.setVisibility(View.GONE);
            }

            // Set place types/info
            String placeInfo = place.getPlaceTypes() != null ?
                    place.getPlaceTypes().stream()
                            .map(type -> type.toString().replace("_", " ").toLowerCase())
                            .collect(Collectors.joining(" • ")) :
                    "Tourist Attraction";
            placeLocation.setText(placeInfo);

            // Handle checkbox selection
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedPlaces.add(place);
                } else {
                    selectedPlaces.remove(place);
                }

                // Update submit button text with selection count
                updateSubmitButtonText(selectedPlaces.size());
            });

            linearLayout.addView(card);
        }

        // Setup submit button
        Button submitButton = findViewById(R.id.button_submit);
        updateSubmitButtonText(0);

        submitButton.setOnClickListener(v -> {
            if (!selectedPlaces.isEmpty()) {
                String selectedPlacesNames = selectedPlaces.stream()
                        .map(Place::getName)
                        .collect(Collectors.joining(", "));

                Intent intent = new Intent(LocationSelector.this, ItineraryList.class);
                ArrayList<LatLng> latLngList = new ArrayList<>();

                for (Place place : selectedPlaces) {
                    LatLng latLng = place.getLatLng();
                    if (latLng != null) {
                        latLngList.add(latLng);
                    }
                }
                intent.putExtra("DESTINATION", location);
                intent.putExtra("LOCATIONS", latLngList);
                intent.putExtra("DURATION", tripDuration);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please select at least one location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSubmitButtonText(int selectedCount) {
        Button submitButton = findViewById(R.id.button_submit);
        if (selectedCount > 0) {
            submitButton.setText(String.format("Create Itinerary (%d selected)", selectedCount));
            submitButton.setEnabled(true);
        } else {
            submitButton.setText("Create Itinerary");
            submitButton.setEnabled(false);
        }
    }
}