package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;

public class Booking {
    public String bookingId;
    public String templeName;
    public String date;
    public String timeSlot;
    public String userCategory; 
    public String status; 
    public String userName;
    public String userPhone;
    public int familySize; 
    public long checkInTime;
    public long checkOutTime;
    public List<FamilyMember> members;

    public Booking() {
        this.members = new ArrayList<>();
    }

    public Booking(String bookingId, String userName, String userPhone, String templeName, String date, String timeSlot, String userCategory, String status, int familySize, List<FamilyMember> members) {
        this.bookingId = bookingId;
        this.userName = userName;
        this.userPhone = userPhone;
        this.templeName = templeName;
        this.date = date;
        this.timeSlot = timeSlot;
        this.userCategory = userCategory;
        this.status = status;
        this.familySize = familySize;
        this.members = members != null ? members : new ArrayList<>();
    }
}
