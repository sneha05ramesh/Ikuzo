package com.example.ikuzo;

import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.PolyUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ItineraryList extends AppCompatActivity implements OnMapReadyCallback {

    private Spinner daySelector;
    private MapView mapView;
    private GoogleMap googleMap;
    private List<List<LatLng>> dailyItineraries;
    private int days = 1; // Example: You can change based on user selection
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    private Spinner transportModeSelector;

    private GeoApiContext geoApiContext;
    private TravelMode currentTravelMode = TravelMode.DRIVING;
    // Add these as class fields in ItineraryList.java
    private LinearLayout locationsContainer;
    private static final int DEFAULT_DURATION_MINUTES = 60; // Default time to spend at each location
    private Map<String, String> locationNames = new HashMap<>(); // To store location names
    private Map<String, String> locationAddresses = new HashMap<>(); // To store location addresses


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itinerary_list);

        // Initialize GeoApiContext with your API key
        geoApiContext = new GeoApiContext.Builder()
                .apiKey("AIzaSyBvhXVJe711TGqgnlMfL8NCXwyuZEQ8eGw")
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS)
                .build();

        // Retrieve selected locations and number of days from intent
        ArrayList<LatLng> locations = getIntent().getParcelableArrayListExtra("LOCATIONS");
        days = getIntent().getIntExtra("DURATION",0);
        Log.d("ItineraryList", "Days: " + days);
        Log.d("ItineraryList", "locations: " + locations);

        if (locations == null || locations.isEmpty() || days <= 0) {
            Toast.makeText(this, "Invalid itinerary data!", Toast.LENGTH_SHORT).show();
            return;
        }
        initializeLocationsList();
        // Split locations into daily itineraries
        dailyItineraries = splitLocationsByDays(locations, days);

        // Initialize Spinner
        setupDaySelector();
        setupTransportModeSelector();

        // Initialize MapView
        mapView = findViewById(R.id.mapView);
        Bundle mapViewBundle = savedInstanceState != null ? savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY) : null;
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    // Add this in onCreate() after setContentView
    private void initializeLocationsList() {
        locationsContainer = findViewById(R.id.locationsContainer);
    }

    // Add this method to fetch location details using Geocoder
    private void fetchLocationDetails(LatLng location, TextView nameView, TextView addressView) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String locationName;

                // Try to get the most specific name for the location
                if (address.getFeatureName() != null && !address.getFeatureName().matches("\\d+")) {
                    locationName = address.getFeatureName();
                } else if (address.getThoroughfare() != null) {
                    locationName = address.getThoroughfare();
                } else {
                    locationName = "";
                }

                // Get full address
                String fullAddress;
                String addressLine = address.getAddressLine(0);
                if (addressLine != null) {
                    fullAddress = addressLine;
                } else {
                    fullAddress = "";
                }

                // Store the details
                String locationKey = location.latitude + "," + location.longitude;
                locationNames.put(locationKey, locationName);
                locationAddresses.put(locationKey, fullAddress);

                // Update UI on main thread
                runOnUiThread(() -> {
                    nameView.setText(locationName);
                    addressView.setText(fullAddress);
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                nameView.setText("Location Name Not Available");
                addressView.setText("Address Not Available");
            });
        }
    }

    // Method to calculate travel time between locations
    private void fetchTravelTime(LatLng origin, LatLng destination, TextView travelInfoView) {
        new Thread(() -> {
            try {
                DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                        .origin(new com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                        .destination(new com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                        .mode(currentTravelMode)
                        .await();

                if (result.routes.length > 0) {
                    DirectionsRoute route = result.routes[0];
                    long durationSeconds = route.legs[0].duration.inSeconds;
                    String travelTime = formatDuration(durationSeconds);

                    runOnUiThread(() -> {
                        travelInfoView.setText(String.format("→ %s by %s",
                                travelTime,
                                currentTravelMode.toString().toLowerCase()));
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    travelInfoView.setText("Travel time unavailable");
                });
            }
        }).start();
    }

    // Helper method to format duration
    private String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }

    private void fetchShortestPathAndDraw(LatLng origin, LatLng destination, GoogleMap map) throws IOException, InterruptedException, ApiException {
        DirectionsApi.newRequest(geoApiContext)
                .origin(new com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                .destination(new com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                .mode(currentTravelMode)
                .setCallback(new PendingResult.Callback<DirectionsResult>() {
                    @Override
                    public void onResult(DirectionsResult result) {
                        if (result != null) {
                            if (result.routes.length > 0)
                            {
                                DirectionsRoute route = result.routes[0]; // Assuming the first route is the shortest
                                List<LatLng> path = PolyUtil.decode(route.overviewPolyline.getEncodedPath());

                                // Create a PolylineOptions object and customize it
                                PolylineOptions polylineOptions = new PolylineOptions()
                                        .addAll(path)
                                        .color(Color.BLUE)
                                        .width(10)
                                        .geodesic(true);

                                // Add the polyline to the map
                                map.addPolyline(polylineOptions);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        // Handle errors, e.g., API rate limits, network issues
                        Log.e("ItineraryList", "Error fetching shortest path", e);
                    }
                });
    }

    private void setupTransportModeSelector() {
        transportModeSelector = findViewById(R.id.transportModeSelector);
        List<String> transportModes = new ArrayList<>();
        transportModes.add("Driving");
        transportModes.add("Walking");
        transportModes.add("Bicycling");
        transportModes.add("Transit");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                transportModes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transportModeSelector.setAdapter(adapter);

        transportModeSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                switch (position) {
                    case 0:
                        currentTravelMode = TravelMode.DRIVING;
                        break;
                    case 1:
                        currentTravelMode = TravelMode.WALKING;
                        break;
                    case 2:
                        currentTravelMode = TravelMode.BICYCLING;
                        break;
                    case 3:
                        currentTravelMode = TravelMode.TRANSIT;
                        break; 
                }
                int selectedDay = daySelector.getSelectedItemPosition() - 1;
                try {
                    updateMapForSelectedDay(selectedDay);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ApiException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }


    private void setupDaySelector() {
        daySelector = findViewById(R.id.daySelector);
        List<String> dayOptions = new ArrayList<>();
        dayOptions.add("All Days");
        for (int i = 1; i <= days; i++) {
            dayOptions.add("Day " + i);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                dayOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySelector.setAdapter(adapter);

        daySelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (googleMap != null) {
                    try {
                        updateMapForSelectedDay(position - 1); // -1 because "All Days" is at position 0
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (ApiException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void updateMapForSelectedDay(int selectedDay) throws IOException, InterruptedException, ApiException {
        googleMap.clear(); // Clear existing markers and polylines

        if (selectedDay == -1) {
            // Show all days
            displayAllDays();
        } else {
            // Show specific day
            displaySpecificDay(selectedDay);
        }
    }

    private void fetchDirectionsAndDrawRoute(List<LatLng> locations, int day) {
        if (locations.size() < 2) return;

        new Thread(() -> {
            try {
                List<com.google.maps.model.LatLng> waypoints = new ArrayList<>();
                for (int i = 1; i < locations.size() - 1; i++) {
                    waypoints.add(new com.google.maps.model.LatLng(
                            locations.get(i).latitude,
                            locations.get(i).longitude
                    ));
                }

                DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                        .origin(new com.google.maps.model.LatLng(
                                locations.get(0).latitude,
                                locations.get(0).longitude))
                        .destination(new com.google.maps.model.LatLng(
                                locations.get(locations.size() - 1).latitude,
                                locations.get(locations.size() - 1).longitude))
                        .waypoints(waypoints.toArray(new com.google.maps.model.LatLng[0]))
                        .mode(currentTravelMode)
                        .await();

                if (result.routes.length > 0) {
                    DirectionsRoute route = result.routes[0];
                    List<LatLng> path = PolyUtil.decode(route.overviewPolyline.getEncodedPath());

                    runOnUiThread(() -> {
                        PolylineOptions polylineOptions = new PolylineOptions()
                                .addAll(path)
                                .clickable(true);

                        switch (currentTravelMode) {
                            case DRIVING:
                                polylineOptions.color(0xFF0000FF); // Blue
                                break;
                            case WALKING:
                                polylineOptions.color(0xFF00FF00); // Green
                                break;
                            case BICYCLING:
                                polylineOptions.color(0xFFFF0000); // Red
                                break;
                            case TRANSIT:
                                polylineOptions.color(0xFFFF00FF); // Purple
                                break;
                        }

                        googleMap.addPolyline(polylineOptions);
                    });
                }
            } catch (Exception e) {
                Log.e("ItineraryList", "Error fetching directions", e);
                runOnUiThread(() -> {
                    Toast.makeText(ItineraryList.this,
                            "Error getting directions: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    // Update the displayAllDays method
    private void displayAllDays() {
        locationsContainer.removeAllViews();

        for (int day = 0; day < dailyItineraries.size(); day++) {
            List<LatLng> dayLocations = dailyItineraries.get(day);
            try {
                displayDayItinerary(dayLocations, day);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }

            // Add day separator
            TextView daySeparator = new TextView(this);
            daySeparator.setText("Day " + (day + 1));
            daySeparator.setTextSize(18);
            daySeparator.setTypeface(null, Typeface.BOLD);
            daySeparator.setPadding(0, 16, 0, 8);
            locationsContainer.addView(daySeparator);

            // Add locations for this day
            for (int i = 0; i < dayLocations.size(); i++) {
                LatLng location = dayLocations.get(i);
                View locationItem = getLayoutInflater().inflate(R.layout.itinerary_location_item, locationsContainer, false);

                TextView titleView = locationItem.findViewById(R.id.locationTitle);
                TextView addressView = locationItem.findViewById(R.id.locationAddress);
                TextView durationView = locationItem.findViewById(R.id.suggestedDuration);
                TextView travelInfoView = locationItem.findViewById(R.id.travelInfo);

                // Set initial loading state
                titleView.setText("Loading...");
                addressView.setText("Fetching address...");
                durationView.setText(String.format("Suggested duration: %d min", DEFAULT_DURATION_MINUTES));

                // Fetch location details
                fetchLocationDetails(location, titleView, addressView);

                // If not the last location in the day, show travel time to next location
                if (i < dayLocations.size() - 1) {
                    LatLng nextLocation = dayLocations.get(i + 1);
                    fetchTravelTime(location, nextLocation, travelInfoView);
                } else {
                    travelInfoView.setVisibility(View.GONE);
                }

                locationsContainer.addView(locationItem);
            }
        }

        // Zoom to first location
        if (!dailyItineraries.isEmpty() && !dailyItineraries.get(0).isEmpty()) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dailyItineraries.get(0).get(0), 12));
        }
    }

    // Update the displaySpecificDay method
    private void displaySpecificDay(int day) throws IOException, InterruptedException, ApiException {
        if (day >= 0 && day < dailyItineraries.size()) {
            List<LatLng> dayLocations = dailyItineraries.get(day);
            displayDayItinerary(dayLocations, day);
            updateLocationsList(dayLocations, day);

            // Zoom to first location of selected day
            if (!dayLocations.isEmpty()) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dayLocations.get(0), 12));
            }
        }
    }

    // Update the updateLocationsList method
    private void updateLocationsList(List<LatLng> locations, int day) {
        locationsContainer.removeAllViews();

        for (int i = 0; i < locations.size(); i++) {
            LatLng location = locations.get(i);
            View locationItem = getLayoutInflater().inflate(R.layout.itinerary_location_item, locationsContainer, false);

            TextView titleView = locationItem.findViewById(R.id.locationTitle);
            TextView addressView = locationItem.findViewById(R.id.locationAddress);
            TextView durationView = locationItem.findViewById(R.id.suggestedDuration);
            TextView travelInfoView = locationItem.findViewById(R.id.travelInfo);

            // Set initial loading state
            titleView.setText("Loading...");
            addressView.setText("Fetching address...");
            durationView.setText(String.format("Suggested duration: %d min", DEFAULT_DURATION_MINUTES));

            // Fetch location details
            fetchLocationDetails(location, titleView, addressView);

            // If not the last location, show travel time to next location
            if (i < locations.size() - 1) {
                LatLng nextLocation = locations.get(i + 1);
                fetchTravelTime(location, nextLocation, travelInfoView);
            } else {
                travelInfoView.setVisibility(View.GONE);
            }

            locationsContainer.addView(locationItem);
        }
    }

    private void displayDayItinerary(List<LatLng> dayLocations, int day) throws IOException, InterruptedException, ApiException {
        if (dayLocations.isEmpty()) return;
        for (int i = 0; i < dayLocations.size() - 1; i++) {
            LatLng origin = dayLocations.get(i);
            LatLng destination = dayLocations.get(i + 1);
            fetchShortestPathAndDraw(origin, destination, googleMap);
        }
        // Add markers for all locations
        for (int i = 0; i < dayLocations.size(); i++) {
            LatLng location = dayLocations.get(i);
            String title = "Day " + (day + 1) + " - Stop " + (i + 1);
            googleMap.addMarker(new MarkerOptions().position(location).title(title));
        }

        // Get directions for the route
        fetchDirectionsAndDrawRoute(dayLocations, day);
    }

    // Method to split locations into daily groups based on the number of days
    private List<List<LatLng>> splitLocationsByDays(List<LatLng> locations, int days) {
        List<List<LatLng>> dailyItineraries = new ArrayList<>();
        int locationsPerDay = locations.size() / days;
        int extra = locations.size() % days;

        int start = 0;
        for (int i = 0; i < days; i++) {
            int end = start + locationsPerDay + (i < extra ? 1 : 0);
            dailyItineraries.add(new ArrayList<>(locations.subList(start, end)));
            start = end;
        }
        return dailyItineraries;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Loop through each day’s itinerary to add markers and polylines
        for (int day = 0; day < dailyItineraries.size(); day++) {
            List<LatLng> dayLocations = dailyItineraries.get(day);

            if (!dayLocations.isEmpty()) {
                // Add a marker for each stop in the day's itinerary
                for (int i = 0; i < dayLocations.size(); i++) {
                    LatLng location = dayLocations.get(i);
                    String title = "Day " + (day + 1) + " - Stop " + (i + 1);
                    googleMap.addMarker(new MarkerOptions().position(location).title(title));
                }

                // Add a polyline to connect all stops for the day
                PolylineOptions polylineOptions = new PolylineOptions()
                        .addAll(dayLocations)
                        .clickable(true);

                googleMap.addPolyline(polylineOptions);

                // Move the camera to the first stop of the first day
                if (day == 0) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dayLocations.get(0), 12));
                }
            }
        }
    }

    // MapView lifecycle methods
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }
}
