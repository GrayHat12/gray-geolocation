package com.grayhat.geolocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;

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
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.tasks.Task;


@NativePlugin(
        permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
        permissionRequestCode = PluginRequestCodes.GEOLOCATION_REQUEST_PERMISSIONS,
        requestCodes = {GrayGeolocation.REQUEST_CHECK_SETTING}
)
public class GrayGeolocation extends Plugin {
    protected final static int REQUEST_CHECK_SETTING = 13451;
    protected final static String TAG = "GGEO";
    FusedLocationProviderClient fusedLocationProviderClient = null;
    Location previousLocation = null;
    int I = 10101;

    @PluginMethod
    public void turnLocationOn(PluginCall call) {
        saveCall(call);
        if(!hasRequiredPermissions()) {
            pluginRequestAllPermissions();
        }else {
            askPermission();
        }
    }

    @PluginMethod
    public void getCurrentPosition(PluginCall call) {
        if(!hasRequiredPermissions()) {
            pluginRequestAllPermissions();
        }
        else{
            getLocation(call);
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
            pluginCall.success(object);
            return;
        }
        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    previousLocation = task.getResult();
                    if(previousLocation != null) {
                        JSObject object = new JSObject();
                        object.put("longitude",previousLocation.getLongitude());
                        object.put("latitude",previousLocation.getLatitude());
                        pluginCall.success(object);
                    } else {
                        LocationRequest locationRequest = new LocationRequest()
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
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
                                    pluginCall.success(object);
                                }else {
                                    pluginCall.error("No Location Found L111");
                                }
                            }
                        };
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
                        //pluginCall.error("No Location Found L82");
                    }
                }
            });
        }
    }

    @SuppressLint("MissingPermission")
    private void returnCoords(final PluginCall call) {

        if(fusedLocationProviderClient == null) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient((Activity)getContext());
        }

        if (previousLocation != null) {
            JSObject object = new JSObject();
            object.put("longitude",previousLocation.getLongitude());
            object.put("latitude",previousLocation.getLatitude());
            call.success(object);
            return;
        }

        if (hasRequiredPermissions()) {
            /*LocationRequest mLocationRequest = LocationRequest.create();
            mLocationRequest.setInterval(60000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationCallback mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    Location location = locationResult.getLocations().get(0);
                    previousLocation = location;
                    if (previousLocation != null) {
                        JSObject object = new JSObject();
                        object.put("longitude",previousLocation.getLongitude());
                        object.put("latitude",previousLocation.getLatitude());
                        call.success(object);
                        return;
                    }
                    call.error("location not found");
                }
            };*/
            //fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,mLocationCallback,null);
            final Task<Location> locationTask = fusedLocationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,null);
            locationTask.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    previousLocation = task.getResult();
                    Logger.debug(TAG,"Task COmpleted");
                    if (call.isReleased()) return;
                    if (previousLocation != null) {
                        JSObject object = new JSObject();
                        object.put("longitude",previousLocation.getLongitude());
                        object.put("latitude",previousLocation.getLatitude());
                        call.success(object);
                        return;
                    }
                    call.error("location not found");
                    //returnCoords(call);
                }
            });
            locationTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    call.error("Location Not Found");
                }
            });
            locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    Logger.debug(TAG,"Location Success");
                    if (call.isReleased()) return;
                    if (location != null) {
                        previousLocation = location;
                        JSObject object = new JSObject();
                        object.put("longitude", previousLocation.getLongitude());
                        object.put("latitude", previousLocation.getLatitude());
                        call.success(object);
                    } else {
                        call.error("Got an error");
                    }
                }
            });
            try {
                Location location = locationTask.getResult();
                if (location != null) {
                    previousLocation = location;
                    JSObject object = new JSObject();
                    object.put("longitude",previousLocation.getLongitude());
                    object.put("latitude",previousLocation.getLatitude());
                    call.success(object);
                }
            }catch (IllegalStateException ex) {
                Logger.debug(TAG,"Fetching Location");
            }
            catch (RuntimeExecutionException ex) {
                ex.printStackTrace();
                call.error("Location Not Found 137");
            }
            /*fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    if(location != null) {
                        //Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                        JSObject object = new JSObject();
                        object.put("longitude",location.getLongitude());
                        object.put("latitude",location.getLatitude());
                        call.success(object);
                    }else {
                        call.reject("Some error occured");
                    }
                }
            });*/
        }
        else {
            call.error("Lacking Permission");
        }
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
                savedCall.error("User Denied Permission");
                return;
            }
        }
        if (requestCode == REQUEST_CHECK_SETTING) {
            if(savedCall.getMethodName().equals("getCurrentPosition")) {
                //returnCoords(savedCall);
                getLocation(savedCall);
            } else if(savedCall.getMethodName().equals("turnLocationOn")) {
                askPermission();
            } else {
                savedCall.resolve();
                savedCall.release(bridge);
            }
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
