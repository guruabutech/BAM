package crescentcitydevelopment.com.bam;


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

import java.util.ArrayList;
import java.util.List;

public class Geofencing implements ResultCallback {
    private static final String TAG = Geofencing.class.getSimpleName();
    private List<Geofence> mGeofenceList;
    private Geofence mGeofence;
    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mGeofencePendingIntent;
    private Context mContext;


    public Geofencing(Context context, GoogleApiClient client){
        mContext = context;
        mGoogleApiClient = client;
       mGeofencePendingIntent = null;
        mGeofenceList = new ArrayList<>();
    }


    public void registerGeofence(){
        if(mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            Log.v(TAG,"API Client Problem");
            return;
        }
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        }catch (SecurityException securityException){
            Log.v(TAG,"Register Geofence Error: "+securityException.getMessage());
            Log.e(TAG, securityException.getMessage());
        }
    }
    public void unregisterAllGeofences(){
        if(mGoogleApiClient == null || !mGoogleApiClient.isConnected()){
            return;
        }
        try {
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        }catch (SecurityException securityException){
            Log.e(TAG, securityException.getMessage());
        }
    }



    public void addGeofence(Event event){
        mGeofence = new Geofence.Builder()
                .setRequestId(Long.toString(event.getTimeStamp()))
                .setExpirationDuration(event.getEventHours())
                .setCircularRegion(event.getLatitude(), event.getLongitude(), event.getRadius())
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

    }


    private GeofencingRequest getGeofencingRequest(){
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        //If user is already inside, trigger entry event
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        //Add geofence to ArrayList & Build it
       // builder.addGeofence(mGeofence);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent(){
        if(mGeofencePendingIntent != null){
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
        mGeofencePendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Log.v(TAG,"Flag Update: " + Integer.toString(PendingIntent.FLAG_UPDATE_CURRENT));
        return mGeofencePendingIntent;
    }

    @Override
    public void onResult(@NonNull Result result) {
            Log.e(TAG, "Error adding/removing geofence: "+result.getStatus().toString());
    }

    //    ADD GEOFENCE LIST
    public void updateGeofencesList(ArrayList<Event> events){
        mGeofenceList = new ArrayList<>();
        if(events == null || events.size() == 0) return;
        for(Event event : events){
            double placeLat = event.getLatitude();
            double placeLng = event.getLongitude();
            Geofence geofence = new Geofence.Builder()
                    .setRequestId(Long.toString(event.getTimeStamp()))
                    .setExpirationDuration(event.getEventHours())
                    .setCircularRegion(placeLat, placeLng, event.getRadius())
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();
            mGeofenceList.add(geofence);
        }
    }


    public void registerAllGeofences(){
        if(mGoogleApiClient == null || !mGoogleApiClient.isConnected() ||
                mGeofenceList == null || mGeofenceList.size() == 0) {
            return;
        }
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
                    ).setResultCallback(this);
        }catch (SecurityException securityException){
            Log.e(TAG, securityException.getMessage());
        }
    }
}
