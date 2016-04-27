package pt.ulisboa.tecnico.cmu.ubibike;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

import pt.ulisboa.tecnico.cmu.ubibike.common.CommonWithButtons;

public class UbiRideMapsActivity extends CommonWithButtons implements OnMapReadyCallback {

    private GoogleMap mMap;
        // <lat, long>
    HashMap<String, String> coordinatesPerRide = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubi_ride_maps);
        this.bikerName = ((UbiBikeApplication) getApplication()).getUsername();

        TextView bikerNameTextView = (TextView) findViewById(R.id.biker_name);
        bikerNameTextView.setText(bikerName);

        coordinatesPerRide = ((UbiBikeApplication) getApplication()).getCoordinatesPerRide();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {

                CameraUpdate center=CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
                CameraUpdate zoom=CameraUpdateFactory.zoomTo(17);
                mMap.moveCamera(center);
                mMap.animateCamera(zoom);
                /**
                 * moved to CommonWithButtons
                 */
//                coordinatesPerRide.put(String.valueOf(location.getLatitude()), // latitude
//                                        String.valueOf(location.getLongitude())); // longitude
//                Log.d("latitude maps ", location.getLatitude()+"");
//                Log.d("longitude maps ", location.getLongitude()+"");
//
//                // FIXME: 26-Apr-16 not sure if we can directly change coordinatesPerRide without a set
//                ((UbiBikeApplication) getApplication()).setCoordinatesPerRide(coordinatesPerRide);
//                // TODO: 26-Apr-16 tratar das verificações dos beacons aqui
//                // TODO: 26-Apr-16 por agora temos que criar uma ligação WIFI-DIRECT por activity

            }
        });

    }





}
