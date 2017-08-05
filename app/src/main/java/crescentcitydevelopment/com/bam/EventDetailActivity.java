package crescentcitydevelopment.com.bam;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class EventDetailActivity extends AppCompatActivity implements OnMapReadyCallback, ChildEventListener, InviteDialogFragment.InviteDialogListener{
    GoogleMap mAddEventMap;
    boolean mapReady = false;
    TextView eventDescriptionView;
    TextView eventTitleView;
    TextView eventDuration;
    TextView eventAdminView;
    TextView eventAttendeesView;
    TextView eventLocationView;
    private double eventLatitude;
    private double eventLongitude;
    private int attendeeCount;
    private Boolean isAttendable;
    private String eventKey;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mEventDatabaseReference;
    private String mKey;
    private LinearLayout mAdminLayout;
    private LinearLayout mAttendeeLayout;
    private LinearLayout mLocationLayout;
    private LinearLayout mDurationLayout;
    private String mAdminName;
    private String mAdminEmail;
    private String mLocation;
    private String mDuration;
    private String mEventName;
    private String mUsername;
    private String mUserEmail;
    private String mUserId;
    private String mEventDesc;
    private List<User> mAttendeeList, mInvitedUsersList;
    private String[] mAttendeeNameArray;
    private String[] mInvitedUsersArray;
    private FloatingActionButton fab;
    private ImageView backIcon;
    private ImageView deleteIcon;
    private LinearLayout fabLayout;
    private User mCurrentUser;
    private Boolean eventIsPrivate;
    private String mTimeStamp;

    //FINGERPRINT VARIABLES
    private KeyGenerator mKeyGenerator;
    static final String DEFAULT_KEY_NAME = "default_key";
    private KeyStore mKeyStore;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String DIALOG_FRAGMENT_TAG = "myFragment";
    private static final String SECRET_MESSAGE = "Very secret message";
    private static final String KEY_NAME_NOT_INVALIDATED = "key_not_invalidated";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String geofenceList = sharedPreferences.getString(getString(R.string.pref_geofence_entered), "none");
        fabLayout = (LinearLayout) findViewById(R.id.fabLayout);
        backIcon = (ImageView) findViewById(R.id.backArrowIcon);
        deleteIcon = (ImageView) findViewById(R.id.deleteIcon);
        eventDescriptionView = (TextView) findViewById(R.id.detailEventDescription);
        eventAdminView = (TextView) findViewById(R.id.detailEventAdmin);
        eventTitleView = (TextView) findViewById(R.id.detailEventTitle);
        eventLocationView = (TextView) findViewById(R.id.detailEventLocation);
        eventDuration = (TextView) findViewById(R.id.detailEventDuration);
        eventAttendeesView = (TextView) findViewById(R.id.detailEventAttendees);
        mAdminLayout = (LinearLayout) findViewById(R.id.adminLayout);
        mAttendeeLayout = (LinearLayout) findViewById(R.id.attendeeLayout);
        mLocationLayout = (LinearLayout) findViewById(R.id.locationLayout);
        mDurationLayout = (LinearLayout) findViewById(R.id.durationLayout);

        fab = (FloatingActionButton) findViewById(R.id.addAttendee);
        Intent intent = getIntent();
        Bundle bd =intent.getExtras();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mAttendeeList = new ArrayList<>();
        mInvitedUsersList = new ArrayList<>();
        if(bd != null) {
            mKey = (String) bd.get("key");
            long duration = (long) bd.get("duration");
            String hours = Long.toString(TimeUnit.MILLISECONDS.toHours(duration))+" Hours";
            String title = (String) bd.get("title");
            mUsername = (String) bd.get("userName");
            mUserEmail = (String) bd.get("userEmail");
            mUserId = (String) bd.get("userId");
            mTimeStamp = (String) bd.get("timeStamp");
            mEventDesc = (String) bd.get("desc");
            String admin = (String) bd.get("admin");
            mLocation = (String) bd.get("eventAddress");
            attendeeCount = (int) bd.get("attendeeCount");
            eventKey = (String) bd.get("key");
            eventLatitude = (Double) bd.get("eventLatitude");
            eventLongitude = (Double) bd.get("eventLongitude");
            mEventName = (String) bd.get("eventName");
            eventTitleView.setText(title);
            eventDescriptionView.setText(mEventDesc);
            eventDuration.setText(hours);
            eventAdminView.setText(admin);
            eventAttendeesView.setText(attendeeCount +" "+"Attendees");
            eventLocationView.setText(mLocation);
        }
        isAttendable = geofenceList.contains(mTimeStamp);
        mCurrentUser = new User(mUsername, mUserEmail,mUserId);

        deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEventDatabaseReference = mFirebaseDatabase.getReference().child("events").child(mKey);
                mEventDatabaseReference.removeValue();
                finish();
            }
        });
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        fab.setImageDrawable(getDrawable(R.drawable.attend_icon));


        mAdminLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlert("EVENT ADMIN",mAdminName+"\n\n"+mAdminEmail);
            }
        });
        mAttendeeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                   showAttendeesDialog();
            }
        });
        mLocationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlert("EVENT LOCATION", mLocation);
            }
        });
        mDurationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlert("EVENT DURATION", "This event is "+mDuration+" hour(s) long.");
            }
        });

      //  mGoogleApiClient = new GoogleApiClient.Builder(this)
      //          .addApi(LocationServices.API)
      //          .addConnectionCallbacks(this)
      //          .addOnConnectionFailedListener(this)
      //          .build();
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.eventDetailsMap);
        mapFragment.getMapAsync(this);
        mEventDatabaseReference = mFirebaseDatabase.getReference().child("events").child(mKey);
        mEventDatabaseReference.addChildEventListener(this);

        /* onCreate FINGERPRINT */
        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (KeyStoreException e) {
            throw new RuntimeException("Failed to get an instance of KeyStore", e);
        }
        try {
            mKeyGenerator = KeyGenerator
                    .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to get an instance of KeyGenerator", e);
        }
        Cipher defaultCipher;
        Cipher cipherNotInvalidated;
        try {
            defaultCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            cipherNotInvalidated = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get an instance of Cipher", e);
        }
        KeyguardManager keyguardManager = getSystemService(KeyguardManager.class);
        FingerprintManager fingerprintManager = getSystemService(FingerprintManager.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                fab.setOnClickListener(
                        new AttendeeButtonListener(cipherNotInvalidated,
                                KEY_NAME_NOT_INVALIDATED));

        } else {
            // Hide the purchase button which uses a non-invalidated key
            // if the app doesn't work on Android N preview
           // purchaseButtonNotInvalidated.setVisibility(View.GONE);
           // findViewById(R.id.purchase_button_not_invalidated_description)
           //         .setVisibility(View.GONE);
        }

        if (!keyguardManager.isKeyguardSecure()) {
            // Show a message that the user hasn't set up a fingerprint or lock screen.
            Toast.makeText(this,
                    "Secure lock screen hasn't set up.\n"
                            + "Go to 'Settings -> Security -> Fingerprint' to set up a fingerprint",
                    Toast.LENGTH_LONG).show();

            return;
        }

        // Now the protection level of USE_FINGERPRINT permission is normal instead of dangerous.
        // See http://developer.android.com/reference/android/Manifest.permission.html#USE_FINGERPRINT
        // The line below prevents the false positive inspection from Android Studio
        // noinspection ResourceType
        if (!fingerprintManager.hasEnrolledFingerprints()) {
            // This happens when no fingerprints are registered.
            Toast.makeText(this,
                    "Go to 'Settings -> Security -> Fingerprint' and register at least one fingerprint",
                    Toast.LENGTH_LONG).show();
            return;
        }
        createKey(DEFAULT_KEY_NAME, true);
        createKey(KEY_NAME_NOT_INVALIDATED, false);
        //purchaseButton.setEnabled(true);

            fab.setOnClickListener(
                    new AttendeeButtonListener(defaultCipher, DEFAULT_KEY_NAME));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapReady = true;
        mAddEventMap = googleMap;

        LatLng location = new LatLng(eventLatitude, eventLongitude);
        mAddEventMap.addMarker(new MarkerOptions().position(location).title("Event"));
        //LatLng friscoTx = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        CameraPosition target = CameraPosition.builder().target(location).zoom(17).build();
        mAddEventMap.moveCamera(CameraUpdateFactory.newCameraPosition(target));
        mAddEventMap.addCircle(new CircleOptions()
                .center(location)
                .radius(50)
                .strokeColor(Color.GREEN)
                .fillColor(Color.argb(64,0,255,0)));
        mAddEventMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(isAdmin(mAdminEmail)){
            getMenuInflater().inflate(R.menu.detail_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete) {

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void showAlert(String heading, String title) {
        AlertDialog.Builder infoDialog = new AlertDialog.Builder(this);
        infoDialog.setMessage(title)
                .setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setTitle(heading)
                .create();
        infoDialog.show();
    }
    public Dialog showAttendeesDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("EVENT ATTENDEES")
                .setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setItems(mAttendeeNameArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        return builder.show();
    }
    public Dialog showInvitedUsersDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("INVITE USERS")
                .setNegativeButton("DISMISS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Add User", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        DialogFragment inviteDialog = new InviteDialogFragment();
                        inviteDialog.show(getSupportFragmentManager(), "InviteDialogFragment");
                    }
                })
                .setItems(mInvitedUsersArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        return builder.show();
    }
    public void rotateVector(){
        AnimatedVectorDrawable crossToTick = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_attend_to_attendee);
        fab.setImageDrawable(crossToTick);
        crossToTick.start();
        crossToTick.setAlpha(255);
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        if(dataSnapshot.getKey().equals("admin")){
            mAdminName  = dataSnapshot.child("userName").getValue().toString();
           mAdminEmail = dataSnapshot.child("emailAddress").getValue().toString();
        }


            if(dataSnapshot.getKey().equals("eventHours")){
            mDuration = dataSnapshot.getValue().toString();
        }
        if(dataSnapshot.getKey().equals("attendees")){
            Long.toString(dataSnapshot.getChildrenCount());
            int childCount = (int) dataSnapshot.getChildrenCount();
            mAttendeeNameArray = new String[childCount];
            for(int i = 0; i < dataSnapshot.getChildrenCount(); i++ )
            {
                String attendeeNumber = Integer.toString(i);
                String attendeeName =dataSnapshot.child(attendeeNumber).child("userName").getValue().toString();
                String attendeeEmail =dataSnapshot.child(attendeeNumber).child("emailAddress").getValue().toString();
                String attendeeUserId =dataSnapshot.child(attendeeNumber).child("userId").getValue().toString();
                User user = new User(attendeeName, attendeeEmail, attendeeUserId);
                mAttendeeList.add(user);
                mAttendeeNameArray[i] = attendeeName;
            }
            eventAttendeesView.setText(dataSnapshot.getChildrenCount()+" "+"Attendees");
        }
        if(dataSnapshot.getKey().equals("privateInvites")){
            Long.toString(dataSnapshot.getChildrenCount());
            int childCount = (int) dataSnapshot.getChildrenCount();
            mInvitedUsersArray = new String[childCount];
            for(int i = 0; i < dataSnapshot.getChildrenCount(); i++ )
            {
                String inviteNumber = Integer.toString(i);
              //  String inviteeName =dataSnapshot.child(inviteNumber).child("userName").getValue().toString();
                String inviteeEmail =dataSnapshot.child(inviteNumber).child("emailAddress").getValue().toString();
              //  String inviteeUserId =dataSnapshot.child(inviteNumber).child("userId").getValue().toString();
                User invitees = new User("Invited User", inviteeEmail, "UserId");
                mInvitedUsersList.add(invitees);
                mInvitedUsersArray[i] = inviteeEmail;
            }
          //  eventAttendeesView.setText(dataSnapshot.getChildrenCount()+" "+"Attendees");
        }
        if(isPresent(mCurrentUser) && !isAdmin(mAdminEmail.toLowerCase())){
            fab.setVisibility(View.INVISIBLE);
        }
        if(!isAdmin(mAdminEmail)){
            deleteIcon.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        if(dataSnapshot.getKey().equals("attendees")){
            int childCount = (int) dataSnapshot.getChildrenCount();
            mAttendeeNameArray = new String[childCount];
            for(int i = 0; i < dataSnapshot.getChildrenCount(); i++ )
            {
                String attendeeNumber = Integer.toString(i);
                String attendeeName =dataSnapshot.child(attendeeNumber).child("userName").getValue().toString();
                mAttendeeNameArray[i] = attendeeName;
            }
                eventAttendeesView.setText(dataSnapshot.getChildrenCount()+" "+"Attendees");
        }
        if(dataSnapshot.getKey().equals("privateInvites")){
            int childCount = (int) dataSnapshot.getChildrenCount();
            mInvitedUsersArray = new String[childCount];
            for(int i = 0; i < dataSnapshot.getChildrenCount(); i++ )
            {
                String attendeeNumber = Integer.toString(i);
                //String attendeeName =dataSnapshot.child(attendeeNumber).child("userName").getValue().toString();
                String attendeeEmail =dataSnapshot.child(attendeeNumber).child("emailAddress").getValue().toString();
                //String attendeeUserId =dataSnapshot.child(attendeeNumber).child("userId").getValue().toString();
                mInvitedUsersArray[i] = attendeeEmail;
            }

        }


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

    private Boolean isPresent(User user){
        String userId = user.getUserId();
        for(int i = 0; i < mAttendeeList.size(); i++){
            if(mAttendeeList.get(i).getUserId().equals(userId)){
                return true;
            }
        }
        return false;
    }

    private Boolean isAdmin(String adminEmail){

           return adminEmail.equals(mUserEmail);
    }
    public void fabOut(){
        new CountDownTimer(300, 1000){

            public void onTick(long millisUntilFinished){

            }

            @Override
            public void onFinish() {
                Slide slide = new Slide(Gravity.RIGHT);
                TransitionManager.beginDelayedTransition(fabLayout, slide);
                fabLayout.setVisibility(View.INVISIBLE);
            }
        }.start();
    }


    /*
     * FINGERPRINT METHODS
     */
    public void createKey(String keyName, boolean invalidatedByBiometricEnrollment) {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.
        try {
            mKeyStore.load(null);
            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder

            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(keyName,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    // Require the user to authenticate with a fingerprint to authorize every use
                    // of the key
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);

            // This is a workaround to avoid crashes on devices whose API level is < 24
            // because KeyGenParameterSpec.Builder#setInvalidatedByBiometricEnrollment is only
            // visible on API level +24.
            // Ideally there should be a compat library for KeyGenParameterSpec.Builder but
            // which isn't available yet.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment);
            }
            mKeyGenerator.init(builder.build());
            mKeyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    private boolean initCipher(Cipher cipher, String keyName) {
        try {
            mKeyStore.load(null);
            SecretKey key = (SecretKey) mKeyStore.getKey(keyName, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }
    public void onPurchased(boolean withFingerprint,
                            @Nullable FingerprintManager.CryptoObject cryptoObject) {
        if (withFingerprint) {
            // If the user has authenticated with fingerprint, verify that using cryptography and
            // then show the confirmation message.
            assert cryptoObject != null;
            tryEncrypt(cryptoObject.getCipher());
        } else {
            // Authentication happened with backup password. Just show the confirmation message.
            showConfirmation(null);
        }
    }

    private void tryEncrypt(Cipher cipher) {
        try {
            byte[] encrypted = cipher.doFinal(SECRET_MESSAGE.getBytes());
            showConfirmation(encrypted);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            Toast.makeText(this, "Failed to encrypt the data with the generated key. "
                    + "Retry the purchase", Toast.LENGTH_LONG).show();
        }
    }

    private void showConfirmation(byte[] encrypted) {
        if (encrypted != null) {
            if(isAttendable) {
                mAttendeeList.add(mCurrentUser);
                mEventDatabaseReference = mFirebaseDatabase.getReference().child("events").child(mKey).child("attendees");
                mEventDatabaseReference.setValue(mAttendeeList);
                fabOut();
                Toast.makeText(this, "Attendance Documented", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Not Within Event Radius", Toast.LENGTH_SHORT).show();

            }
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        EditText email = (EditText) dialog.getDialog().findViewById(R.id.inviteeEmailAddress);
        mEventDatabaseReference = mFirebaseDatabase.getReference().child("events").child(mKey).child("privateInvites");
        User newInvitee = new User("Invited User", email.getText().toString(), "UserId");
        mInvitedUsersList.add(newInvitee);
        mEventDatabaseReference.setValue(mInvitedUsersList);
        dialog.dismiss();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        dialog.dismiss();
    }

    private class AttendeeButtonListener implements View.OnClickListener {

        Cipher mCipher;
        String mKeyName;

        AttendeeButtonListener(Cipher cipher, String keyName) {
            mCipher = cipher;
            mKeyName = keyName;
        }

        @Override
        public void onClick(View view) {


            // Set up the crypto object for later. The object will be authenticated by use
            // of the fingerprint.
            if(isAdmin(mAdminEmail)){
                showInvitedUsersDialog();
            }else {
                rotateVector();

                if (initCipher(mCipher, mKeyName)) {
                    showFingerprintDialog();
                } else {
                    // This happens if the lock screen has been disabled or or a fingerprint got
                    // enrolled. Thus show the dialog to authenticate with their password first
                    // and ask the user if they want to authenticate with fingerprints in the
                    // future
                    FingerprintAuthenticationDialogFragment fragment
                            = new FingerprintAuthenticationDialogFragment();
                    fragment.setCryptoObject(new FingerprintManager.CryptoObject(mCipher));
                    fragment.setStage(
                            FingerprintAuthenticationDialogFragment.Stage.NEW_FINGERPRINT_ENROLLED);
                    fragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
                }
            }
        }

        private void showFingerprintDialog(){
            new CountDownTimer(300, 2000){

                public void onTick(long millisUntilFinished){

                }

                @Override
                public void onFinish() {
                    // Show the fingerprint dialog. The user has the option to use the fingerprint with
                    // crypto, or you can fall back to using a server-side verified password.
                    FingerprintAuthenticationDialogFragment fragment
                            = new FingerprintAuthenticationDialogFragment();
                    fragment.setCryptoObject(new FingerprintManager.CryptoObject(mCipher));
                    boolean useFingerprintPreference = true;
                    if (useFingerprintPreference) {
                        fragment.setStage(
                                FingerprintAuthenticationDialogFragment.Stage.FINGERPRINT);
                    } else {
                        fragment.setStage(
                                FingerprintAuthenticationDialogFragment.Stage.PASSWORD);
                    }
                    fragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
                }
            }.start();
        }


    }
}
