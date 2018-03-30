package com.example.android.shushme;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;
import java.util.List;

public class Geofencing implements ResultCallback {
    private static final String TAG = "Geofencing";
    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private List<Geofence> mGeofenceList = new ArrayList<>();
    private PendingIntent mGeoFencePEndingIntent;
    private static final float GEOFENCE_RADIUS = 50; // 50 meters
    private static final long GEOFENCE_TIMEOUT = 24 * 60 * 60 * 1000; // 24 hours

    public Geofencing(Context context, GoogleApiClient googleApiClient) {
        mContext = context;
        mGoogleApiClient = googleApiClient;
    }

    public void updateGeofencesList(PlaceBuffer placeBuffer) {
        if(placeBuffer == null || placeBuffer.getCount() == 0) return;
        for(Place place : placeBuffer) {
            Geofence geofence = new Geofence.Builder()
                    .setRequestId(place.getId())
                    .setCircularRegion(place.getLatLng().latitude, place.getLatLng().latitude, GEOFENCE_RADIUS)
                    .setExpirationDuration(GEOFENCE_TIMEOUT)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();
            mGeofenceList.add(geofence);
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeoFencePEndingIntent != null) {
            return mGeoFencePEndingIntent;
        }
        Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeoFencePEndingIntent = PendingIntent.getService(mContext, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeoFencePEndingIntent;
    }

    public void registerAllGeofences() {
        LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(),
                getGeofencePendingIntent()).setResultCallback(this);

    }

    public void unRegisterAllGeofences() {
        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient,
                getGeofencePendingIntent()).setResultCallback(this);
    }

    @Override
    public void onResult(@NonNull Result result) {
        Log.d(TAG, "onResult: Error adding/removing geofence" + result.getStatus().getStatusMessage());
    }
}
