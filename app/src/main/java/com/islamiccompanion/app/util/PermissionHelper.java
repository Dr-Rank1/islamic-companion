package com.islamiccompanion.app.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;

/** Utility methods for runtime permission checks and requests. */
public final class PermissionHelper {

    public static final int REQUEST_LOCATION = 1001;
    public static final int REQUEST_NOTIFICATIONS = 1002;

    private PermissionHelper() {}

    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Pre-Android 13: permission granted by default
    }

    /**
     * Launch the location permission request using the Activity Result API launcher.
     * The caller should create the launcher with
     * {@code registerForActivityResult(new RequestMultiplePermissions(), callback)}.
     */
    public static void requestLocationPermissions(ActivityResultLauncher<String[]> launcher) {
        launcher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    public static void requestNotificationPermission(ActivityResultLauncher<String> launcher) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    /** Whether the user has permanently denied a permission (and we need to send them to Settings). */
    public static boolean isPermanentlyDenied(Activity activity, String permission) {
        return !activity.shouldShowRequestPermissionRationale(permission)
                && ContextCompat.checkSelfPermission(activity, permission)
                   != PackageManager.PERMISSION_GRANTED;
    }
}
