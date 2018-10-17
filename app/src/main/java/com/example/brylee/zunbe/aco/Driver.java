package com.example.brylee.zunbe.aco;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by brylee on 5/1/17.
 */

public class Driver {
    private int numOfAnts = 2000;
    public static final double PROCESSING_CYCLE_PROBABILITY = 0.8;
    public static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    public static ExecutorCompletionService<Ant> executorCompletionService = new ExecutorCompletionService<Ant>(executorService);
    public Route shortestRoute = null;
    public int activeAnts = 0;

    public Driver(int accuracyLevel) {
        numOfAnts = accuracyLevel * 150 + 500;
        System.out.println("ACCURACY " + numOfAnts);
    }
//
//    public static void main(String[] args) {
//        ArrayList<Location> locations = new ArrayList<>(Arrays.asList(new Location("New York", 40.7128, -74.0059, null),
//                new Location("Los Angeles", 34.0522, -118.2437, null),
//                new Location("Chicago", 41.8781, -87.6298, null),
//                new Location("Boston", 42.3601, -71.0589, null),
//                new Location("Houston", 29.7604, -95.3698, null),
//                new Location("Austin", 30.2672, -97.7431, null),
//                new Location("San Francisco", 37.7749, -122.4194, null),
//                new Location("Denver", 39.7392, -104.9903, null)
//                ));
//
//        Driver driver=  new Driver();
//        AntColonyOptimization aco = new AntColonyOptimization(locations);
//        for (int i = 1; i < numOfAnts; i++) {
//            executorCompletionService.submit(new Ant(aco, i, locations));
//            driver.activeAnts++;
//            if (Math.random() > PROCESSING_CYCLE_PROBABILITY) driver.processAnts();
//        }
//        driver.processAnts();
//        executorService.shutdownNow();
//        System.out.println("OptimalRoute: " + Arrays.toString(driver.shortestRoute.getLocations().toArray()));
//    }

    public void processAnts() {
        while (activeAnts > 0) {
            try {
                Ant ant = executorCompletionService.take().get();
                Route currentRoute = ant.getRoute();
                if (shortestRoute == null || currentRoute.getDistance() < shortestRoute.getDistance()) {
                    shortestRoute = currentRoute;
                    System.out.println(Arrays.toString(shortestRoute.getLocations().toArray()) + " |" + currentRoute.getDistance() + "| " + ant.getAntNumber());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            activeAnts--;
        }
    }

    public static void resetExecutorService() {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        executorCompletionService = new ExecutorCompletionService<Ant>(executorService);
    }

    public int getNumOfAnts() {
        return numOfAnts;
    }

    public void setNumOfAnts(int numOfAnts) {
        this.numOfAnts = numOfAnts;
    }
}
