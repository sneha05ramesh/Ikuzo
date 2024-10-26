package com.example.ikuzo;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {

    private EditText editTextRegisterFullName, editTextRegisterEmail, editTextRegisterPhoneNumber, editTextRegisterDoB, editTextRegisterPassword;
    private ProgressBar progressBar;
    private RadioGroup radioGroupRegisterGender;
    private RadioButton radioButtonRegisterGenderSelected;
    private DatePickerDialog picker;
    private static final String TAG= "Register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toast.makeText(Register.this, "You can register now!", Toast.LENGTH_LONG).show();

        // Edit Text
        editTextRegisterFullName = findViewById(R.id.et_register_full_name);
        editTextRegisterEmail = findViewById(R.id.et_register_email);
        editTextRegisterDoB = findViewById(R.id.et_register_dob);
        editTextRegisterPhoneNumber = findViewById(R.id.et_phonenum);
        editTextRegisterPassword = findViewById(R.id.et_password);

        // Radio Group
        radioGroupRegisterGender = findViewById(R.id.rg_register_gender);
        radioGroupRegisterGender.clearCheck();

        // edit DoB
        editTextRegisterDoB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);


                // Date Picker dialog

                picker = new DatePickerDialog(Register.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        editTextRegisterDoB.setText(dayOfMonth+ "/" + (month +1) + "/" + year);
                    }
                }, year, month, day);
                picker.show();
            }
        });


        Button buttonRegister = findViewById(R.id.register_button);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedGenderId = radioGroupRegisterGender.getCheckedRadioButtonId();
                radioButtonRegisterGenderSelected = findViewById(selectedGenderId);

                //Obtain data
                String textFullName = editTextRegisterFullName.getText().toString();
                String textEmail = editTextRegisterEmail.getText().toString();
                String textDoB = editTextRegisterDoB.getText().toString();
                String textMobile = editTextRegisterPhoneNumber.getText().toString();
                String textPwD = editTextRegisterPassword.getText().toString();
                String textGender; // Can't obtain the value before verifying if button was selected or not;

                // Validate Mobile Number
                String mobileRegex = "[0-9]{10}";
                Matcher mobileMatcher;
                Pattern mobilePattern = Pattern.compile(mobileRegex);
                mobileMatcher = mobilePattern.matcher(textMobile);

                if(TextUtils.isEmpty(textFullName)){
                    Toast.makeText(Register.this, "Please Enter Full Name", Toast.LENGTH_SHORT).show();
                    editTextRegisterFullName.setError("Full Name is required!");
                    editTextRegisterFullName.requestFocus();
                } else if(TextUtils.isEmpty(textFullName)){
                    Toast.makeText(Register.this, "Please Enter Email", Toast.LENGTH_SHORT).show();
                    editTextRegisterEmail.setError("Email is required!");
                    editTextRegisterEmail.requestFocus();
                } else if(!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()){
                    Toast.makeText(Register.this, "Please re-enter Email", Toast.LENGTH_SHORT).show();
                    editTextRegisterEmail.setError("Valid Email is required!");
                    editTextRegisterEmail.requestFocus();
                } else if(TextUtils.isEmpty(textDoB)){
                    Toast.makeText(Register.this, "Please enter your DOB", Toast.LENGTH_SHORT).show();
                    editTextRegisterDoB.setError("Date of Birth is required!");
                    editTextRegisterDoB.requestFocus();
                } else if(radioGroupRegisterGender.getCheckedRadioButtonId() == -1){
                    Toast.makeText(Register.this, "Please select your gender", Toast.LENGTH_SHORT).show();
                    radioButtonRegisterGenderSelected.setError("Gender is required!");
                    radioButtonRegisterGenderSelected.requestFocus();
                } else if(TextUtils.isEmpty(textMobile)){
                    Toast.makeText(Register.this, "Please enter mobile number", Toast.LENGTH_SHORT).show();
                    editTextRegisterPhoneNumber.setError("Mobile number is required");
                    editTextRegisterPhoneNumber.requestFocus();
                } else if(textMobile.length() != 10){
                    Toast.makeText(Register.this, "Mobile number should be valid", Toast.LENGTH_SHORT).show();
                    editTextRegisterPhoneNumber.setError("Mobile number should be 10 digits!");
                    editTextRegisterPhoneNumber.requestFocus();
                } else if(!mobileMatcher.find()){
                    Toast.makeText(Register.this, "Please re-enter your mobile number", Toast.LENGTH_SHORT).show();
                    editTextRegisterPhoneNumber.setError("Mobile number should be 10 digits!");
                    editTextRegisterPhoneNumber.requestFocus();
                } else if(TextUtils.isEmpty(textPwD)){
                    Toast.makeText(Register.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                    editTextRegisterPassword.setError("Password is reqd");
                    editTextRegisterPassword.requestFocus();
                } else if(textPwD.length()<6){
                    Toast.makeText(Register.this, "Password must be at least 6 digits", Toast.LENGTH_SHORT).show();
                    editTextRegisterPassword.setError("Password too weak");
                    editTextRegisterPassword.requestFocus();
                } else{
                    textGender = radioButtonRegisterGenderSelected.getText().toString();
                    registerUser(textFullName, textEmail, textDoB, textGender, textMobile, textPwD);
                }

            }
        });
    }

    // Register User using the creds given
    private void registerUser(String textFullName, String textEmail, String textDoB, String textGender, String textMobile, String textPwD) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(textEmail,textPwD).addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(Register.this, "User Registered Successfully", Toast.LENGTH_SHORT).show();
                    FirebaseUser firebaseUser = auth.getCurrentUser();

                    // Update Display Name of User
                    

                    // Enter User Data into the Firebase Realtime Database
                    ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(textFullName, textDoB, textGender, textMobile);

                    // Extracting User Reference from Database for " Registered Users "
                    DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");
                    referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                // Send Verification Email
                                firebaseUser.sendEmailVerification();
                                Toast.makeText(Register.this, "User Registered Successfully, Please verify mail address!", Toast.LENGTH_LONG).show();
                                // Open User Profile after successful registration
//                                Intent intent = new Intent(Register.this, UserProfileActivity.class);
//                                // To prevent user returning to Register Activity on Pressing back button after registration
//                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                                startActivity(intent);
//                                finish(); // to close register activity

                            }else{
                                Toast.makeText(Register.this, "User Registration Failed, Please Try Again!", Toast.LENGTH_LONG).show();

                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });

                }
                else{
                    try{
                        throw task.getException();
                    }catch(FirebaseAuthWeakPasswordException e){
                        editTextRegisterPassword.setError("Your Password is too weak! use mix of alphabets and numbers");
                        editTextRegisterPassword.requestFocus();
                    }catch(FirebaseAuthInvalidCredentialsException e){
                        editTextRegisterPassword.setError("Your email is invalid or already used!");
                        editTextRegisterPassword.requestFocus();
                    }catch(FirebaseAuthUserCollisionException e){
                        editTextRegisterPassword.setError("User is already registered");
                        editTextRegisterPassword.requestFocus();
                    }catch(Exception e){
                        Log.e(TAG,e.getMessage());
                        Toast.makeText(Register.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
        });
    }
}