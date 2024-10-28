package com.example.ikuzo;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class Questionnaire_DurationPlace extends AppCompatActivity {

    private TextView dateRange;
    private EditText inputLocation;
    private String startDate, endDate;

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
            // Process the date range and location input
            Toast.makeText(this, "Location: " + location + "\nDates: " + startDate + " - " + endDate, Toast.LENGTH_LONG).show();

            // Move to interests screen
            Intent intent = new Intent(Questionnaire_DurationPlace.this, Questionnaire_Interests.class);
            intent.putExtra("STARTDATE", startDate);
            intent.putExtra("ENDDATE",endDate);
            intent.putExtra("LOCATION", location);
            startActivity(intent);
        });
    }

    private void showDateRangePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog startDatePicker = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            startDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
            dateRange.setText("Start Date: " + startDate);

            DatePickerDialog endDatePicker = new DatePickerDialog(this, (view1, year2, month2, dayOfMonth2) -> {
                endDate = dayOfMonth2 + "/" + (month2 + 1) + "/" + year2;
                dateRange.setText("Trip Dates: " + startDate + " - " + endDate);
            }, year, month, day);

            endDatePicker.setTitle("Select End Date");
            endDatePicker.show();
        }, year, month, day);

        startDatePicker.setTitle("Select Start Date");
        startDatePicker.show();
    }
}