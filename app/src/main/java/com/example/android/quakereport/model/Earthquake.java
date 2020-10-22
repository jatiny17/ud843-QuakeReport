package com.example.android.quakereport.model;

import java.net.URL;

public class Earthquake {
    private String location, date, url;
    private double magnitude;

    public Earthquake(String location, String date, double magnitude, String url) {
        this.location = location;
        this.date = date;
        this.magnitude = magnitude;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }
}
