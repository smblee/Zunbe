package com.example.brylee.zunbe.aco;

import com.example.brylee.zunbe.Location;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by brylee on 5/1/17.
 */

public class Route implements Serializable {
    private int id;
    private String title;
    private ArrayList<Location> locations;
    private double distance;
    private String createdAt;

    public Route() {
        locations = new ArrayList<>();
        distance = 0;
        title = "";
    }

    public Route(String title) {
        locations = new ArrayList<>();
        distance = 0;
        this.title = title;
    }
    public Route(ArrayList<Location> locations, double distance) {
        this.locations = locations;
        this.distance = distance;
    }
    public ArrayList<Location> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<Location> locs) {
        this.locations = locs;
    }


    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

    public String toString() {
        return Arrays.toString(locations.toArray()) + " | " + distance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
