package com.ottershare.ottershare;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Binder;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.model.LatLng;

public class FusedGpsService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final IBinder gpsBinder = new LocalBinder();
    private FusedLocationProviderApi myFusedLocationProviderApi;
    private GoogleApiClient myGoogleApiClient;
    private LocationRequest myLocationRequest;
    private String LOG_TAG = myFusedLocationProviderApi.getClass().getSimpleName();
    private LatLng currentLocation;

    //Default Constructor.
    public FusedGpsService() {
    }

    @Override
    public void onCreate() {
        Log.w(LOG_TAG , "on create");
        myGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        myLocationRequest = new LocationRequest();
        myLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);

        myFusedLocationProviderApi = LocationServices.FusedLocationApi;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return gpsBinder;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.w(LOG_TAG, "on conected");
        requestLocationUpdates();
    }

    private void requestLocationUpdates() {
        Log.w(LOG_TAG, "Request location updates");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(myGoogleApiClient, myLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w("gps", "on connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(LOG_TAG, "on connection failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.w(LOG_TAG, "on location changed");
        currentLocation = new LatLng(location.getLatitude(),location.getLongitude());
    }

    // used to bind activity to this service
    public class LocalBinder extends Binder{
        FusedGpsService getservice(){
            return FusedGpsService.this;
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.w(LOG_TAG, "on start");
        onstartgps();
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w(LOG_TAG, "onStartCommand");
        onstartgps();
        onResume();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.w(LOG_TAG, "on destroy");
        super.onDestroy();
        onstop();
    }


    public void onstartgps(){
        Log.w(LOG_TAG, "onstart gps");
        myGoogleApiClient.connect();
    }


    public void onPause(){
        LocationServices.FusedLocationApi.removeLocationUpdates(myGoogleApiClient,this);
    }

    public void onResume(){
        Log.w(LOG_TAG, "on Resume");
        if(myGoogleApiClient.isConnected()){
            requestLocationUpdates();
        }
    }

    public void onstop(){
        Log.w(LOG_TAG, "on stop");
        myGoogleApiClient.disconnect();
    }

    public LatLng getLocationLatLng(){
        return currentLocation;
    }
}
