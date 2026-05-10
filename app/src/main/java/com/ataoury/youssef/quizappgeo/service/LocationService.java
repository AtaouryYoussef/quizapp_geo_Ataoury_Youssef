package com.ataoury.youssef.quizappgeo.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationService {

    public interface LocationCallback {
        void onLocationReceived(double latitude, double longitude);

        void onError(String message);
    }

    @SuppressLint("MissingPermission")
    public void getCurrentLocation(@NonNull Context ctx, @NonNull LocationCallback callback) {
        if (!hasLocationPermission(ctx)) {
            callback.onError("Permissions de localisation refusées (ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION).");
            return;
        }

        if (!isLocationEnabled(ctx)) {
            callback.onError("Le GPS est désactivé. Veuillez activer la localisation.");
            return;
        }

        FusedLocationProviderClient fusedLocationClient = LocationServices
                .getFusedLocationProviderClient(ctx.getApplicationContext());

        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.getToken())
                .addOnSuccessListener(location -> handleLocationResult(location, callback))
                .addOnFailureListener(e -> callback.onError(e.getMessage() != null
                        ? e.getMessage()
                        : "Impossible de récupérer la position actuelle."));
    }

    public String getCityFromLocation(@NonNull Context ctx, double lat, double lng) {
        Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                if (address.getLocality() != null && !address.getLocality().trim().isEmpty()) {
                    return address.getLocality();
                }
                if (address.getSubAdminArea() != null && !address.getSubAdminArea().trim().isEmpty()) {
                    return address.getSubAdminArea();
                }
                if (address.getAdminArea() != null && !address.getAdminArea().trim().isEmpty()) {
                    return address.getAdminArea();
                }
            }
        } catch (IOException ignored) {
            // En cas d'échec du Geocoder, on retourne un fallback sur les coordonnées.
        }

        return String.format(Locale.US, "%.5f, %.5f", lat, lng);
    }

    private boolean hasLocationPermission(@NonNull Context ctx) {
        boolean hasFine = ContextCompat.checkSelfPermission(ctx,
                Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED;
        boolean hasCoarse = ContextCompat.checkSelfPermission(ctx,
                Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED;
        return hasFine || hasCoarse;
    }

    private boolean isLocationEnabled(@NonNull Context ctx) {
        LocationManager locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return false;
        }

        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ignored) {
            // Provider indisponible.
        }

        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ignored) {
            // Provider indisponible.
        }

        return gpsEnabled || networkEnabled;
    }

    private void handleLocationResult(Location location, @NonNull LocationCallback callback) {
        if (location == null) {
            callback.onError("Position introuvable pour le moment. Réessayez.");
            return;
        }
        callback.onLocationReceived(location.getLatitude(), location.getLongitude());
    }
}
