package com.example.ikuzo;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FirebaseItineraryManager {
    private DatabaseReference userRef;
    private String userId;

    public FirebaseItineraryManager(String userId) {
        this.userId = userId;
        this.userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
    }

    public void saveVisitedLocation(String destination) {
        DatabaseReference visitedLocationsRef = userRef.child("visitedLocations");

        Map<String, Object> visitedLocation = new HashMap<>();
        visitedLocation.put("destination", destination);
        visitedLocation.put("timestamp", System.currentTimeMillis());

        String visitId = visitedLocationsRef.push().getKey();
        if (visitId != null) {
            visitedLocationsRef.child(visitId).setValue(visitedLocation);
        }
    }

    public void saveItinerary(String destination,
                              List<List<LatLng>> dailyItineraries,
                              Map<String, String> locationNames,
                              Map<String, String> locationAddresses,
                              String transportMode) {

        DatabaseReference itinerariesRef = userRef.child("itineraries");
        String itineraryId = itinerariesRef.push().getKey();

        if (itineraryId != null) {
            Map<String, Object> itineraryData = new HashMap<>();
            itineraryData.put("destination", destination);
            itineraryData.put("transportMode", transportMode);
            itineraryData.put("createdAt", System.currentTimeMillis());

            // Convert daily itineraries to a saveable format
            List<List<Map<String, Object>>> savableItineraries = new ArrayList<>();
            for (List<LatLng> dayItinerary : dailyItineraries) {
                List<Map<String, Object>> dayLocations = new ArrayList<>();
                for (LatLng location : dayItinerary) {
                    Map<String, Object> locationMap = new HashMap<>();
                    locationMap.put("latitude", location.latitude);
                    locationMap.put("longitude", location.longitude);

                    // Add location details directly to the location object
                    String locationKey = location.latitude + "," + location.longitude;
                    if (locationNames.containsKey(locationKey)) {
                        locationMap.put("name", locationNames.get(locationKey));
                    }
                    if (locationAddresses.containsKey(locationKey)) {
                        locationMap.put("address", locationAddresses.get(locationKey));
                    }

                    dayLocations.add(locationMap);
                }
                savableItineraries.add(dayLocations);
            }
            itineraryData.put("dailyItineraries", savableItineraries);

            // Save the complete itinerary
            itinerariesRef.child(itineraryId).setValue(itineraryData);
        }
    }
}