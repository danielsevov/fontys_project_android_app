package com.fontys.vistaapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.fontys.vistaapplication.API.BackendAPI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class MapsFragment extends Fragment {
    @SuppressLint("MissingPermission")
    ActivityResultLauncher<String[]> permissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                        result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                        result.getOrDefault(Manifest.permission.CAMERA, false);
                    }
            );

    private FusedLocationProviderClient fusedLocationClient;

    /**
     * To get the spot when the actual upload happens
     */
    @SuppressLint("MissingPermission")
    private void getCurrentLocation(OnSuccessListener<? super Location> callback) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation().addOnSuccessListener(callback);
            }
            // You can use the API that requires the permission.
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
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

    private HashMap<Integer, Marker> _markers = new HashMap<>();
    private Marker lastClickedMarker;
    private Timer timer;
    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @SuppressLint("MissingPermission")
        @Override
        public void onMapReady(GoogleMap googleMap) {
            timer = new Timer("Map-Updater", false);

            getCurrentLocation(location -> {
                if (location == null) return;

                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLatLng, 10);
                googleMap.moveCamera(update);
            });

            try {
                googleMap.setMyLocationEnabled(true);
            } catch (SecurityException ex) {
                //No permission!
            }

            googleMap.setOnMapClickListener(latLng -> lastClickedMarker = null);

            googleMap.setOnMarkerClickListener(marker -> {
                if (lastClickedMarker == null) {
                    lastClickedMarker = marker;
                    Log.d("MARKER_CLICKED", "Once");
                } else if (lastClickedMarker.getPosition().equals(marker.getPosition())) {
                    JSONObject spot = (JSONObject) (marker.getTag());
                    Log.d("MARKER_CLICKED", "Twice");
                    SpotOverviewFragment fragment = null;
                    try {
                        fragment = new SpotOverviewFragment(spot.getInt("id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    timer.cancel();
                    Helper.replaceFragment(getParentFragmentManager(), fragment);
                    lastClickedMarker = marker;
                } else {
                    lastClickedMarker = marker;
                    Log.d("MARKER", "different clicked once");
                }
                return false;
            });

            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (Looper.myLooper() == null) Looper.prepare();

                    if (!BackendAPI.IsJWTPresent()) return;

                    Optional<JSONObject> response = BackendAPI.Send(null, "spots", "GET");
                    if (!response.isPresent()) return;

                    try {
                        JSONObject data = response.get();
                        JSONArray spots = data.getJSONArray("data");
                        Collection<Marker> existingMarkers = ((HashMap<Integer, Marker>) _markers.clone()).values();

                        for (int i = 0; i < spots.length(); i++) {
                            JSONObject spot = spots.getJSONObject(i);
                            LatLng markerPoint = new LatLng(Float.parseFloat(spot.getString("coordinates").split(",")[0]),
                                    Float.parseFloat(spot.getString("coordinates").split(",")[1]));

                            try {
                                getActivity().runOnUiThread(() -> {
                                    try {
                                        if (!_markers.containsKey(spot.getInt("id"))) {
                                            Marker marker = googleMap.addMarker(new MarkerOptions().position(markerPoint).title(spot.getString("title")));
                                            marker.setTag(spot);
                                            System.out.println("Adding a marker: " + spot.getString("title"));
                                            _markers.put(spot.getInt("id"), marker);
                                        }

                                        _markers.get(spot.getInt("id")).setTitle(spot.getString("title"));
                                        _markers.get(spot.getInt("id")).setTag(spot);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                });
                            } catch (NullPointerException ex) {
                                //Could not run on ui thread!
                            }

                            if (_markers.containsKey(spot.getInt("id"))) {
                                Marker mrk = _markers.get(spot.getInt("id"));
                                existingMarkers.remove(mrk);
                            }
                        }

                        ///System.out.println("Markers to remove: " + existingMarkers.stream().count());

                        try {
                            getActivity().runOnUiThread(() -> {
                                for (Marker mark : existingMarkers) {
                                    _markers.remove(mark.getPosition());
                                    mark.remove();
                                }
                            });
                        } catch (NullPointerException ex) {
                            //Cannot run on ui thread!
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 5000);

        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        if (getActivity().findViewById(R.id.bottomNavigationView) != null) {
            getActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.VISIBLE);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }
}