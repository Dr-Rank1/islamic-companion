package com.islamiccompanion.app.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

/** Thin wrapper around FusedLocationProviderClient. Caller must hold location permission. */
public class LocationHelper {

    public interface LocationCallback2 {
        void onLocationReceived(double lat, double lng);
        void onLocationFailed(String reason);
    }

    private final FusedLocationProviderClient fusedClient;
    private LocationCallback locationCallback;

    public LocationHelper(Context context) {
        fusedClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Requests a single location fix. Uses last known location first; falls back to a fresh
     * one-shot request if stale or unavailable.
     */
    @SuppressLint("MissingPermission")
    public void getLocation(Context context, LocationCallback2 callback) {
        fusedClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        callback.onLocationReceived(location.getLatitude(), location.getLongitude());
                    } else {
                        requestFreshLocation(callback);
                    }
                })
                .addOnFailureListener(e -> requestFreshLocation(callback));
    }

    @SuppressLint("MissingPermission")
    private void requestFreshLocation(LocationCallback2 callback) {
        LocationRequest req = new LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10000)
                .setMaxUpdates(1)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                fusedClient.removeLocationUpdates(locationCallback);
                Location loc = result.getLastLocation();
                if (loc != null) {
                    callback.onLocationReceived(loc.getLatitude(), loc.getLongitude());
                } else {
                    callback.onLocationFailed("No location result");
                }
            }
        };

        fusedClient.requestLocationUpdates(req, locationCallback, Looper.getMainLooper())
                .addOnFailureListener(e -> callback.onLocationFailed(e.getMessage()));
    }

    public void stopUpdates() {
        if (locationCallback != null) {
            fusedClient.removeLocationUpdates(locationCallback);
        }
    }

    /** Compute great-circle bearing from (fromLat, fromLng) to (toLat, toLng). */
    public static float bearingTo(double fromLat, double fromLng, double toLat, double toLng) {
        double dLng = Math.toRadians(toLng - fromLng);
        double lat1 = Math.toRadians(fromLat);
        double lat2 = Math.toRadians(toLat);
        double x = Math.sin(dLng) * Math.cos(lat2);
        double y = Math.cos(lat1) * Math.sin(lat2)
                 - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLng);
        double bearing = Math.toDegrees(Math.atan2(x, y));
        return (float) ((bearing + 360) % 360);
    }
}
