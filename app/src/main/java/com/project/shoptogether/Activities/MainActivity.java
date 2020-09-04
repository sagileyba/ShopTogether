package com.project.shoptogether.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.project.shoptogether.Fragments.CreatedListFragment;
import com.project.shoptogether.Fragments.FavoritesFragment;
import com.project.shoptogether.Fragments.HomeFragment;
import com.project.shoptogether.Fragments.SharedListFragment;
import com.project.shoptogether.R;
import com.squareup.picasso.Picasso;


import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int RC_SIGN_IN = 123; // Sign in Id. The number is not relevant
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private BottomNavigationView bottomNav;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    NavigationView navigationView;
    List<AuthUI.IdpConfig> providers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //toolbar settings
        toolbar = findViewById(R.id.main_activity_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.app_name));  //toolbar title

        firebaseAuth=FirebaseAuth.getInstance();
        user=firebaseAuth.getCurrentUser();

        // Navigation drawer settings
        drawer = findViewById(R.id.drawer_layout);
        navigationView= findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this); // Open the selected fragment from navigation drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState(); // display navigation drawer icon


        //Bottom navigation settings
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener); // Open the selected fragment from bottom navigation

        // keep the selected fragment when rotating the device
      /*  if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
        }*/

        // Sign in options- Email, Google and Facebook
        providers= Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build()

        );

        // If the the user is disconnected, display registration screen
        if(user==null){
            showSignInOptions();
        }


    }


    @Override //on open and after refresh the activity
    protected void onResume() {
        super.onResume();
        if(toolbar.hasExpandedActionView())
            toolbar.collapseActionView();
        updateProfile();
    }



    public void signOut() {

        AuthUI.getInstance()
                .signOut(MainActivity.this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        firebaseAuth.signOut();
                        showSignInOptions();
                    }
                });
    }

    private void showSignInOptions() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder().setIsSmartLockEnabled(false)
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                user = FirebaseAuth.getInstance().getCurrentUser();

            } else {
                Toast.makeText(this, "Sign in error: "+ Objects.requireNonNull(Objects.requireNonNull(response).getError()).getMessage(), Toast.LENGTH_SHORT).show();
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    private void updateProfile(){
        View view= navigationView.getHeaderView(0);
        ImageView profile_image = view.findViewById(R.id.nav_pic);
        TextView username= view.findViewById(R.id.nav_userName);
        TextView email= view.findViewById(R.id.nav_email);
        //If the user is connected display his details in the navigation drawer
        if(user!=null){
            username.setText(user.getDisplayName());
            email.setText(user.getEmail());
            if(user.getPhotoUrl()!=null) // Display user image by his url
            {
                Uri uri;
                uri=user.getPhotoUrl();
                Picasso.get().load(uri).into(profile_image);
            }
            else
                profile_image.setImageResource(R.mipmap.ic_launcher_round);

            //open user profile
            profile_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(),MyProfileActivity.class));
                }
            });
        }

    }


    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {

                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            selectedFragment = new HomeFragment();
                            break;
                        case R.id.nav_createdShopping:
                            selectedFragment = new CreatedListFragment();
                            break;
                        case R.id.nav_sharedShopping:
                            selectedFragment = new SharedListFragment();
                            break;
                        case R.id.nav_favorites:
                            selectedFragment = new FavoritesFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            Objects.requireNonNull(selectedFragment)).commit();
                    return true;
                }
            };

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.nav_share:
                Intent intent= new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                // what you want to send
                String sharebody= "Wanna share your shopping list with your friend?";
                // subject
                String sharesub= "Download App";

                intent.putExtra(Intent.EXTRA_SUBJECT, sharesub);
                intent.putExtra(Intent.EXTRA_TEXT, sharebody);
                // pop-up title
                startActivity(Intent.createChooser(intent, "Share the app"));
                break;
            case R.id.nav_about:
                startActivity(new Intent(this,AboutActivity.class));
                break;
            case R.id.nav_logout:
                signOut();
                break;

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //close navigation drawer at first
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else { // go back
            super.onBackPressed();
        }
    }

}

