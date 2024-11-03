package com.example.ikuzo;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ItineraryList extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final String MAP_VIEW_BUNDLE_KEY = "AIzaSyAdh26AFnrQNhrwS8Xx2jJEDf4rt2-NEBQ";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_itinerary_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void generateItineraries() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        ArrayList<LatLng> locations = (ArrayList<LatLng>) getIntent().getSerializableExtra("LOCATIONS");

        if (locations == null || locations.isEmpty()) {
            Log.e("ItineraryList", "Locations data is null or empty");
            Toast.makeText(this, "No locations passed to the itinerary", Toast.LENGTH_SHORT).show();
        }
        for (int i = 0; i < locations.size() - 1; i++) {
            LatLng origin = locations.get(i);
            LatLng destination = locations.get(i + 1);

            String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=" + origin.latitude + "," + origin.longitude +
                    "&destination=" + destination.latitude + "," + destination.longitude +
                    "&alternatives=true&mode=driving&key=AIzaSyAdh26AFnrQNhrwS8Xx2jJEDf4rt2-NEBQ";

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            JSONArray routes = response.getJSONArray("routes");

                            for (int j = 0; j < routes.length(); j++) {
                                JSONObject route = routes.getJSONObject(j);
                                String polyline = route.getJSONObject("overview_polyline").getString("points");
                                List<LatLng> points = PolyUtil.decode(polyline);

                                // Draw the polyline on the map
                                PolylineOptions polylineOptions = new PolylineOptions()
                                        .addAll(points)
                                        .color(Color.BLUE)
                                        .width(10);
                                mMap.addPolyline(polylineOptions);

                                // Display travel time
                                JSONArray legs = route.getJSONArray("legs");
                                JSONObject leg = legs.getJSONObject(0);
                                String duration = leg.getJSONObject("duration").getString("text");

                                LinearLayout itineraryLayout = findViewById(R.id.itinerary_layout);
                                // Create a card or view to display the details
                                View card = LayoutInflater.from(ItineraryList.this)
                                        .inflate(R.layout.itinerary_card, itineraryLayout, false);
                                TextView itineraryDetails = card.findViewById(R.id.itinerary_details);
                                itineraryDetails.setText("Route " + (j + 1) + ": Time taken: " + duration);

                                itineraryLayout.addView(card);
                            }
                        } catch (JSONException e) {
                            Log.e("ItineraryActivity", "JSON parsing error: " + e.getMessage());
                        }
                    }, error -> Log.e("ItineraryActivity", "Error fetching directions: " + error.toString()));

            requestQueue.add(request);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Call generateItineraries() after the map is ready
        generateItineraries();
    }

}