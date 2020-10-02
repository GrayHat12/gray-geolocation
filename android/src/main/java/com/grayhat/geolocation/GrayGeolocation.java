package com.grayhat.geolocation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import com.getcapacitor.JSObject;
import com.getcapacitor.Logger;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.PluginRequestCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.util.HashMap;
import java.util.Map;

@NativePlugin(
    permissions = { Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION },
    permissionRequestCode = PluginRequestCodes.GEOLOCATION_REQUEST_PERMISSIONS,
    requestCodes = {GrayGeolocation.REQUEST_CHECK_SETTING}
)
public class GrayGeolocation extends Plugin {
    protected final static int REQUEST_CHECK_SETTING = 13451;
    protected final static String TAG = "GGEO";

    @PluginMethod
    public void turnLocationOn(PluginCall call) {
        saveCall(call);
        //pluginRequestAllPermissions();
        askPermission();
    }

    @Override
    protected void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.handleRequestPermissionsResult(requestCode, permissions, grantResults);
        PluginCall savedCall = getSavedCall();
        if(savedCall == null) {
            Logger.debug(TAG,"No stored plugin call for Location request result");
            return;
        }
        for(int result: grantResults) {
            if(result == PackageManager.PERMISSION_DENIED) {
                Logger.debug("Test", "User denied Permission");
                return;
            }
        }
        if (requestCode == REQUEST_CHECK_SETTING) {
            askPermission();
        }
    }

    private void askPermission() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getContext()).checkLocationSettings(builder.build());
        Logger.debug(TAG,"HERE L64");
        result.addOnCompleteListener(
                new OnCompleteListener<LocationSettingsResponse>() {

                    @Override
                    public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                        Logger.debug(TAG,"HERE L70");
                        try {
                            LocationSettingsResponse response = task.getResult(ApiException.class);
                            sendResponse(true);
                            Logger.debug(TAG,"HERE L74");
                        } catch (ApiException err) {
                            err.printStackTrace();
                            Logger.debug(TAG,"HERE L77 "+err.getStatusCode());
                            switch (err.getStatusCode()) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    ResolvableApiException resolvableApiException = (ResolvableApiException) err;
                                    try {
                                        //((Activity)getContext()).startIntentSenderForResult(err.getStatus().getResolution().getIntentSender(),REQUEST_CHECK_SETTING,null,0,0,0,null);
                                        err.getStatus().startResolutionForResult((Activity) getContext(), REQUEST_CHECK_SETTING);
                                    } catch (IntentSender.SendIntentException ex) {
                                        ex.printStackTrace();
                                        sendResponse(false);
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    sendResponse(false);
                                    break;
                                default:
                                    sendResponse(false);
                                    break;
                            }
                        }
                    }
                }
        );
    }

    @Override
    protected void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
        super.handleOnActivityResult(requestCode, resultCode, data);
        Logger.debug(TAG,requestCode+"GRAYCODE"+REQUEST_CHECK_SETTING);
        if(requestCode == REQUEST_CHECK_SETTING) {
            if(resultCode == Activity.RESULT_OK) {
                sendResponse(true);
            }else {
                sendResponse(false);
            }
        }
        else {
            sendResponse(false);
        }
    }

    private void sendResponse(boolean b) {
        Logger.debug(TAG,"SENDING RESPONSE" + b);
        JSObject object = new JSObject();
        object.put("res" , b);
        PluginCall call = getSavedCall();
        call.resolve(object);
    }
}
