package com.example.ikuzo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditProfileActivity extends AppCompatActivity {

    private CheckBox makeContactPublicCheckbox;
    private DatabaseReference userRef;
    private ImageView editUsernameIcon, editPasswordIcon, editemailIcon;
    private LinearLayout editUsernameSection, editPasswordSection, editemailSection;
    private EditText editUsernameField, editPasswordField;
    private Button saveUsernameButton, savePasswordButton, changePictureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        makeContactPublicCheckbox = findViewById(R.id.make_contact_public_checkbox);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

        // Load current value for the checkbox
        userRef.child("makeContactDetailsPublic").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isPublic = snapshot.getValue(Boolean.class);
                makeContactPublicCheckbox.setChecked(isPublic != null && isPublic);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfileActivity.this, "Failed to load settings.", Toast.LENGTH_SHORT).show();
            }
        });

        // Update the value in Firebase when the checkbox is toggled
        makeContactPublicCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userRef.child("makeContactDetailsPublic").setValue(isChecked)
                    .addOnSuccessListener(aVoid -> Toast.makeText(EditProfileActivity.this, "Settings updated.", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Failed to update settings.", Toast.LENGTH_SHORT).show());
        });


        //edit username, password and email
        editUsernameIcon = findViewById(R.id.edit_username_icon);
        editPasswordIcon = findViewById(R.id.edit_password_icon);
        editemailIcon = findViewById(R.id.edit_email_icon);

        editUsernameSection = findViewById(R.id.edit_username_section);
        editPasswordSection = findViewById(R.id.edit_password_section);
        editemailSection = findViewById(R.id.edit_email_section);

        editUsernameField = findViewById(R.id.edit_username_field);
        editPasswordField = findViewById(R.id.edit_password_field);

        saveUsernameButton = findViewById(R.id.save_username_button);
        savePasswordButton = findViewById(R.id.save_password_button);
        changePictureButton = findViewById(R.id.save_email_button);

        editUsernameIcon.setOnClickListener(v -> toggleVisibility(editUsernameSection));

//        saveUsernameButton.setOnClickListener(v -> {
//            String newUsername = editUsernameField.getText().toString();
//            if (!newUsername.isEmpty()) {
//                userRef.child("username").setValue(newUsername);
//                Toast.makeText(this, "Username updated", Toast.LENGTH_SHORT).show();
//            }
//        });

        // Toggle password edit section
        editPasswordIcon.setOnClickListener(v -> toggleVisibility(editPasswordSection));

        // Save password
//        savePasswordButton.setOnClickListener(v -> {
//            String newPassword = editPasswordField.getText().toString();
//            if (!newPassword.isEmpty()) {
//                userRef.child("password").setValue(newPassword);  // Save password
//                Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show();
//            }
//        });

        editemailIcon.setOnClickListener(v -> toggleVisibility(editemailSection));


    }
    private void toggleVisibility(View section) {
        if (section.getVisibility() == View.GONE) {
            section.setVisibility(View.VISIBLE);
        } else {
            section.setVisibility(View.GONE);
        }
    }
}
