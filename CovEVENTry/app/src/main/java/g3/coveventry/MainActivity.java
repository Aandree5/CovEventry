package g3.coveventry;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Profile;
import com.facebook.ProfileTracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;

import g3.coveventry.customViews.CovImageView;

import static g3.coveventry.User.FILE_USER_PHOTO;
import static g3.coveventry.User.KEY_EMAIL;
import static g3.coveventry.User.KEY_FACEBOOKID;
import static g3.coveventry.User.KEY_NAME;
import static g3.coveventry.User.KEY_PHOTOURL;


public class MainActivity extends AppCompatActivity {
    // Keep user info
    User user;
    ProfileTracker profileTracker;
    NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

            switch (menuItem.getItemId())
            {
                case R.id.nav_home:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new HomeFragment())
                            .commit();
                    break;

                case R.id.nav_facebook_events:
                    Toast.makeText(getApplicationContext(), "Load facebook events fragment", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.nav_twitter_events:
                    Toast.makeText(getApplicationContext(), "Load twitter events fragment", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.nav_map:
                    Toast.makeText(getApplicationContext(), "Load map fragment", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.nav_database:
                    Toast.makeText(getApplicationContext(), "Load database fragment", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.nav_settings:
                    Toast.makeText(getApplicationContext(), "Open settings page", Toast.LENGTH_SHORT).show();
                    break;
            }

            // Close drawer after choosing an option
            drawer.closeDrawer(GravityCompat.START);

            return true;
        });


        // Keep track of user profile to look out for changes, mainly logging out
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                // Catch user logging out
                if (currentProfile == null)
                {
                    // Reset all user related information
                    user = null;

                    SharedPreferences.Editor sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                    sharedPreferences.remove(KEY_FACEBOOKID);
                    sharedPreferences.remove(KEY_NAME);
                    sharedPreferences.remove(KEY_EMAIL);
                    sharedPreferences.remove(KEY_PHOTOURL);
                    sharedPreferences.apply();

                    // Delete user photo file
                    //noinspection ResultOfMethodCallIgnored
                    new File(FILE_USER_PHOTO).delete();

                    // Find drawer header
                    View navHeader = navigationView.getHeaderView(0);

                    ((TextView) navHeader.findViewById(R.id.nav_header_name)).setText(getString(R.string.nav_header_name_guest));
                    ((CovImageView) navHeader.findViewById(R.id.nav_header_photo)).resetImage();
                }
            }
        };


        if(savedInstanceState == null)
        {
            // Load default fragment on start up
           getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();

            navigationView.setCheckedItem(R.id.nav_home);
        }

        // If user is logged in, create object and show information called for
        // app start up and when phone is rotated or brought back from background
        if(Profile.getCurrentProfile() != null)
            createUser();


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
    protected void onDestroy() {
        super.onDestroy();

        // Stop profile tracker from running
        profileTracker.stopTracking();
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

    void createUser(){
        // Find drawer header
        View navHeader = navigationView.getHeaderView(0);

        // Create new user object from shared preferences information
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        user = new User(sharedPreferences.getString(KEY_FACEBOOKID, null), sharedPreferences.getString(KEY_NAME, null),
                sharedPreferences.getString(KEY_EMAIL, null));

        // Show user name
        ((TextView) navHeader.findViewById(R.id.nav_header_name)).setText(user.name);


        // Get photo from file, if was already downloaded, otherwise download it from the kept link
        if(new File(FILE_USER_PHOTO).exists()) {
            FileInputStream fInpStream = null;
            try {
                fInpStream = openFileInput(FILE_USER_PHOTO);

                ((CovImageView) navHeader.findViewById(R.id.nav_header_photo)).setImageBitmap(BitmapFactory.decodeStream(fInpStream));

            } catch (Exception e) {
                e.printStackTrace();

            } finally {
                if (fInpStream != null) {
                    try {
                        fInpStream.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else if (sharedPreferences.getString(KEY_PHOTOURL, null) != null) {
            try {
                ((CovImageView) navHeader.findViewById(R.id.nav_header_photo)).setImageBitmap(new URL(sharedPreferences.getString(KEY_PHOTOURL, null)),
                        FILE_USER_PHOTO);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
}
