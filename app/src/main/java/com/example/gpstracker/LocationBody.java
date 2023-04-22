package com.example.gpstracker;

import android.location.Location;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

public class LocationBody {
    public double longitude;
    public double latitude;
    public double altitude;
    public double speed;
    public double bearing;
    public LocalDateTime date;
//    private Location location;
//
//    @SerializedName("date")
//    private LocalDateTime date;
//
//    public LocationBody(Location location, LocalDateTime date) {
//        this.location = location;
//        this.date = date;
//    }
//
//    public LocalDateTime getDate() {
//        return date;
//    }
//
//    public void setLocation(Location location) {
//        this.location = location;
//    }
//
//    public Location getLocation() {
//        return location;
//    }
}
