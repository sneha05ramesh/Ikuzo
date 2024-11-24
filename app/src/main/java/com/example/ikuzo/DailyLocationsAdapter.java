package com.example.ikuzo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class DailyLocationsAdapter extends RecyclerView.Adapter<DailyLocationsAdapter.LocationViewHolder> {
    private List<List<Map<String, Object>>> dailyItineraries;

    public DailyLocationsAdapter(List<List<Map<String, Object>>> dailyItineraries) {
        this.dailyItineraries = dailyItineraries;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_location, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        int dayIndex = 0;
        int locationIndex = position;

        // Find which day this position belongs to
        for (List<Map<String, Object>> day : dailyItineraries) {
            if (locationIndex >= day.size()) {
                locationIndex -= day.size();
                dayIndex++;
            } else {
                break;
            }
        }

        Map<String, Object> location = dailyItineraries.get(dayIndex).get(locationIndex);

        // Show day header for first location of each day
        if (locationIndex == 0) {
            holder.dayHeader.setVisibility(View.VISIBLE);
            holder.dayHeader.setText("Day " + (dayIndex + 1));
        } else {
            holder.dayHeader.setVisibility(View.GONE);
        }

        holder.locationName.setText((String) location.get("name"));
        holder.locationAddress.setText((String) location.get("address"));

        double lat = (double) location.get("latitude");
        double lng = (double) location.get("longitude");
        holder.locationCoordinates.setText(String.format("(%.6f, %.6f)", lat, lng));
    }

    @Override
    public int getItemCount() {
        int total = 0;
        if (dailyItineraries != null) {
            for (List<Map<String, Object>> day : dailyItineraries) {
                total += day.size();
            }
        }
        return total;
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView dayHeader;
        TextView locationName;
        TextView locationAddress;
        TextView locationCoordinates;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            dayHeader = itemView.findViewById(R.id.tv_day_header);
            locationName = itemView.findViewById(R.id.tv_location_name);
            locationAddress = itemView.findViewById(R.id.tv_location_address);
            locationCoordinates = itemView.findViewById(R.id.tv_location_coordinates);
        }
    }
}
