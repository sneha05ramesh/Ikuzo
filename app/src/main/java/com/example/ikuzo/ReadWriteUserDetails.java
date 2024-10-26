package com.example.ikuzo;

public class ReadWriteUserDetails {
    public String dob, gender, mobile;
    // No-argument constructor required for Firebase
    public ReadWriteUserDetails() {}
    public ReadWriteUserDetails(String textDoB, String textGender, String textMobile){
        this.dob = textDoB;
        this.gender = textGender;
        this.mobile = textMobile;
    }
}
