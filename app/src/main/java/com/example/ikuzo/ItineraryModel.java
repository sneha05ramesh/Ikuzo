package com.example.ikuzo;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ItineraryModel implements Parcelable {
    private String destination;
    private String transportMode;
    private long createdAt;
    private List<List<Map<String, Object>>> dailyItineraries;
    // Default constructor for Firebase
    public ItineraryModel() {}

    private Map<String, String> locationNames;
    private Map<String, String> locationAddresses;

    public Map<String, String> getLocationNames() { return locationNames; }
    public Map<String, String> getLocationAddresses() { return locationAddresses; }


    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getTransportMode() { return transportMode; }
    public void setTransportMode(String transportMode) { this.transportMode = transportMode; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public List<List<Map<String, Object>>> getDailyItineraries() { return dailyItineraries; }
    public void setDailyItineraries(List<List<Map<String, Object>>> dailyItineraries) {
        this.dailyItineraries = dailyItineraries;
    }

    // Parcelable implementation
    protected ItineraryModel(Parcel in) {
        destination = in.readString();
        transportMode = in.readString();
        createdAt = in.readLong();
        // Note: You'll need to implement proper reading of dailyItineraries
        // This is a simplified version
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(destination);
        dest.writeString(transportMode);
        dest.writeLong(createdAt);
        // Note: You'll need to implement proper writing of dailyItineraries
        // This is a simplified version
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ItineraryModel> CREATOR = new Creator<ItineraryModel>() {
        @Override
        public ItineraryModel createFromParcel(Parcel in) {
            return new ItineraryModel(in);
        }

        @Override
        public ItineraryModel[] newArray(int size) {
            return new ItineraryModel[size];
        }
    };
}
