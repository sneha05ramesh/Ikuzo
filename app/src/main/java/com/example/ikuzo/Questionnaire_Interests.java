package com.example.ikuzo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Questionnaire_Interests extends AppCompatActivity {

    private CheckBox checkSightseeing, checkShopping, checkBeach, checkPartying, checkMuseums;
    private RadioGroup radioGroupFood, radioGroupTransport;
    private Button submitButton;

    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_questionnaire_interests);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child("user_id").child("preferences");

        // Initialize UI elements
        checkSightseeing = findViewById(R.id.check_sightseeing);
        checkShopping = findViewById(R.id.check_shopping);
        checkBeach = findViewById(R.id.check_beach);
        checkPartying = findViewById(R.id.check_partying);
        checkMuseums = findViewById(R.id.check_museums);

        radioGroupFood = findViewById(R.id.radioGroup_food);
        radioGroupTransport = findViewById(R.id.radioGroup_transport);

        submitButton = findViewById(R.id.button_submit);

        // Submit button click listener
        submitButton.setOnClickListener(v -> savePreferences());
    }
    private void savePreferences() {
        // Collect interests
        Map<String, Boolean> interests = new HashMap<>();
        interests.put("Sightseeing", checkSightseeing.isChecked());
        interests.put("Shopping", checkShopping.isChecked());
        interests.put("Beach", checkBeach.isChecked());
        interests.put("Partying", checkPartying.isChecked());
        interests.put("Museums", checkMuseums.isChecked());

        StringBuilder selectedInterests = new StringBuilder();
        for (Map.Entry<String, Boolean> entry : interests.entrySet()) {
            if (entry.getValue()) {  // Check if the interest is selected
                if (selectedInterests.length() > 0) {
                    selectedInterests.append(", ");  // Add a comma if there are already selected interests
                }
                selectedInterests.append(entry.getKey());  // Append the selected interest
            }
        }

        // Convert to string
        String selectedInterestsString = selectedInterests.toString();

        // Collect food and transport preferences
        int selectedFoodId = radioGroupFood.getCheckedRadioButtonId();
        String foodPreference = selectedFoodId != -1 ? ((RadioButton) findViewById(selectedFoodId)).getText().toString() : "No preference";

        int selectedTransportId = radioGroupTransport.getCheckedRadioButtonId();
        String transportPreference = selectedTransportId != -1 ? ((RadioButton) findViewById(selectedTransportId)).getText().toString() : "No preference";

        // Get data from Intent
        Intent intent = getIntent();
        String dateStart = intent.getStringExtra("STARTDATE");
        String dateEnd = intent.getStringExtra("ENDDATE");
        String location = intent.getStringExtra("LOCATION");

        // Create intent for next activity
        Intent locationIntent = new Intent(Questionnaire_Interests.this, LocationSelectionActivity.class);
        locationIntent.putExtra("LOCATION", location);
        locationIntent.putExtra("INTERESTS", selectedInterestsString);
        locationIntent.putExtra("FOOD_PREFERENCE", foodPreference);
        locationIntent.putExtra("TRANSPORT_PREFERENCE", transportPreference);
        startActivity(locationIntent);
    }



}