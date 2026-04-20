package com.example.myapplication;

public class Alert {
    public String status;
    public long timestamp;
    public double latitude;
    public double longitude;

    public Alert() {}

    public Alert(String status, long timestamp, double latitude, double longitude) {
        this.status = status;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
