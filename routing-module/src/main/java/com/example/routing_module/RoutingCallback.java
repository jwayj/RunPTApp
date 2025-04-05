package com.example.routing_module;

import java.nio.file.Path;



public interface RoutingCallback {
    void onRouteCalculated(String geoJson, Path selectedPath);
}
