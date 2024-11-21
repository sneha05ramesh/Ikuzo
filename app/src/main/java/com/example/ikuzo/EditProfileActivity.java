package com.example.ikuzo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView editNameIcon, saveNameButton, changePasswordIcon, profilePicture;
    private TextView profileName;
    private EditText editNameField, newPasswordField;
    private LinearLayout changePasswordSection;
    private Button savePasswordButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize views
        profilePicture = findViewById(R.id.profile_picture);
        profileName = findViewById(R.id.profile_name);
        editNameIcon = findViewById(R.id.edit_name_icon);
        //saveNameButton = findViewById(R.id.save_name_button);
        editNameField = findViewById(R.id.edit_name_field);
        changePasswordIcon = findViewById(R.id.change_password_icon);
        changePasswordSection = findViewById(R.id.change_password_section);
        newPasswordField = findViewById(R.id.new_password_field);
        savePasswordButton = findViewById(R.id.save_password_button);

        mAuth = FirebaseAuth.getInstance();

        // Get the current user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Set the profile name from the Firebase current user
            profileName.setText(currentUser.getDisplayName());
        }

        // Show the editable name field when pencil icon is clicked
        editNameIcon.setOnClickListener(v -> {
            editNameField.setVisibility(View.VISIBLE);
            saveNameButton.setVisibility(View.VISIBLE);
        });

        // Save the new name when the save button is clicked
        saveNameButton.setOnClickListener(v -> {
            String newName = editNameField.getText().toString();
            if (!newName.isEmpty()) {
                updateProfileName(newName);
            } else {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        // Toggle the change password section visibility when clicked
        changePasswordIcon.setOnClickListener(v -> {
            if (changePasswordSection.getVisibility() == View.GONE) {
                changePasswordSection.setVisibility(View.VISIBLE);
            } else {
                changePasswordSection.setVisibility(View.GONE);
            }
        });

        // Save the new password
        savePasswordButton.setOnClickListener(v -> {
            String newPassword = newPasswordField.getText().toString();
            if (!newPassword.isEmpty()) {
                updatePassword(newPassword);
            } else {
                Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to update profile name
    private void updateProfileName(String newName) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.updateProfile(new UserProfileChangeRequest.Builder()
                            .setDisplayName(newName)
                            .build())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Update UI with the new name
                            profileName.setText(newName);
                            Toast.makeText(EditProfileActivity.this, "Name updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(EditProfileActivity.this, "Failed to update name", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Method to update password
    private void updatePassword(String newPassword) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditProfileActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(EditProfileActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
