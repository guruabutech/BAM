package crescentcitydevelopment.com.bam;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;

import static crescentcitydevelopment.com.bam.R.id.fabLayout;


public class AddEventActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener, ChildEventListener {
    private final static String TAG = AddEventActivity.class.getSimpleName();
    private boolean hasEventCount;
    private final String LOG_TAG = "BAM TAG";
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mEventDatabaseReference;
    private String lat, lng, mUsername, mUserEmail, mUserId;
    private Menu menu;
    private MenuItem invite;
    private EditText eventName, eventDesc;
    private long mSelectedHours;
    private LatLng mPlacePickerLatLng, pickerCurrentLocation;
    private float mSelectedRadius= 50;
    private Spinner eventTimeSpinner, eventRadiusSpinner;
    private TextView mAddEventLocationView;
    private GoogleMap mAddEventMap;
    boolean mapReady = false;
    private GoogleApiClient mGoogleApiClient;
    private Geofencing mGeofencing;
    private double mLat, mLng;
    private boolean privateEvent = false;
    private LinearLayout locationLayout;
    private List<User> mPrivateInvites;
    private ArrayList<Event> mEvents;

    int PLACE_PICKER_REQUEST = 1;
    private long eventCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        locationLayout = (LinearLayout) findViewById(R.id.locationLinearLayout);
        mPrivateInvites = new ArrayList<>();
        mEvents = new ArrayList<>();
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        hasEventCount = false;

        if(bd != null){
            lat = (String) bd.get("lat");
             lng = (String) bd.get("long");
            mUsername = (String) bd.get("userName");
            mUserEmail = (String) bd.get("userEmail");
            mUserId = (String) bd.get("userId");

        }

        mLat = Double.parseDouble(lat);
        mLng = Double.parseDouble(lng);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .enableAutoManage(this,this)
                .addOnConnectionFailedListener(this)
                .build();
        mGeofencing = new Geofencing(this, mGoogleApiClient);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.eventMap);
        mapFragment.getMapAsync(this);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mEventDatabaseReference = mFirebaseDatabase.getReference();
        mEventDatabaseReference.addChildEventListener(this);
        eventTimeSpinner = (Spinner) findViewById(R.id.eventLengthSpinner);
        eventRadiusSpinner = (Spinner) findViewById(R.id.eventRadiusSpinner);
        ArrayAdapter<CharSequence> durationSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.time_array, R.layout.spinner_item);
        eventTimeSpinner.setAdapter(durationSpinnerAdapter);
        ArrayAdapter<CharSequence> radiusSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.radius_array, R.layout.spinner_item);
        eventRadiusSpinner.setAdapter(radiusSpinnerAdapter);
        locationLayout.setOnClickListener(this);
        eventTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mSelectedHours = adapterView.getSelectedItemPosition() + 1;
                mSelectedHours = mSelectedHours * 60 * 60 * 1000;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mSelectedHours =  60 * 60 * 1000;
            }
        });
        eventRadiusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int selection;
                selection = adapterView.getSelectedItemPosition() ;
                switch(selection){
                    case 1:
                        mSelectedRadius = 10;
                        updateMap();
                        break;
                    case 2:
                        mSelectedRadius = 15;
                        updateMap();
                        break;
                    case 3:
                        mSelectedRadius = 20;
                        updateMap();
                        break;
                    case 4:
                        mSelectedRadius = 40;
                        updateMap();
                        break;
                    case 5:
                        mSelectedRadius = 50;
                        updateMap();
                        break;
                    default:
                        mSelectedRadius = 50;
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                    mSelectedRadius = 7;
            }
        });

        eventName = (EditText) findViewById(R.id.eventNameField);
        eventDesc = (EditText) findViewById(R.id.eventDescriptionField);
        eventTimeSpinner = (Spinner) findViewById(R.id.eventLengthSpinner);
        mAddEventLocationView = (TextView) findViewById(R.id.addEventLocation);
        secondQuery();
    }

    public void showAlert() {
        AlertDialog.Builder infoDialog = new AlertDialog.Builder(this);
        infoDialog.setMessage("Are you sure? This event will not be saved.")
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("CONTINUE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                }).create();
        infoDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        if(requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK){
            Place place = PlacePicker.getPlace(this, data);
            String placeAddress;


            if(place == null){
                return;
            }else {
                mAddEventMap.clear();
                placeAddress = place.getAddress().toString();
                mAddEventLocationView.setText(placeAddress);
                pickerCurrentLocation = place.getLatLng();
                mLat = pickerCurrentLocation.latitude;
                mLng = pickerCurrentLocation.longitude;
                mPlacePickerLatLng = place.getLatLng();
                mAddEventMap.addMarker(new MarkerOptions().position(pickerCurrentLocation).title("Event"));
                CameraPosition target = CameraPosition.builder().target(pickerCurrentLocation).zoom(17).build();
                mAddEventMap.moveCamera(CameraUpdateFactory.newCameraPosition(target));
                mAddEventMap.addCircle(new CircleOptions()
                        .center(pickerCurrentLocation)
                        .radius(mSelectedRadius)
                        .strokeColor(Color.GREEN)
                        .fillColor(Color.argb(64,0,255,0)));
                mAddEventMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.event_menu, menu);
        this.menu = menu;
        invite = menu.findItem(R.id.action_private);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        if (id == R.id.action_cancel) {
            showAlert();
            return true;
        }
        if (id == R.id.action_save) {
            if (eventName.getText().toString().trim().isEmpty() || eventDesc.getText().toString().trim().isEmpty()|| mSelectedRadius == 7) {
                if (eventName.getText().toString().trim().isEmpty()) {
                    eventName.setText("");
                    eventName.setHintTextColor(getResources().getColor(R.color.colorRed));
                }
                if (eventDesc.getText().toString().trim().isEmpty()) {
                    eventDesc.setText("");
                    eventDesc.setHintTextColor(getResources().getColor(R.color.colorRed));
                }
                Toast.makeText(AddEventActivity.this, "Form Incomplete", Toast.LENGTH_SHORT).show();
            } else {
                List<User>attendees = new ArrayList<>();
                User attendee1 = new User(mUsername, mUserEmail, mUserId);
                mPrivateInvites.add(attendee1);
                attendees.add(attendee1);
                Event event = new Event(eventName.getText().toString(), eventDesc.getText().toString(),mLat, mLng, mSelectedHours, mSelectedRadius, attendees, attendee1, privateEvent, mPrivateInvites, System.currentTimeMillis());
                Event eventGeofence = new Event(event.getLatitude(), event.getLongitude(), event.getRadius(), event.getEventHours(), event.getTimeStamp());
                mEventDatabaseReference.push().setValue(event);

                mEvents.add(eventGeofence);
                mGeofencing.updateGeofencesList(mEvents);
                mGeofencing.registerAllGeofences();
                finish();
            }
            return true;
        }
        if(id == R.id.action_private){

            Snackbar.make(findViewById(R.id.addEventLayout), "Event Status: Private", Snackbar.LENGTH_LONG).show();
            return true;
        }
        if(id == R.id.action_private_event){
            privateEvent = true;
           if(!invite.isVisible()){
               invite.setVisible(true);
           }
            return true;
        }
        if(id == R.id.action_public_event){
            privateEvent = false;
            if(invite.isVisible()){
                invite.setVisible(false);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapReady = true;
        mAddEventMap = googleMap;
        LatLng location = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        mPlacePickerLatLng = location;
        mAddEventMap.addMarker(new MarkerOptions().position(location).title("Event"));
        LatLng friscoTx = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        CameraPosition target = CameraPosition.builder().target(friscoTx).zoom(17).build();
        mAddEventMap.moveCamera(CameraUpdateFactory.newCameraPosition(target));
        mAddEventMap.addCircle(new CircleOptions()
        .center(location)
        .radius(mSelectedRadius)
        .strokeColor(Color.GREEN)
        .fillColor(Color.argb(64,0,255,0)));
        mAddEventMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    //    mLocationRequest = LocationRequest.create();
    //    mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    //    mLocationRequest.setInterval(10000);
    //    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
    //        return;
    //    }
    //    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {


    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onBackPressed(){
        showAlert();

    }

    public void updateMap(){
        mAddEventMap.clear();
        mAddEventMap.addMarker(new MarkerOptions().position(mPlacePickerLatLng).title("Event"));
        CameraPosition target = CameraPosition.builder().target(mPlacePickerLatLng).zoom(17).build();
        mAddEventMap.moveCamera(CameraUpdateFactory.newCameraPosition(target));
        mAddEventMap.addCircle(new CircleOptions()
                .center(mPlacePickerLatLng)
                .radius(mSelectedRadius)
                .strokeColor(Color.GREEN)
                .fillColor(Color.argb(64,0,255,0)));
        mAddEventMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

    @Override
    public void onClick(View view) {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        Intent intent;
        try {
              intent = builder.build(this);
            startActivityForResult(intent, PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e("Places Error ", e.getMessage());
        } catch (Exception e) {
            Log.e("Places Error ", e.getMessage());
        }
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        if(!hasEventCount) {
            if (dataSnapshot.getKey().equals("events")) {
                eventCount = dataSnapshot.getChildrenCount();
            } else {
                Event newEvent = dataSnapshot.getValue(Event.class);
                Event geofenceParam = new Event(newEvent.getLatitude(), newEvent.getLongitude(), newEvent.getRadius(), newEvent.getEventHours(), newEvent.getTimeStamp());
                mEvents.add(geofenceParam);
                if (mEvents.size() >= eventCount) {
                    hasEventCount = true;
                }
            }
        }

    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
    public void query(){

        mEventDatabaseReference = mFirebaseDatabase.getReference().child("events");
                mEventDatabaseReference.addChildEventListener(this);

    }
    private void secondQuery(){
        new CountDownTimer(300, 1000){

            public void onTick(long millisUntilFinished){

            }

            @Override
            public void onFinish() {
                query();
            }
        }.start();
    }
}
