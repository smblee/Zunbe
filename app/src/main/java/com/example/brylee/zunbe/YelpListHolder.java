package com.example.brylee.zunbe;

import java.util.ArrayList;

/**
 * Singleton design pattern to hold one instance of our savedList throughout the application.
 */
public class YelpListHolder {

    private static YelpListHolder instance = null;
    private static ArrayList<Restaurant> savedList;

    public static YelpListHolder getInstance() {
        if (instance == null) {
            savedList = new ArrayList<>();
            instance = new YelpListHolder();
        }

        return instance;
    }

    public ArrayList<Restaurant> getSavedList() {
        return savedList;
    }

    public void setSavedList(ArrayList<Restaurant> list) {
        savedList = list;
    }
}
