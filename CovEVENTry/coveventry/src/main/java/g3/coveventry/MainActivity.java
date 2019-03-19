package g3.coveventry;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;

import java.security.MessageDigest;

import g3.coveventry.customviews.CovImageView;
import g3.coveventry.database.Database;
import g3.coveventry.events.AddEventFragment;
import g3.coveventry.events.EventsFragment;
import g3.coveventry.events.MapFragment;
import g3.coveventry.user.User;


public class MainActivity extends AppCompatActivity {
    //Constants
    public static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 100;

    NavigationView navigationView;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Set theme to replace the theme to show the splash screen
        // NOTE: As to be before super.onCreate()!
        setTheme(R.style.AppTheme_NoActionBar);

        super.onCreate(savedInstanceState);

        // Set the view for the activity
        setContentView(R.layout.activity_main);

        // Set menu_toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Add drawer toggle button to menu_toolbar
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        // Set navigation view listener
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            // Prevent reselection
            if (navigationView.getCheckedItem() == menuItem)
                return false;

            switch (menuItem.getItemId()) {
                case R.id.nav_home:
                    toolbar.setTitle(R.string.app_name);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new HomeFragment())
                            .commit();
                    break;

                case R.id.nav_events:
                    toolbar.setTitle("Events");
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new EventsFragment())
                            .addToBackStack(null)
                            .commit();
                    break;

                case R.id.nav_AddEvent:
                    toolbar.setTitle("AddEvent");
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new AddEventFragment())
                            .commit();
                    break;

                case R.id.nav_map:
                    toolbar.setTitle("Maps");
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new MapFragment())
                            .addToBackStack(null)
                            .commit();
                    break;


                case R.id.nav_account:
                    toolbar.setTitle("Profile");
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ProfileFragment())
                            .commit();
                    break;


                case R.id.nav_settings:
                    Toast.makeText(getApplicationContext(), "Open settings page", Toast.LENGTH_SHORT).show();
                    break;
            }

            // Close drawer after choosing an option
            drawer.closeDrawer(GravityCompat.START);

            return true;
        });

        bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {

            switch (menuItem.getItemId()) {
                case R.id.bottom_nav_messages:
                    break;

                case R.id.bottom_nav_create_event:
                    break;

                case R.id.bottom_nav_notifications:
                    break;
            }

            return true;
        });

        // Initialize twitter API
        Twitter.initialize(new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG)) // Enable logging when app is in debug mode
                .twitterAuthConfig(new TwitterAuthConfig(getResources().getString(R.string.TWITTER_KEY), getResources().getString(R.string.TWITTER_SECRET)))
                .debug(true) // Enable debug mode
                .build());


        // Initialize User and register the callback for when data is updated
        User.initialize(this, () -> {
            // Find drawer header
            View navHeader = navigationView.getHeaderView(0);

            // Update user name
            if (User.getCurrentUser().getName() != null)
                ((TextView) navHeader.findViewById(R.id.nav_header_name)).setText(User.getCurrentUser().getName());

            else
                ((TextView) navHeader.findViewById(R.id.nav_header_name)).setText(getResources().getString(R.string.nav_header_name_guest));


            // Update user photo
            if (User.getCurrentUser().getProfilePicture() != null)
                ((CovImageView) navHeader.findViewById(R.id.nav_header_photo)).setImageBitmap(User.getCurrentUser().getProfilePicture());

            else
                ((CovImageView) navHeader.findViewById(R.id.nav_header_photo)).resetImage();
        });

        // Initialize Database
        Database.initialize(this);

        if (savedInstanceState == null) {
            // Load default fragment on start up
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();

            navigationView.setCheckedItem(R.id.nav_home);
        }

        // Request needed permission if user didn't given them yet
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            navigationView.getMenu().findItem(R.id.nav_events).setEnabled(false);

            // If user already said no once, show a message to explain better why we need them, otherwise just ask for permission
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("Access device location")
                        .setMessage("The location will only be used to show events that are happening near you.")
                        .setPositiveButton("Allow", (dialog, which) -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION))
                        .setNegativeButton("Cancel", null)
                        .show();

            } else
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ACCESS_FINE_LOCATION);

        }

        // DEBUG
        // Development hash key (Facebook)
        try {
            @SuppressLint("PackageManagerGetSignatures")
            final PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                final MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                final String hashKey = new String(Base64.encode(md.digest(), 0));
                Log.i("AppLog", "HashKey: " + hashKey + "=");
            }
        } catch (Exception e) {
            Log.e("AppLog", "error:", e);
        }
    }


    @Override
    public void onBackPressed() {
        // Close drawer if is open
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add items to toolbar menu
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Items action
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the fragment, which will then pass the result to the login buttons
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for requested permissions
        switch (requestCode) {
            case PERMISSION_REQUEST_ACCESS_FINE_LOCATION:
                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    navigationView.getMenu().findItem(R.id.nav_events).setEnabled(true);

                else
                    navigationView.getMenu().findItem(R.id.nav_events).setEnabled(true);
        }
    }
}
