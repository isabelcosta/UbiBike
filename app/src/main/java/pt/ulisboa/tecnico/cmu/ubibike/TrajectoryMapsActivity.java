package pt.ulisboa.tecnico.cmu.ubibike;

import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import pt.ulisboa.tecnico.cmu.ubibike.common.CommonWithButtons;

import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.INTENT_LOCATION_MESSAGE;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.INTENT_LATITUDE;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.INTENT_LONGITUDE;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.MAPS_ZOOM_LEVEL_STATION;

public class TrajectoryMapsActivity extends CommonWithButtons implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String bikerName;
    private LatLng location;
    private double latitude;
    private double longitude;
    private String locationMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trajectory_maps);
        this.bikerName = ((UbiBikeApplication) getApplication()).getUsername();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        TextView bikerNameTextView = (TextView)findViewById(R.id.biker_name);
        bikerNameTextView.setText(bikerName);


        latitude = Double.parseDouble(getIntent().getStringExtra(INTENT_LATITUDE));
        longitude = Double.parseDouble(getIntent().getStringExtra(INTENT_LONGITUDE));
        locationMessage = getIntent().getStringExtra(INTENT_LOCATION_MESSAGE);



    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        location = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(location).title(locationMessage));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,MAPS_ZOOM_LEVEL_STATION));
    }


}
