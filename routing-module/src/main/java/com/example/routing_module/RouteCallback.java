package com.example.routing_module;

public interface RouteCallback {
    void onRouteCalculated(String geoJson);
    void onFailure(String error);
}