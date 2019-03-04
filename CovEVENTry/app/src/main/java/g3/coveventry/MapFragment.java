package g3.coveventry;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;

import g3.coveventry.customViews.CovImageView;

import static g3.coveventry.User.FILE_USER_PHOTO;
import static g3.coveventry.User.KEY_PHOTOURL;

public class MapFragment extends Fragment implements OnMapReadyCallback
{



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_map_fragment, container, false);

        SupportMapFragment mapView = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map);

        mapView.getMapAsync(this);
        Log.i("AppLog", "YAa");

        return view;
    }

    @Override
    public void onMapReady (GoogleMap googleMap)
    {
        Log.i("AppLog", "YAa");
        LatLng cv = new LatLng(52.406822, -1.519693);
        googleMap.addMarker(new MarkerOptions().position(cv).title("Coventry"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(cv));
    }

}


