package com.example.ikuzo;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Questionnaire_DurationPlace extends AppCompatActivity {

    private TextView dateRange;
    private EditText inputLocation;
    private String startDate, endDate;
    private long durationInMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_questionnaire_duration_place);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dateRange = findViewById(R.id.date_range);
        inputLocation = findViewById(R.id.input_location);

        // Open DatePickerDialog on TextView click
        dateRange.setOnClickListener(v -> showDateRangePicker());
        Button submitBtn = findViewById(R.id.button_submit);
        submitBtn.setOnClickListener(v -> {
            String location = inputLocation.getText().toString();
            Log.d("Questionnaire", "Daterange: " + dateRange);
            // Process the date range and location input
            if (startDate == null || endDate == null) {
                Toast.makeText(this, "Please select a valid start and end date.", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("Questionnaire", "Location: " + location);

            Log.d("Questionnaire", "Start Date: " + startDate);
            Log.d("Questionnaire", "End Date: " + endDate);

            // Calculate the duration in days
            int durationDays = calculateDuration(startDate, endDate);
            Log.d("Questionnaire", "Days: " + durationDays);

            // Move to interests screen
            Intent intent = new Intent(Questionnaire_DurationPlace.this, Questionnaire_Interests.class);
            intent.putExtra("STARTDATE", startDate);
            intent.putExtra("ENDDATE", endDate);
            intent.putExtra("DURATION", durationDays); // Pass the duration
            intent.putExtra("LOCATION", location);
            startActivity(intent);
        });
    }

    private void showDateRangePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Start Date Picker Dialog
        DatePickerDialog startDatePicker = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            startDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
            dateRange.setText("Start Date: " + startDate);


            // After setting the start date, show the End Date Picker Dialog
            DatePickerDialog endDatePicker = new DatePickerDialog(this, (view1, year2, month2, dayOfMonth2) -> {
                endDate = dayOfMonth2 + "/" + (month2 + 1) + "/" + year2;
                dateRange.setText("Trip Dates: " + startDate + " - " + endDate);

                // Log the selected dates to verify they're set correctly
                Log.d("Questionnaire", "Selected Start Date: " + startDate);
                Log.d("Questionnaire", "Selected End Date: " + endDate);

            }, year, month, day);

            // Set the title and show the End Date Picker Dialog
            endDatePicker.setTitle("Select End Date");
            endDatePicker.show();

        }, year, month, day);

        // Set the title and show the Start Date Picker Dialog
        startDatePicker.setTitle("Select Start Date");
        startDatePicker.show();
    }


    // Method to calculate the duration between start and end dates
    private int calculateDuration(String startDate, String endDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);
            long differenceInMillis = end.getTime() - start.getTime();
            durationInMillis = differenceInMillis;

            // Convert milliseconds to days
            long durationInDays = differenceInMillis / (1000 * 60 * 60 * 24);
            return (int) durationInDays;
        } catch (Exception e) {
            Log.e("Questionnaire", "Date parsing error: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

}
