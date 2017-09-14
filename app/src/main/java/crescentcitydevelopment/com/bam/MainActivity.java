package crescentcitydevelopment.com.bam;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import java.util.Arrays;
public class MainActivity extends AppCompatActivity {
    private Typeface script;
    private static final String ANONYMOUS = "anonymous";
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private static final int RC_SIGN_IN = 123;
    private SignInButton signInButton;
    private CardView cardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String mUserName = ANONYMOUS;
        mFirebaseAuth = FirebaseAuth.getInstance();
        signInButton = (SignInButton) findViewById(R.id.signInButton);
        signInButton.setColorScheme(SignInButton.COLOR_DARK);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        cardView = (CardView) findViewById(R.id.signInCard);
        cardView.setVisibility(View.INVISIBLE);
        TextView logo = (TextView) findViewById(R.id.logoScript);
        script = Typeface.createFromAsset(this.getAssets(), "Tangerine_Bold.ttf");
        logo.setTypeface(script);
        logo.setText("Biometric Attendance Manager");

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    Intent intent = new Intent(MainActivity.this, EventsActivity.class);
                    startActivity(intent);
                }
            }
        };
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){
                            //user signed in
                        }else{
                            //user signed out
                            startActivity(
                                    AuthUI.getInstance()
                                            .createSignInIntentBuilder()
                                            .setIsSmartLockEnabled(false)
                                            .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                            .build());
                        }
                }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                Toast.makeText(this, "Sign in successful", Toast.LENGTH_SHORT).show();
            }else if(resultCode == RESULT_CANCELED ){
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume(){
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        cardIn();
        super.onResume();
    }

    private void cardIn(){
        new CountDownTimer(300, 1000){

            public void onTick(long millisUntilFinished){

            }

            @Override
            public void onFinish() {
                Slide slide = new Slide(Gravity.BOTTOM);
                TransitionManager.beginDelayedTransition(cardView, slide);
                cardView.setVisibility(View.VISIBLE);
            }
        }
        .start();
    }

    private void cardOut(){
        Slide slide = new Slide(Gravity.BOTTOM);
        TransitionManager.beginDelayedTransition(cardView, slide);
        cardView.setVisibility(View.INVISIBLE);
    }
    @Override
    protected void onPause(){
        super.onPause();
        cardOut();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }
    @Override
    protected void onStop(){
        super.onStop();
    }
}

