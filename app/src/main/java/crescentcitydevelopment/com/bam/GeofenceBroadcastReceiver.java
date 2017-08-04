package crescentcitydevelopment.com.bam;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import java.util.ArrayList;
import java.util.List;


public class GeofenceBroadcastReceiver extends BroadcastReceiver{
    private String mGeofenceDetails;
    private static final String TAG = GeofenceBroadcastReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG,"On Receive");

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if(geofencingEvent.hasError()){
            Log.v(TAG,"Has Error "+Integer.toString(geofencingEvent.getErrorCode()));
           // Toast.makeText(context, "Has Error "+ geofencingEvent.getErrorCode(), Toast.LENGTH_SHORT).show();
            return;
        }
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT){
            List<Geofence> triggeredGeofences = geofencingEvent.getTriggeringGeofences();
            mGeofenceDetails = getGeofenceTransitionDetails(geofenceTransition,triggeredGeofences);
            Log.v(TAG,"Geofence Details: " + triggeredGeofences.size()+" "+ mGeofenceDetails);
        }

        Log.v(TAG,Integer.toString(geofenceTransition)+" "+Integer.toString(Geofence.GEOFENCE_TRANSITION_ENTER));

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
           // Toast.makeText(context, "Geofence Entered", Toast.LENGTH_SHORT).show();
           // List<Geofence> triggeredGeofences = geofencingEvent.getTriggeringGeofences();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(context.getString(R.string.pref_geofence_entered), mGeofenceDetails);
            editor.commit();
              //
            Log.v(TAG, "Event Entered: "+mGeofenceDetails);
            //setRingerMode(context, AudioManager.RINGER_MODE_SILENT);
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
          //  Toast.makeText(context, "Geofence Exited", Toast.LENGTH_SHORT).show();
            //setRingerMode(context, AudioManager.RINGER_MODE_NORMAL);
        } else {
            // Log the error.
           // Log.e(TAG, String.format("Unknown transition : %d", geofenceTransition));
            // No need to do anything else
            return;
        }
    }

    private String getGeofenceTransitionDetails(int geofenceTransition, List<Geofence> triggeringGeofences){
        String geofenceTransitionString = getTransitionString(geofenceTransition);

        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        for(Geofence geofence : triggeringGeofences){
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(",", triggeringGeofencesIdsList);

        return geofenceTransitionString+ ": "+ triggeringGeofencesIdsString;
    }


    private String getTransitionString(int transitionType){
        switch(transitionType){
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "Event Entered";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "Event Exited";
            default:
                return "Unknown Transition";
        }
    }
    //            List<Geofence> triggeredGeofences = geofencingEvent.getTriggeringGeofences();

}
