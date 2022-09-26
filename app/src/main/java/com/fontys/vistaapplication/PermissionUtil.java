package com.fontys.vistaapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Class that gives access to the camera. Use the static open method to access.
 */
public class PermissionUtil {

    public static final int CAMERA_PERMISSION_CODE = 1;
    public static final int CAMERA_REQUEST_CODE = 2;

    private static ActivityResultLauncher<String[]> permissionRequest;

    /**
     * Location section
     */

    public static boolean isGPSactive(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            new AlertDialog.Builder(context)
                    .setMessage(R.string.gps_network_not_enabled)
                    .setPositiveButton(R.string.open_location_settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton(R.string.Cancel, null)
                    .show();
            return false;

        }
        return true;
    }

    @SuppressLint("MissingPermission")
    public static void getCurrentLocation(Context context, Fragment fragment, OnSuccessListener<? super Location> callback) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation().addOnSuccessListener(callback);
            }
            // You can use the API that requires the permission.
        } else if (fragment != null && fragment.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            permissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    /**
     * Camera section
     */

    public static void openCamera(Context context, Fragment fragment) {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                fragment.startActivityForResult(intent, CAMERA_REQUEST_CODE);
            } else {
                permissionRequest.launch(new String[]{
                        Manifest.permission.CAMERA
                });
            }
        }, 250);


    }


    /**
     * Permissions section
     */

    public static void initPermission(Fragment fragment) {
        permissionRequest = fragment.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                        result -> {
                            result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                            result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                            result.getOrDefault(Manifest.permission.CAMERA, false);
                        }
                );
    }
}
