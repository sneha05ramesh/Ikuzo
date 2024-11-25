package com.example.ikuzo;

import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
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
    private EditText editUsernameField, editPasswordField, editEmailField;
    private Button saveUsernameButton, savePasswordButton, saveEmailButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        makeContactPublicCheckbox = findViewById(R.id.make_contact_public_checkbox);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();
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

        // Initialize views for username, email, and password editing
        editUsernameIcon = findViewById(R.id.edit_username_icon);
        editPasswordIcon = findViewById(R.id.edit_password_icon);
        editemailIcon = findViewById(R.id.edit_email_icon);

        editUsernameSection = findViewById(R.id.edit_username_section);
        editPasswordSection = findViewById(R.id.edit_password_section);
        editemailSection = findViewById(R.id.edit_email_section);

        editUsernameField = findViewById(R.id.edit_username_field);
        editPasswordField = findViewById(R.id.edit_password_field);
        editEmailField = findViewById(R.id.edit_email_field);

        saveUsernameButton = findViewById(R.id.save_username_button);
        savePasswordButton = findViewById(R.id.save_password_button);
        saveEmailButton = findViewById(R.id.save_email_button);

        // Toggle visibility of sections
        editUsernameIcon.setOnClickListener(v -> toggleVisibility(editUsernameSection));
        editPasswordIcon.setOnClickListener(v -> toggleVisibility(editPasswordSection));
        editemailIcon.setOnClickListener(v -> toggleVisibility(editemailSection));

        // Handle saving username
        saveUsernameButton.setOnClickListener(v -> {
            String newUsername = editUsernameField.getText().toString().trim();
            if (!newUsername.isEmpty()) {
                updateUsername(newUsername);
            } else {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle saving email
        saveEmailButton.setOnClickListener(v -> {
            String newEmail = editEmailField.getText().toString().trim();
            if (!newEmail.isEmpty()) {
                updateEmail(newEmail);
            } else {
                Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle saving password
        savePasswordButton.setOnClickListener(v -> {
            String newPassword = editPasswordField.getText().toString().trim();
            if (!newPassword.isEmpty() && newPassword.length() >= 6) {
                updatePassword(newPassword);
            } else {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleVisibility(View section) {
        if (section.getVisibility() == View.GONE) {
            section.setVisibility(View.VISIBLE);
        } else {
            section.setVisibility(View.GONE);
        }
    }

    // Method to update username
    private void updateUsername(String newUsername) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUser.updateProfile(new UserProfileChangeRequest.Builder()
                            .setDisplayName(newUsername)
                            .build())
                    .addOnSuccessListener(aVoid -> {
                        userRef.child("username").setValue(newUsername);
                        Toast.makeText(EditProfileActivity.this, "Username updated successfully.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditProfileActivity.this, "Failed to update username.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Method to update email
    private void updateEmail(String newEmail) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUser.updateEmail(newEmail)
                    .addOnSuccessListener(aVoid -> {
                        userRef.child("email").setValue(newEmail);
                        Toast.makeText(EditProfileActivity.this, "Email updated successfully.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        String errorMessage = "Failed to update email. " + e.getMessage();
                        Log.e("EditProfile", errorMessage); // Log the error
                        Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Method to update password
    private void updatePassword(String newPassword) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUser.updatePassword(newPassword)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(EditProfileActivity.this, "Password updated successfully.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        String errorMessage = "Failed to update password. " + e.getMessage();
                        Log.e("EditProfile", errorMessage); // Log the error
                        Toast.makeText(EditProfileActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
