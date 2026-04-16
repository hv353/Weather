package com.example.weather.Activitis;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

public class LocationHelper {

    public static final int REQUEST_CODE = 1001;

    public interface LocationCallback {
        void onLocationReady(double lat, double lon);
        void onLocationFailed(String reason);
    }

    // Kiểm tra và xin permission nếu chưa có
    public static boolean hasPermission(Context ctx) {
        return ActivityCompat.checkSelfPermission(ctx,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(ctx,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                REQUEST_CODE);
    }

    // Lấy vị trí hiện tại (một lần, không liên tục)
    public static void getCurrentLocation(Context ctx, LocationCallback callback) {
        if (!hasPermission(ctx)) {
            callback.onLocationFailed("Permission denied");
            return;
        }

        FusedLocationProviderClient client =
                LocationServices.getFusedLocationProviderClient(ctx);

        CancellationTokenSource cts = new CancellationTokenSource();

        try {
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            callback.onLocationReady(location.getLatitude(), location.getLongitude());
                        } else {
                            // Fallback: thử lastLocation
                            client.getLastLocation().addOnSuccessListener(last -> {
                                if (last != null) {
                                    callback.onLocationReady(last.getLatitude(), last.getLongitude());
                                } else {
                                    callback.onLocationFailed("Location unavailable");
                                }
                            });
                        }
                    })
                    .addOnFailureListener(e -> callback.onLocationFailed(e.getMessage()));
        } catch (SecurityException e) {
            callback.onLocationFailed("Security exception: " + e.getMessage());
        }
    }
}