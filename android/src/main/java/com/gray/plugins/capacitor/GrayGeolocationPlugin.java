package com.gray.plugins.capacitor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;

import com.getcapacitor.JSObject;
import com.getcapacitor.Logger;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;

@CapacitorPlugin(
        name = "GrayGeolocation",
        requestCodes = {
                GrayGeolocationPlugin.REQUEST_CHECK_SETTING
        },
        permissions = {
                @Permission(strings = Manifest.permission.ACCESS_COARSE_LOCATION, alias = "coarse"),
                @Permission(strings = Manifest.permission.ACCESS_FINE_LOCATION, alias = "fine")
        })
public class GrayGeolocationPlugin extends Plugin {

    protected final static int REQUEST_CHECK_SETTING = 13451;
    protected final static String TAG = "GGEO";
    private Location previousLocation = null;
    private FusedLocationProviderClient fusedLocationProviderClient = null;

    private boolean hasPermission() {
        return getPermissionState("coarse") == PermissionState.GRANTED && getPermissionState("fine") == PermissionState.GRANTED;
    }

    private void askPermission(final PluginCall call) {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getContext()).checkLocationSettings(builder.build());
        Logger.debug(TAG,"HERE L64");
        result.addOnCompleteListener(
                task -> {
                    Logger.debug(TAG,"HERE L70");
                    JSObject call_response = new JSObject();
                    try {
                        LocationSettingsResponse response = task.getResult(ApiException.class);
                        call_response.put("res", true);
                        call.resolve(call_response);
                        Logger.debug(TAG,"HERE L74");
                    } catch (ApiException err) {
                        err.printStackTrace();
                        Logger.debug(TAG,"HERE L77 "+err.getStatusCode());
                        switch (err.getStatusCode()) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                ResolvableApiException resolvableApiException = (ResolvableApiException) err;
                                try {
                                    //((Activity)getContext()).startIntentSenderForResult(err.getStatus().getResolution().getIntentSender(),REQUEST_CHECK_SETTING,null,0,0,0,null);
                                    resolvableApiException.getStatus().startResolutionForResult((Activity) getContext(), REQUEST_CHECK_SETTING);
                                    // startActivityForResult(call, resolvableApiException.getResolution(), "a");
                                } catch (IntentSender.SendIntentException ex) {
                                    ex.printStackTrace();
                                    call_response.put("res", false);
                                    call.resolve(call_response);
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            default:
                                call_response.put("res", false);
                                call.resolve(call_response);
                                break;
                        }
                    }
                }
        );
    }

    @PluginMethod
    public void turnLocationOn(PluginCall call) {
        if(!hasPermission()) {
            requestAllPermissions(call, "locationPermissionCallbackTurnOn");
        }else {
            askPermission(call);
        }
    }

    @PermissionCallback
    private void locationPermissionCallbackTurnOn(PluginCall call) {
        if (hasPermission()) {
            askPermission(call);
        } else {
            call.reject("User Denied Permission");
        }
    }

    @SuppressLint("MissingPermission")
    private  void getLocation(final PluginCall pluginCall) {
        if(fusedLocationProviderClient == null) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient((Activity)getContext());
        }
        if(previousLocation != null) {
            JSObject object = new JSObject();
            object.put("longitude",previousLocation.getLongitude());
            object.put("latitude",previousLocation.getLatitude());
            pluginCall.resolve(object);
            return;
        }
        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
                previousLocation = task.getResult();
                if(previousLocation != null) {
                    JSObject object = new JSObject();
                    object.put("longitude",previousLocation.getLongitude());
                    object.put("latitude",previousLocation.getLatitude());
                    pluginCall.resolve(object);
                } else {
                    LocationRequest locationRequest = LocationRequest.create()
                            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                            .setInterval(10000)
                            .setFastestInterval(1000)
                            .setNumUpdates(1);
                    LocationCallback locationCallback = new LocationCallback(){
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            previousLocation = locationResult.getLastLocation();
                            if(previousLocation != null) {
                                JSObject object = new JSObject();
                                object.put("longitude",previousLocation.getLongitude());
                                object.put("latitude",previousLocation.getLatitude());
                                pluginCall.resolve(object);
                            }else {
                                pluginCall.reject("No Location Found L111");
                            }
                        }
                    };
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
                    //pluginCall.error("No Location Found L82");
                }
            });
        }
    }

    @PluginMethod
    public void getCurrentPosition(PluginCall call) {
        if(!hasPermission()) {
            requestAllPermissions(call, "locationPermissionCallbackCurrentPos");
        }
        else{
            getLocation(call);
        }
    }

    @PermissionCallback
    private void locationPermissionCallbackCurrentPos(PluginCall call) {
        if (hasPermission()) {
            getLocation(call);
        } else {
            call.reject("User Denied Permission");
        }
    }

    @Override
    protected void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
        super.handleOnActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.handleRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
