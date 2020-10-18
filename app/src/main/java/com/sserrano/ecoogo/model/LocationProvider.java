package com.sserrano.ecoogo.model;

import android.Manifest;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.sserrano.ecoogo.model.interfaces.LocationReceiver;

public class LocationProvider extends LocationCallback {

    public final static String TAG = LocationProvider.class.getSimpleName();
    public final static int REQUEST_CODE = 14879;

    private FusedLocationProviderClient fusedLocationClient;
    private Activity contActivity;
    private Location currentLocation;
    private LocationReceiver locReceiver;
    private LocationRequest locRequest;

    private boolean canRequestLocUpdates = false;


    public LocationProvider(final Activity contActivity, final LocationReceiver locReceiver){
        this.contActivity = contActivity;
        this.locReceiver = locReceiver;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(contActivity);
        trySetup();
    }

    private void trySetup(){
        try {
            if(ContextCompat.checkSelfPermission(contActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestLocationPermission();
            }


            fusedLocationClient.getLastLocation().
                    addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                currentLocation = location;
                                locReceiver.onLocationReceived(location);
                            }
                        }
                    });

            locRequest = LocationRequest.create().setInterval(3000).setFastestInterval(3000).
                    setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationSettingsRequest.Builder requestBuilder = new LocationSettingsRequest.Builder().
                    addLocationRequest(locRequest);

            SettingsClient settingsClient = LocationServices.getSettingsClient(contActivity);
            Task<LocationSettingsResponse> task =
                    settingsClient.checkLocationSettings(requestBuilder.build());

            task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    settingsAccepted();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof ResolvableApiException) {
                        try {
                            ResolvableApiException rae = (ResolvableApiException) e;
                            rae.startResolutionForResult(contActivity, REQUEST_CODE);
                        } catch (IntentSender.SendIntentException sendEx) {

                        }
                    } else {
                        locReceiver.onLocationDisabled();
                    }
                }
            });
        } catch (SecurityException e){
            requestLocationPermission();
        }
    }


    public boolean requestLocUpdatesIfPossible(){
        if(canRequestLocUpdates){
            try {
                fusedLocationClient.requestLocationUpdates
                        (locRequest, this, null);
            } catch (SecurityException e){
                Log.e(TAG, "Boolean canRequestLocationUpdates returned true, but a " +
                        "SecurityException was found when trying to request location updates: "
                        + e.toString());
            }
        }
        return canRequestLocUpdates;
    }

    public void settingsAccepted(){
        canRequestLocUpdates = true;
        requestLocUpdatesIfPossible();
    }

    public void permissionAccepted(){
        trySetup();
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
        super.onLocationResult(locationResult);
        if(locationResult == null) return;
        currentLocation = locationResult.getLastLocation();
        locReceiver.onLocationReceived(currentLocation);
    }

    @Override
    public void onLocationAvailability(LocationAvailability locationAvailability) {
        super.onLocationAvailability(locationAvailability);
        if(!locationAvailability.isLocationAvailable()){
            locReceiver.onLocationNotAvailable();
        } else {
            locReceiver.onLocationAvailable();
        }
    }

    public void requestLocationPermission() {
        ActivityCompat.requestPermissions(contActivity,
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }
}
