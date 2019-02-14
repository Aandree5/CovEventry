package g3.coveventry;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

import java.security.MessageDigest;

import g3.coveventry.customViews.RoundImage;


public class MainActivity extends AppCompatActivity {

    public static String userID = null;
    public static String userName = null;
    public static String userEmail = null;
    public static LoginForm loginForm = LoginForm.LF_NONE;

    // Possible login forms
    enum LoginForm{
        LF_NONE,
        LF_FACEBOOK,
        LF_TWITTER,
        LF_GOOGLE
    }

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
                if (currentProfile == null)
                {
                    userLoggedOut();
                }
            }
        };

        //TODO: Load data from logged user
        // Load menu_toolbar fragment
        if(savedInstanceState == null)
        {
           getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();

            navigationView.setCheckedItem(R.id.nav_home);

        } else if (userName != null) {

            View navHeader = navigationView.getHeaderView(0);

            //TODO: load image from file

            ((TextView) navHeader.findViewById(R.id.nav_header_name)).setText(userName);
            ((RoundImage) navHeader.findViewById(R.id.nav_header_photo)).resetImage();
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
    protected void onDestroy() {
        super.onDestroy();

        // Stop profile tracker from running
        profileTracker.stopTracking();
    }

    /**
     * Logout user from the app
     */
    private void userLoggedOut() {
        userID = null;
        userName = null;
        userEmail = null;
        loginForm = LoginForm.LF_NONE;

        View navHeader = navigationView.getHeaderView(0);

        ((TextView) navHeader.findViewById(R.id.nav_header_name)).setText("Guest");
        ((RoundImage) navHeader.findViewById(R.id.nav_header_photo)).resetImage();
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
}
