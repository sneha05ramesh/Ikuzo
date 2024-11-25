package com.example.ikuzo;

public class ReadWriteUserDetails {
    private String dob;
    private String gender;
    private String phoneNumber;
    private String email;
    private String username;

    public ReadWriteUserDetails(String dob, String gender, String phoneNumber, String email, String username) {
        this.dob = dob;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.username = username;
    }

    // Getters and Setters
    public String getDob() {
        return dob;
    }

    public String getGender() {
        return gender;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }
}
