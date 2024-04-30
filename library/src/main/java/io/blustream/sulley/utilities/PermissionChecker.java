package io.blustream.sulley.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

public class PermissionChecker {

    public PermissionChecker(Activity activity) {
        this.activity = activity;
    }

    private Activity activity;

    public boolean checkPermissions() {

        if (ContextCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            Toast.makeText(activity, "Debug: ACCESS_FINE_LOCATION permission not granted.", Toast.LENGTH_LONG).show();
            return false;
        }
    }
}
