package com.example.ikuzo;

import com.google.android.libraries.places.api.model.Place;

public class PlaceScore {
    Place place;
    int score;

    PlaceScore(Place place, int score) {
        this.place = place;
        this.score = score;
    }
}
