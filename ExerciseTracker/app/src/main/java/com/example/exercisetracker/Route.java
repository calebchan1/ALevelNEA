package com.example.exercisetracker;

import java.util.ArrayList;

public class Route {
    private ArrayList<Double[]> route;

    public Route(ArrayList<Double[]> route) {
        this.route = route;
    }

    public ArrayList<Double[]> getRoute() {
        return route;
    }

    public void setRoute(ArrayList<Double[]> route) {
        this.route = route;
    }

    public void addRoute(Double[] entry){
        this.route.add(entry);
    }
}
