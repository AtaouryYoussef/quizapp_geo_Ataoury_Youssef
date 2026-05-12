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
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationService {

    private static final long FRESH_LOCATION_MAX_AGE_MS = 2 * 60 * 1000L;
    private static final float FINE_ACCURACY_MAX_METERS = 120f;
    private static final float COARSE_ACCURACY_MAX_METERS = 1200f;

    public interface LocationCallback {
        void onLocationReceived(double latitude, double longitude);

        void onError(String message);
    }

    @SuppressLint("MissingPermission")
    public void getCurrentLocation(@NonNull Context ctx, @NonNull LocationCallback callback) {
        boolean hasFine = hasFineLocationPermission(ctx);
        boolean hasCoarse = hasCoarseLocationPermission(ctx);
        if (!hasFine && !hasCoarse) {
            callback.onError("Permissions de localisation refusées (ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION).");
            return;
        }

        if (!isLocationEnabled(ctx)) {
            Location fallbackFromProviders = getLastKnownLocationFromProviders(ctx, hasFine);
            if (isLocationUsable(fallbackFromProviders, hasFine)) {
                handleLocationResult(fallbackFromProviders, callback);
                return;
            }
            callback.onError("Activez la localisation (GPS) pour obtenir votre position exacte.");
            return;
        }

        FusedLocationProviderClient fusedLocationClient = LocationServices
                .getFusedLocationProviderClient(ctx.getApplicationContext());
        int desiredPriority = hasFine ? Priority.PRIORITY_HIGH_ACCURACY : Priority.PRIORITY_BALANCED_POWER_ACCURACY;

        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(desiredPriority,
                cancellationTokenSource.getToken())
                .addOnSuccessListener(location -> {
                    if (isLocationUsable(location, hasFine)) {
                        handleLocationResult(location, callback);
                        return;
                    }

                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(lastLocation -> {
                                if (isLocationUsable(lastLocation, hasFine)) {
                                    handleLocationResult(lastLocation, callback);
                                    return;
                                }

                                Location providerLocation = getLastKnownLocationFromProviders(ctx, hasFine);
                                if (isLocationUsable(providerLocation, hasFine)) {
                                    handleLocationResult(providerLocation, callback);
                                    return;
                                }

                                requestHighAccuracyLocation(fusedLocationClient, ctx, callback, hasFine);
                            })
                            .addOnFailureListener(e -> requestHighAccuracyLocation(fusedLocationClient, ctx, callback,
                                    hasFine));
                })
                .addOnFailureListener(e -> requestHighAccuracyLocation(fusedLocationClient, ctx, callback, hasFine));
    }

    public String getCityFromLocation(@NonNull Context ctx, double lat, double lng) {
        if (!Geocoder.isPresent()) {
            String approximateCity = approximateCityByCoordinates(lat, lng);
            if (approximateCity != null) {
                return approximateCity;
            }
            return String.format(Locale.US, "%.5f, %.5f", lat, lng);
        }

        Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 5);
            if (addresses != null && !addresses.isEmpty()) {
                for (Address address : addresses) {
                    String candidateCity = extractBestCityFromAddress(address);
                    if (candidateCity != null) {
                        return candidateCity;
                    }
                }
            }
        } catch (IOException ignored) {
            // En cas d'échec du Geocoder, on retourne un fallback sur les coordonnées.
        }

        String approximateCity = approximateCityByCoordinates(lat, lng);
        if (approximateCity != null) {
            return approximateCity;
        }

        return String.format(Locale.US, "%.5f, %.5f", lat, lng);
    }

    private boolean hasFineLocationPermission(@NonNull Context ctx) {
        return ContextCompat.checkSelfPermission(ctx,
                Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasCoarseLocationPermission(@NonNull Context ctx) {
        return ContextCompat.checkSelfPermission(ctx,
                Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED;
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

    @SuppressLint("MissingPermission")
    private void requestHighAccuracyLocation(@NonNull FusedLocationProviderClient fusedLocationClient,
            @NonNull Context ctx,
            @NonNull LocationCallback callback,
            boolean hasFinePermission) {
        CancellationTokenSource highAccuracyToken = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, highAccuracyToken.getToken())
                .addOnSuccessListener(location -> {
                    if (isLocationUsable(location, hasFinePermission)) {
                        handleLocationResult(location, callback);
                        return;
                    }

                    Location fallbackLocation = getLastKnownLocationFromProviders(ctx, hasFinePermission);
                    if (isLocationUsable(fallbackLocation, hasFinePermission)) {
                        handleLocationResult(fallbackLocation, callback);
                        return;
                    }

                    callback.onError("Impossible d'obtenir une localisation précise. Vérifiez GPS et connexion.");
                })
                .addOnFailureListener(e -> {
                    Location fallbackLocation = getLastKnownLocationFromProviders(ctx, hasFinePermission);
                    if (isLocationUsable(fallbackLocation, hasFinePermission)) {
                        handleLocationResult(fallbackLocation, callback);
                        return;
                    }

                    callback.onError(
                            "Position introuvable. Sur emulateur, ouvrez Extended Controls > Location puis Set Location.");
                });
    }

    @SuppressLint("MissingPermission")
    private Location getLastKnownLocationFromProviders(@NonNull Context ctx, boolean hasFinePermission) {
        LocationManager locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return null;
        }

        Location bestLocation = null;
        String[] providers = new String[] {
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
        };

        for (String provider : providers) {
            try {
                Location location = locationManager.getLastKnownLocation(provider);
                if (!isLocationUsable(location, hasFinePermission)) {
                    continue;
                }
                if (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy()) {
                    bestLocation = location;
                }
            } catch (Exception ignored) {
                // Provider inaccessible.
            }
        }

        return bestLocation;
    }

    private boolean isLocationUsable(Location location, boolean hasFinePermission) {
        if (location == null) {
            return false;
        }

        long nowMs = System.currentTimeMillis();
        long ageMs = nowMs - location.getTime();
        if (ageMs < 0 || ageMs > FRESH_LOCATION_MAX_AGE_MS) {
            return false;
        }

        if (location.hasAccuracy()) {
            float maxAccuracy = hasFinePermission ? FINE_ACCURACY_MAX_METERS : COARSE_ACCURACY_MAX_METERS;
            return location.getAccuracy() <= maxAccuracy;
        }

        return hasFinePermission;
    }

    private String extractBestCityFromAddress(Address address) {
        if (address == null) {
            return null;
        }

        List<String> candidates = new ArrayList<>();
        candidates.add(address.getLocality());
        candidates.add(address.getSubAdminArea());
        candidates.add(address.getAdminArea());
        candidates.add(address.getSubLocality());

        for (String candidate : candidates) {
            String normalized = normalizeCityName(candidate);
            if (normalized != null) {
                return normalized;
            }
        }
        return null;
    }

    private String normalizeCityName(String raw) {
        if (raw == null) {
            return null;
        }

        String city = raw.trim();
        if (city.isEmpty()) {
            return null;
        }

        city = city.replace("Province de ", "")
                .replace("Préfecture de ", "")
                .replace("Region de ", "")
                .replace("Région de ", "")
                .trim();

        if (city.matches("^-?\\d+(?:\\.\\d+)?,\\s*-?\\d+(?:\\.\\d+)?$")) {
            return null;
        }

        return city;
    }

    private String approximateCityByCoordinates(double lat, double lng) {
        if (isNear(lat, lng, 33.5731, -7.5898, 0.35)) {
            return "Casablanca";
        }
        if (isNear(lat, lng, 34.0209, -6.8416, 0.30)) {
            return "Rabat";
        }
        if (isNear(lat, lng, 31.6295, -7.9811, 0.35)) {
            return "Marrakech";
        }
        if (isNear(lat, lng, 34.0331, -5.0003, 0.35)) {
            return "Fes";
        }
        if (isNear(lat, lng, 35.7595, -5.8340, 0.35)) {
            return "Tanger";
        }
        if (isNear(lat, lng, 30.4278, -9.5981, 0.40)) {
            return "Agadir";
        }
        return null;
    }

    private boolean isNear(double lat, double lng, double targetLat, double targetLng, double thresholdDegrees) {
        return Math.abs(lat - targetLat) <= thresholdDegrees && Math.abs(lng - targetLng) <= thresholdDegrees;
    }
}
