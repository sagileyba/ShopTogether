package com.project.shoptogether;

import android.content.Intent;

import android.os.Bundle;

import android.util.Log;
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
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewUser = findViewById(R.id.user_text);
        logoView = findViewById(R.id.sign_in_logo);
        FB_Button = findViewById(R.id.login_button);
        mFirebaseAuth = FirebaseAuth.getInstance();
    mCallbackManager= CallbackManager.Factory.create();
        FB_Button.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i(TAG,"onSuccess"+loginResult);
                handleFacebookToken(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {
                Log.i(TAG,"onCancel");
                LoginManager.getInstance().logOut();
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
                  updateUI(firebaseUser);
                }
                else
                    updateUI(null);
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
        mCallbackManager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
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
                    updateUI(firebaseUser);
                }
                else{
                    Log.i(TAG, "sign in with credential: failure "+ task.getException());
                    Toast.makeText(MainActivity.this,"Authentication Failed",Toast.LENGTH_SHORT);
                    updateUI(null);

                }
            }
        });
    }
   private void updateUI(FirebaseUser user){
    /*   ProfilePictureView profilePictureView;
       profilePictureView = (ProfilePictureView) findViewById(R.id.friendProfilePicture);
*/
       if(user != null){
            textViewUser.setText(user.getDisplayName());
            if(user.getPhotoUrl() != null) {
                String photoUrl = user.getPhotoUrl().toString();
                Picasso.get().load(photoUrl).resize(250,250).into(logoView);



//                profilePictureView.setProfileId(Profile.getCurrentProfile().getId());
            }
        }
        else{
                textViewUser.setText("");
                logoView.setImageResource(R.drawable.app_logo);

            }

   }

}

