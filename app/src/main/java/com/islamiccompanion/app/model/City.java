package com.islamiccompanion.app.model;

import com.google.gson.annotations.SerializedName;

/** Represents a world city with coordinates for prayer-time calculation. */
public class City {

    @SerializedName("name")
    private String name;

    @SerializedName("country")
    private String country;

    @SerializedName("lat")
    private double latitude;

    @SerializedName("lng")
    private double longitude;

    @SerializedName("tz")
    private String timezone;

    public City() {}

    public City(String name, String country, double latitude, double longitude, String timezone) {
        this.name = name;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timezone = timezone;
    }

    public String getName() { return name; }
    public String getCountry() { return country; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getTimezone() { return timezone; }

    @Override
    public String toString() {
        return name + ", " + country;
    }
}
