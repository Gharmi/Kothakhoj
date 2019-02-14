package com.lipak.models;

/**
 * Author : nilkamal,
 * Creation Date: 8/8/18.
 */
public class User {

    private String fullName;
    private String contactNo;
    private String Email;
    private boolean isRenter;


    public User(){

    }


    public User(String fullName, String contactNo, String email, boolean isRenter) {
        this.fullName = fullName;
        this.contactNo = contactNo;
        Email = email;
        this.isRenter = isRenter;
    }


    public String getFullName() {
        return fullName;
    }

    public String getContactNo() {
        return contactNo;
    }

    public String getEmail() {
        return Email;
    }

    public boolean isRenter() {
        return isRenter;
    }
}
