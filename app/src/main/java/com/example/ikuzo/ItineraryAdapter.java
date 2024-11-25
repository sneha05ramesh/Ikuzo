package com.example.ikuzo;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// ItineraryAdapter.java
public class ItineraryAdapter extends RecyclerView.Adapter<ItineraryAdapter.ItineraryViewHolder> {
    private List<ItineraryModel> itineraries;
    private Context context;

    public ItineraryAdapter(Context context) {
        this.context = context;
        this.itineraries = new ArrayList<>();
    }

    @NonNull
    @Override
    public ItineraryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_itinerary, parent, false);
        return new ItineraryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItineraryViewHolder holder, int position) {
        ItineraryModel itinerary = itineraries.get(position);

        holder.destinationText.setText(itinerary.getDestination());
        holder.transportModeText.setText("Transport: " + itinerary.getTransportMode());

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String date = sdf.format(new Date(itinerary.getCreatedAt()));
        holder.createdDateText.setText("Created: " + date);

        // Set up the nested RecyclerView for daily itineraries
        holder.dailyItinerariesRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        DailyLocationsAdapter dailyAdapter = new DailyLocationsAdapter(itinerary.getDailyItineraries());
        holder.dailyItinerariesRecyclerView.setAdapter(dailyAdapter);

        int totalLocations = 0;
        if (itinerary.getDailyItineraries() != null) {
            for (List<Map<String, Object>> day : itinerary.getDailyItineraries()) {
                totalLocations += day.size();
            }
        }
        holder.locationsCountText.setText("Total Locations: " + totalLocations);
    }

    @Override
    public int getItemCount() {
        return itineraries.size();
    }

    public void updateItineraries(List<ItineraryModel> newItineraries) {
        this.itineraries = newItineraries;
        notifyDataSetChanged();
    }

    static class ItineraryViewHolder extends RecyclerView.ViewHolder {
        TextView destinationText;
        TextView transportModeText;
        TextView createdDateText;
        TextView locationsCountText;
        RecyclerView dailyItinerariesRecyclerView;

        public ItineraryViewHolder(@NonNull View itemView) {
            super(itemView);
            destinationText = itemView.findViewById(R.id.tv_destination);
            transportModeText = itemView.findViewById(R.id.tv_transport_mode);
            createdDateText = itemView.findViewById(R.id.tv_created_date);
            locationsCountText = itemView.findViewById(R.id.tv_locations_count);
            dailyItinerariesRecyclerView = itemView.findViewById(R.id.rv_daily_itineraries);
        }

    }

}