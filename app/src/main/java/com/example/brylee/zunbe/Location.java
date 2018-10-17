package com.example.brylee.zunbe;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by brylee on 5/1/17.
 */

public class Location implements Serializable {
    private static final double EARTH_EQUATORIAL_RADIUS = 6378.1370D;
    private static final double CONVERT_DEGREES_TO_RADIANS = Math.PI / 180D;
    private static final double CONVERT_KM_TO_MILES = 0.621371;
    private int id;
    private String name;
    private double latitudeDegrees;
    private double latitudeRadians;
    private double longitudeDegrees;
    private double longitudeRadians;
    private String address;
    private String createdAt;

    public Location() {
        this.name = "";
        this.address = "";
    }

    public Location(String name, double latitude, double longitude, String address) {
        this.name = name;
        this.latitudeDegrees = latitude;
        this.latitudeRadians = latitudeDegrees * CONVERT_DEGREES_TO_RADIANS;
        this.longitudeDegrees = longitude;
        this.longitudeRadians = longitudeDegrees * CONVERT_DEGREES_TO_RADIANS;
        this.address = address;
    }

    public Location(String name, LatLng latLng, String address) {
        this.name = name;
        this.latitudeDegrees = latLng.latitude;
        this.latitudeRadians = latitudeDegrees * CONVERT_DEGREES_TO_RADIANS;
        this.longitudeDegrees = latLng.longitude;
        this.longitudeRadians = longitudeDegrees * CONVERT_DEGREES_TO_RADIANS;
        this.address = address;
    }

    public Location(Place place) {
        this.name = place.getName().toString();
        this.latitudeDegrees = place.getLatLng().latitude;
        this.latitudeRadians = latitudeDegrees * CONVERT_DEGREES_TO_RADIANS;
        this.longitudeDegrees = place.getLatLng().longitude;
        this.longitudeRadians = longitudeDegrees * CONVERT_DEGREES_TO_RADIANS;
        this.address = place.getAddress().toString();
    }

    public double measureDistance(Location loc) {
        double deltaLon = (loc.getLongitudeRadians() - this.getLongitudeRadians());
        double deltaLat = (loc.getLatitudeRadians() - this.getLatitudeRadians());
        double a = Math.pow(Math.sin(deltaLat / 2D), 2D) +
                Math.cos(this.getLatitudeRadians()) * Math.cos(loc.getLatitudeRadians()) * Math.pow(Math.sin(deltaLon / 2D), 2D);
        return CONVERT_KM_TO_MILES * EARTH_EQUATORIAL_RADIUS * 2D * Math.atan2(Math.sqrt(a), Math.sqrt(1D - a));
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitudeDegrees() {
        return latitudeDegrees;
    }

    public void setLatitudeDegrees(double latitudeDegrees) {
        this.latitudeDegrees = latitudeDegrees;
    }

    public double getLongitudeDegrees() {
        return longitudeDegrees;
    }

    public void setLongitudeDegrees(double longitudeDegrees) {
        this.longitudeDegrees = longitudeDegrees;
    }

    @Override
    public String toString() {
        return name;
    }

    public double getLatitudeRadians() {
        return latitudeRadians;
    }

    public void setLatitudeRadians(double latitudeRadians) {
        this.latitudeRadians = latitudeRadians;
    }

    public double getLongitudeRadians() {
        return longitudeRadians;
    }

    public void setLongitudeRadians(double longitudeRadians) {
        this.longitudeRadians = longitudeRadians;
    }

    public LatLng getLatlng() {
        return new LatLng(latitudeDegrees, longitudeDegrees);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
