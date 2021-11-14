package com.example.exercisetracker;

import java.util.ArrayList;

public class Route {
    private ArrayList<Double[]> route;
    private Double distance;
    private Integer lastRouteIndex;

    public Route(ArrayList<Double[]> route) {
        this.route = route;
        this.distance = 0d;
        this.lastRouteIndex = 0;
    }

    public void calculateDistance(){
        for (int i=lastRouteIndex;i<route.size()-1;i++){
            //implementing haversine formula to get distance
            Double [] entry1 = route.get(i);
            Double [] entry2 = route.get(i+1);
            double latDistance = Math.toRadians(entry2[0] - entry1[0]);
            double lonDistance = Math.toRadians(entry2[1] - entry1[1]);
            double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                    + Math.cos(Math.toRadians(entry1[0])) * Math.cos(Math.toRadians(entry2[0]))
                    * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            lastRouteIndex++;
            //converting to metres, and multiplying by the radius of the Earth
            distance  += (6370 * c * 1000);
        }
    }

    public Double getDistance() {
        return distance;
    }

    public Integer getLastRouteIndex() {
        return lastRouteIndex;
    }
    public ArrayList<Double[]> getRoute() {
        return route;
    }

    public void addRoute(Double[] entry){
        this.route.add(entry);
    }

    public int getRouteSize(){
        return route.size();
    }

}
