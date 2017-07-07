package crescentcitydevelopment.com.bam;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class EventsActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ChildEventListener {
    private static final int REQUEST_CHECK_SETTINGS = 1;
    private int mLocationStatus;
    private final String LOG_TAG = "BAM TAG";
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mEventDatabaseReference;
    private EventAdapter mEventAdapter;
    private ListView mEventListView;
    private TextView mNoEvents;
    private ProgressBar progressBar;
    boolean mIsLargeLayout;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double mLat;
    private double mLng;
    private String mUserId;
    List<Event> mEvents;
    FirebaseAuth mFirebaseAuth;
    FirebaseAuth.AuthStateListener mAuthStateListener;
    private String mUserName;
    private String mUserEmail;
    public static final int REQUEST_LOCATION = 99;
    private int attendeeCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);
        mEventListView = (ListView) findViewById(R.id.eventList);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        mNoEvents = (TextView) findViewById(R.id.noEvents);
        mNoEvents.setVisibility(View.GONE);
        mEventListView.setEmptyView(progressBar);
        mLocationStatus = 777;
        mEvents = new ArrayList<>();
        mEventAdapter = new EventAdapter(this, R.layout.event_list_item, mEvents);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    finish();
                } else {
                    mUserName = user.getDisplayName();
                    mUserEmail = user.getEmail();
                    mUserId = user.getUid();

                }
            }
        };
        mEventListView.setAdapter(mEventAdapter);
        mEventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Event event = mEventAdapter.getItem(i);
                String location;
                location = locationToAddress(event.getLatitude(),event.getLongitude());
                Intent intent = new Intent(EventsActivity.this, EventDetailActivity.class)
                        .putExtra("title", event.getEventName())
                        .putExtra("desc", event.getEventDescription())
                        .putExtra("duration", event.getEventHours())
                        .putExtra("key", event.getKey().toString())
                        .putExtra("admin", event.getAdmin().getUserName())
                        .putExtra("attendeeCount", attendeeCount)
                        .putExtra("eventAddress", location)
                        .putExtra("eventLatitude", event.getLatitude())
                        .putExtra("eventLongitude", event.getLongitude())
                        .putExtra("userName", mUserName)
                        .putExtra("userEmail", mUserEmail)
                        .putExtra("userId", mUserId)
                        .putExtra("eventName",event.getEventName());

                        startActivity(intent);
            }
        });
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (progressBar.isShown() && mLocationStatus == 6) {
                    Snackbar.make(view, "Configuring Location", Snackbar.LENGTH_LONG).show();
                } else if(mLat == 0 || mLng == 0) {
                    configureLocation();
                    Snackbar.make(view, "Location Denied... Check Permissions", Snackbar.LENGTH_LONG).show();
                }else{
                    Log.v("LOCATION STATUS---", Double.toString(mLat) + Double.toString(mLng));
                    Intent intent = new Intent(EventsActivity.this, AddEventActivity.class)
                            .putExtra("lat", Double.toString(mLat))
                            .putExtra("long", Double.toString(mLng))
                            .putExtra("userName", mUserName)
                            .putExtra("userEmail", mUserEmail)
                            .putExtra("userId", mUserId);
                            startActivity(intent);
                }
            }
        });
        mEventDatabaseReference = mFirebaseDatabase.getReference().child("events");
        mEventDatabaseReference.addChildEventListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sign_out) {
            AuthUI.getInstance().signOut(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        if (mLocationStatus == 6) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        mLocationStatus = 777;
        super.onStop();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);
        //Log.v(LOG_TAG, "CONNECTED-----------CONNECTED");
        configureLocation();

    }

    public String locationToAddress(double lat, double lng){
        Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> addresses = null;
        String editedAddress = null;
        try {
            addresses = gcd.getFromLocation(lat, lng, 1);
            String address = addresses.get(0).getAddressLine(0);
            editedAddress = address.substring(address.indexOf(",")+1);
           // Log.v("DEBUG GEO","---"+addresses.get(0).getAddressLine(0));
            //Log.v("EDITED ADDRESS","---"+editedAddress);

        } catch (IOException e) {
            e.printStackTrace();
            Log.v("GEOCODER ERROR","GEOCODER ERROR");
        }
        if (addresses.size() > 0)
        {
            Log.v("LOCATION ADDRESS","ADD "+addresses.get(0).getAddressLine(1));
        }
        return editedAddress;
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.v("CONNECTION SUSPENDED", "SUSPENDED");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        showAlert();
        Log.v("CONNECTION FAILED", "FAILED");

    }

    @Override
    public void onLocationChanged(Location location) {
        if (progressBar.isShown() && mLocationStatus == 6) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Location Configured", Toast.LENGTH_SHORT).show();
            mLocationStatus = 777;
            Log.v(LOG_TAG, "LOCATION CHANGED---");
        }
        mLat = location.getLatitude();
        mLng = location.getLongitude();

        Log.v(LOG_TAG, "LOCATION CHANGED---");
        Log.v(LOG_TAG, mLat+" "+mLng);
    }

    public void showAlert() {
        AlertDialog.Builder infoDialog = new AlertDialog.Builder(this);
        infoDialog.setMessage("Must enable location to continue.")
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                }).setTitle("WARNING")
                .create();
        infoDialog.show();

    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Event newEvent = dataSnapshot.getValue(Event.class);
        newEvent.setKey(dataSnapshot.getKey());
        attendeeCount = newEvent.getAttendees().size();
        progressBar.setVisibility(View.GONE);
        mEventAdapter.add(newEvent);
        if (mEventAdapter.getCount() == 0) {
            progressBar.setVisibility(View.GONE);
            mEventListView.setEmptyView(mNoEvents);
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        mEventAdapter.notifyDataSetChanged();
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

        for (Event events : mEvents) {
            if (events.getKey().toString().equals(dataSnapshot.getKey().toString())) {
                mEvents.remove(events);
                break;
            }
        }
        mEventAdapter.notifyDataSetChanged();
        mEventAdapter = new EventAdapter(this, R.layout.event_list_item, mEvents);
        mEventListView.setAdapter(mEventAdapter);
        if (mEventAdapter.getCount() == 0) {
            progressBar.setVisibility(View.GONE);
            mEventListView.setEmptyView(mNoEvents);
        }
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        mEventAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        mEventAdapter.notifyDataSetChanged();
    }

    public void configureLocation(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
            return;
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            Log.v(LOG_TAG, "LOCATION FUSED");
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.v(LOG_TAG, "PERMISSION RESULTS---");

        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We can now safely use the API we requested access to
                Log.v(LOG_TAG, "PERMISSION GRANTED---");

                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                        .addLocationRequest(mLocationRequest);
                PendingResult<LocationSettingsResult> result =
                        LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                                builder.build());
                result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

                    @Override
                    public void onResult(@NonNull LocationSettingsResult result) {
                        final Status status = result.getStatus();
                        final LocationSettingsStates states = result.getLocationSettingsStates();
                        switch (status.getStatusCode()) {
                            case LocationSettingsStatusCodes.SUCCESS:
                                // All location settings are satisfied. The client can
                                // initialize location requests here.
                                Log.v("SUCCESS---SUCCESS", Integer.toString(mLocationStatus));

                                break;
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                // Location settings are not satisfied, but this can be fixed
                                // by showing the user a dialog.
                                try {
                                    // Show the dialog by calling startResolutionForResult(),
                                    // and check the result in onActivityResult().

                                    status.startResolutionForResult(EventsActivity.this, REQUEST_CHECK_SETTINGS);
                                    mLocationStatus = LocationSettingsStatusCodes.RESOLUTION_REQUIRED;
                                    Log.v("RESOLUTION REQUIRED", Integer.toString(mLocationStatus));

                                } catch (IntentSender.SendIntentException e) {
                                    // Ignore the error.
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                // Location settings are not satisfied. However, we have no way
                                // to fix the settings so we won't show the dialog.g
                                Log.v("CHANGE UNAVAILABLE", Integer.toString(mLocationStatus));
                                break;
                        }
                    }
                });
                                configureLocation();
            }
        }
    }
}
