package com.example.routing_module;

import android.content.Context;

public interface RouteService {
    void calculateRoute(
            Context context,
            double startLat,
            double startLon,
            double endLat,
            double endLon,
            RouteCallback callback
    );
}


