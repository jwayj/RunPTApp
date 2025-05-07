package com.example.myapplication.ui.activities;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class DisMeasurement {

    public interface DistanceCallback {
        void onDistanceChanged(float displayDistanceKm);
    }

    private final LocationManager locationManager;
    private final DistanceCallback callback;

    private Location lastLocation = null;
    private float totalRawDistance = 0f;
    private int displayedStepCount = 0;

    private double lastAltitude = Double.NaN;
    private double totalElevationGain = 0.0;

    public DisMeasurement(Context context, DistanceCallback callback) {
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.callback = callback;
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (lastLocation != null) {
                float segment = lastLocation.distanceTo(location);
                totalRawDistance += segment;

                int newStepCount = (int)(totalRawDistance / 10f);
                if (newStepCount > displayedStepCount) {
                    displayedStepCount = newStepCount;
                    float displayDistance = displayedStepCount * 0.01f;
                    callback.onDistanceChanged(displayDistance);
                }
            }

            // 고도 누적 측정
            double currentAltitude = location.getAltitude();
            if (!Double.isNaN(lastAltitude)) {
                double diff = currentAltitude - lastAltitude;
                if (diff > 0.5) {
                    totalElevationGain += diff;
                }
            }
            lastAltitude = currentAltitude;

            lastLocation = location;
        }

        @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override public void onProviderEnabled(String provider) {}
        @Override public void onProviderDisabled(String provider) {}
    };

    public void start() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 1, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        locationManager.removeUpdates(locationListener);
    }

    public void reset() {
        totalRawDistance = 0f;
        displayedStepCount = 0;
        lastLocation = null;
        lastAltitude = Double.NaN;
        totalElevationGain = 0.0;
    }

    public float getDisplayDistanceKm() {
        return displayedStepCount * 0.01f;
    }

    public double getTotalElevationGain() {
        return totalElevationGain;
    }
}
