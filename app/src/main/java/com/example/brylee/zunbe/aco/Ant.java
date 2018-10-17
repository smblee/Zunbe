package com.example.brylee.zunbe.aco;

import com.example.brylee.zunbe.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by brylee on 5/1/17.
 */

public class Ant implements Callable<Ant> {
    public static final double Q = 0.0005; // used for adjusting amount of phermone deposited. between 0 and 1
    public static final double RHO = 0.2; // varying the level of phermone evaporation. between 0 and 1
    public static final double ALPHA = 0.01; // controlling the importance of the phermone trail. value is >= 0
    public static final double BETA = 9.5; // controlling the importance of distance between source and destination.

    private AntColonyOptimization aco;
    private int antNumber;
    private Route route = null;
    static int invalidLocationIndex = -1;
    private ArrayList<Location> locations;
    private int numOfLocations;
    private int startingLocation;
    public Route getRoute() {
        return route;
    }

    public Ant(AntColonyOptimization aco, int antNumber, ArrayList<Location> locations, int startingLoc) {
        this.aco = aco;
        this.antNumber = antNumber;
        this.locations = locations;
        this.numOfLocations = locations.size();
        this.startingLocation = startingLoc;
    }

    public int getAntNumber() {
        return antNumber;
    }

    @Override
    public Ant call() throws Exception {
        int originatingLocIndex = startingLocation >= 0 ? startingLocation : ThreadLocalRandom.current().nextInt(numOfLocations);
        ArrayList<Location> routeLocations = new ArrayList<>(numOfLocations);
        HashMap<String, Boolean> visited = new HashMap<>();

        for (int i = 0; i < numOfLocations; i++) {
            visited.put(locations.get(i).getName(), false);
        }
        int numOfVisited = 0;
        visited.put(locations.get(originatingLocIndex).getName(), true);
        double routeDistance = 0.0;
        int x = originatingLocIndex;
        int y = invalidLocationIndex;
        if (numOfVisited != numOfLocations) y = getY(x, visited);
        while(y != invalidLocationIndex) {
            routeLocations.add(numOfVisited++, locations.get(x));
            routeDistance += aco.getDistancesMatrix()[x][y];
            adjustPhermoneLevel(x, y, routeDistance);
            visited.put(locations.get(y).getName(), true);
            x = y;
            if (numOfVisited != numOfLocations)
                y = getY(x, visited);
            else y = invalidLocationIndex;
        }
        routeDistance += aco.getDistancesMatrix()[x][originatingLocIndex];
        routeLocations.add(numOfVisited, locations.get(x));

        route = new Route(routeLocations, routeDistance);
        
        return this;
    }

    private void adjustPhermoneLevel(int x, int y, double routeDistance) {
        boolean flag = false;
        while (!flag) {
            double currentPhermoneLevel = aco.getPhermonLevelsMatrix()[x][y].doubleValue();
            double updatedPhermoneLevel = (1-RHO)*currentPhermoneLevel + Q/routeDistance;
            if (updatedPhermoneLevel < 0.00) flag = aco.getPhermonLevelsMatrix()[x][y].compareAndSet(0);
            else flag = aco.getPhermonLevelsMatrix()[x][y].compareAndSet(updatedPhermoneLevel);
        }
    }


    private int getY(int x, HashMap<String, Boolean> visited) {
        int ret = invalidLocationIndex;
        double random = ThreadLocalRandom.current().nextDouble();
        ArrayList<Double> transitionProbabilities = getTransitionProbabilities(x, visited);
        for (int y = 0; y < numOfLocations; y++ ) {
            if (transitionProbabilities.get(y) > random) {
                ret = y;
                break;
            }
            random -= transitionProbabilities.get(y);
        }
        return ret;
    }

    private ArrayList<Double> getTransitionProbabilities(int x, HashMap<String, Boolean> visited) {
        ArrayList<Double> ret = new ArrayList<>(numOfLocations);
        for (int i = 0; i < numOfLocations; i++) {
            ret.add(0.0);
        }
        double denom = getTPDenominator(ret, x, visited);
        for (int i = 0; i < numOfLocations; i++) {
            ret.set(i, ret.get(i) / denom);
        }

        return ret;
    }

    private double getTPDenominator(ArrayList<Double> transitionProbabilities, int x, HashMap<String, Boolean> visited) {
        double denom = 0.0;
        for (int y = 0; y < numOfLocations; y++) {
            if (!visited.get(locations.get(y).getName())) {
                if (x == y)
                    transitionProbabilities.set(y, 0.0);
                else
                    transitionProbabilities.set(y, getTPNumerator(x, y));
                denom += transitionProbabilities.get(y);

            }
        }

        return denom;
    }

    private double getTPNumerator(int x, int y) {
        double numer = 0.0;
        double phermoneLevel = aco.getPhermonLevelsMatrix()[y][x].doubleValue();
        if (phermoneLevel != 0.0)
            numer = Math.pow(phermoneLevel,ALPHA) * Math.pow(1/aco.getDistancesMatrix()[x][y],BETA);
        return numer;
    }

}
