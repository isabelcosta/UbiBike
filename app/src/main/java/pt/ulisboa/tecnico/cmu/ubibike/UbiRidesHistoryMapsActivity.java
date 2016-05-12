package pt.ulisboa.tecnico.cmu.ubibike;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import pt.ulisboa.tecnico.cmu.ubibike.common.MapsCoordinates;

import static com.ubibike.Constants.*;


public class UbiRidesHistoryMapsActivity extends WifiDirectActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String bikerName;
    private LatLng location;
    private String rideNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubiride_history_maps);
        this.bikerName = ((UbiBikeApplication) getApplication()).getUsername();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        TextView bikerNameTextView = (TextView)findViewById(R.id.biker_name);
        bikerNameTextView.setText(bikerName);



        rideNumber = getIntent().getStringExtra(INTENT_RIDE_NUMBER);




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
        app = ((UbiBikeApplication) getApplication());

        final ArrayList<LatLng> coordsList = new ArrayList<>();

        ArrayList<MapsCoordinates>
                rideCoordinates = app.getRidesHistory().get(rideNumber);
        System.out.println("lolp " + rideCoordinates);
        if (!rideCoordinates.isEmpty()) {
            for (MapsCoordinates coord : rideCoordinates ) {
                coordsList.add(new LatLng(coord.getLatitude(), coord.getLongitude()));
                Log.d("coord Lat Lng", coord.getLatitude() + " " + coord.getLongitude());
            }
        }
        if (coordsList.isEmpty()) {
            return;
        }
        final Polyline line = mMap.addPolyline(new PolylineOptions()
                .addAll(coordsList)
                .width(5)
                .color(Color.RED));

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(moveToBounds(line), 30));

            }
        });

    }

    private LatLngBounds moveToBounds(Polyline p)
    {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        List<LatLng> arr = p.getPoints();
        for(int i = 0; i < arr.size();i++){
            builder.include(arr.get(i));
            Log.d("build arr", arr.get(i) + " " );

        }
        return builder.build();

    }


}
