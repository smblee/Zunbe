package com.example.brylee.zunbe.aco;

import com.example.brylee.zunbe.Location;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by brylee on 5/1/17.
 */

public class AntColonyOptimization {
    private AtomicDouble[][] phermonLevelsMatrix = null;
    private double[][] distancesMatrix = null;
    private ArrayList<Location> locations;
    private int locationsSize;

    public AntColonyOptimization(ArrayList<Location> locations) {
        this.locations = locations;
        this.locationsSize = locations.size();
        initializeDistances();
        initializePheromoneLevels();
    }

    private void initializeDistances() {
        distancesMatrix = new double[locationsSize][locationsSize];
        for (int i = 0; i < locationsSize; i++) {
            Location locY = locations.get(i);
            for (int j = 0; j < locationsSize; j++) {
                distancesMatrix[i][j] = locY.measureDistance(locations.get(j));
            }
        }
    }

    private void initializePheromoneLevels() {
        phermonLevelsMatrix = new AtomicDouble[locationsSize][locationsSize];
        Random random = new Random();
        for (int i = 0; i < locationsSize; i++) {
            for (int j = 0; j < locationsSize; j++) {
                phermonLevelsMatrix[i][j] = new AtomicDouble(random.nextDouble());
            }
        }
    }

    public AtomicDouble[][] getPhermonLevelsMatrix() {
        return phermonLevelsMatrix;
    }

    public double[][] getDistancesMatrix() {
        return distancesMatrix;
    }
}
