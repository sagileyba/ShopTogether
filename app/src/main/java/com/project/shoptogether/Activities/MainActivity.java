package com.project.shoptogether.Activities;

import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.project.shoptogether.R;
import com.squareup.picasso.Picasso;


public class MainActivity extends AppCompatActivity {
    private CallbackManager mCallbackManager;
    private FirebaseAuth mFirebaseAuth;
    private TextView textViewUser;
    private ImageView logoView;
    private LoginButton FB_Button;
    private FirebaseAuth.AuthStateListener authStateListener;
    private AccessTokenTracker accessTokenTracker;
    private static final String TAG= "FacebookAuthentication";
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private ProfileTracker mProfileTracker;
    private String UserID;
    private String UserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewUser = findViewById(R.id.user_text);
        logoView = findViewById(R.id.sign_in_logo);
        FB_Button = findViewById(R.id.login_button);
        sharedPref = getSharedPreferences("FacebookImage", MODE_PRIVATE);
        editor = sharedPref.edit();
        mFirebaseAuth = FirebaseAuth.getInstance();
    mCallbackManager= CallbackManager.Factory.create();


        FB_Button.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
/*
                if(Profile.getCurrentProfile() == null) {
                    mProfileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                            Log.v("facebook - profile", currentProfile.getFirstName());
                            mProfileTracker.stopTracking();
                        }
                    };
                    // no need to call startTracking() on mProfileTracker
                    // because it is called by its constructor, internally.
                }
                else {
                    Profile profile = Profile.getCurrentProfile();

                }*/
                UserID = loginResult.getAccessToken().getUserId();
                editor.putString(UserEmail,UserID);
                editor.apply();
                Log.i(TAG,"onSuccess"+loginResult);
                handleFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.i(TAG,"onCancel");
                LoginManager.getInstance().logOut();
                Log.i("userr",  "onCancel");

                updateUI(null);

            }

            @Override
            public void onError(FacebookException error) {
                Log.i(TAG,"onError"+ error);

            }
        });
        authStateListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null){
                        UserEmail=firebaseUser.getEmail();
                        Log.i("email",UserEmail);
                    Log.i("userr",  "onauthStateChanged");

                    updateUI(firebaseUser);
                }
                else {
                    Log.i("userr",  "onauthStateChangednull");

                    updateUI(null);
                }
            }
        };
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if(currentAccessToken == null)
                    mFirebaseAuth.signOut();
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if you don't add following block,
        // your registered `FacebookCallback` won't be called
        if (mCallbackManager.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(authStateListener!= null){
            mFirebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    private void handleFacebookToken(AccessToken token){
        Log.i(TAG, "handleFacebookToken" + token);

        AuthCredential credential= FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "sign in with credential: successful ");
                    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                    Log.i("userr",  "onComplete");

                    updateUI(firebaseUser);
                }
                else{
                    Log.i(TAG, "sign in with credential: failure "+ task.getException());
                    Toast.makeText(MainActivity.this,"Authentication Failed",Toast.LENGTH_SHORT);
                    Log.i("userr",  "onFail");

                    updateUI(null);

                }
            }
        });
    }
   private void updateUI(FirebaseUser user){
     /* ProfilePictureView profilePictureView;
      profilePictureView = (ProfilePictureView) findViewById(R.id.friendProfilePicture);*/

       if(user != null){

           textViewUser.setText(user.getDisplayName());

                String photoUrl = user.getPhotoUrl().toString();

                String profilePicUrl = "https://graph.facebook.com/" + sharedPref.getString(user.getEmail(),null) +"/picture?type=large";

                Picasso.get().load(profilePicUrl).fit().into(logoView);
 /*             if(Profile.getCurrentProfile() != null) {
                logoView.setVisibility(View.INVISIBLE);
                profilePictureView.setVisibility(View.VISIBLE);
                profilePictureView.setProfileId(Profile.getCurrentProfile().getId());
            }*/
        }
        else{
           textViewUser.setText("");
        /*   profilePictureView.setVisibility(View.INVISIBLE);
           logoView .setVisibility(View.VISIBLE);*/
           logoView.setImageResource(R.drawable.app_logo);

            }

   }

}

