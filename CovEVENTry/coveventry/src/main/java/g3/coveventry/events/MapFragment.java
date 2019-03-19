package g3.coveventry.events;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import g3.coveventry.R;

public class MapFragment extends Fragment
{



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_map_fragment, container, false);

        SupportMapFragment mapView = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapView != null)
            mapView.getMapAsync(googleMap -> {

                LatLng position = new LatLng(52.40656, -1.51217);

                MarkerOptions marker = new MarkerOptions()
                        .position(position)
                        .title("Coventry");

                marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));

                googleMap.addMarker(marker);
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(position)
                        .zoom(12)
                        .build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(52.4053, -1.4997))
                        .title("Coventry University")
                        .snippet("This is the university")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                );

                /*marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));*/

                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(52.4129, -1.5032))
                        .title("Kasbah Night Club"));

                marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));

            });


        return view;
    }

 /*   @Override
    public void onMapReady (GoogleMap googleMap)
    {
        Log.i("AppLog", "YAa");
        LatLng cv = new LatLng(52.406822, -1.519693);
        googleMap.addMarker(new MarkerOptions().position(cv).title("Coventry"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(cv));
    }
*/
}


