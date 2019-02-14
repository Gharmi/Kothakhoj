package com.project.models;

import java.io.Serializable;

/**
 * Author : nilkamal,
 * Creation Date: 11/8/18.
 */
public class Room implements Serializable {

    public  String roomType;
    public  String address;
    public  long price;
    public  String date;
    public  String preference;
    public  String floor;
    public  String contact;
    public  String cooking;
    public  String rent;
    public  String owner;
    public String docrefId;



    public Room(String roomType, String address, long price, String date, String preference, String floor, String contact, String cooking, String rent, String owner,String docrefId) {
        this.roomType = roomType;
        this.address = address;
        this.price = price;
        this.date = date;
        this.preference = preference;
        this.floor = floor;
        this.contact = contact;
        this.cooking = cooking;
        this.rent = rent;
        this.owner = owner;
        this.docrefId = docrefId;
    }



    public Room(String roomType, String address, long price, String date,  String rent,String docrefId) {
        this.roomType = roomType;
        this.address = address;
        this.price = price;
        this.date = date;
        this.rent = rent;
        this.docrefId = docrefId;
    }



    public Room(){
    }


    public String getRoomType() {
        return roomType;
    }

    public String getAddress() {
        return address;
    }

    public long getPrice() {
        return price;
    }

    public String getDate() {
        return date;
    }

    public String getPreference() {
        return preference;
    }

    public String getFloor() {
        return floor;
    }

    public String getContact() {
        return contact;
    }

    public String getCooking() {
        return cooking;
    }

    public String getRent() {
        return rent;
    }

    public String getOwner() {
        return owner;
    }

    public String getDocrefId() {
        return docrefId;
    }
}
