package pt.ulisboa.tecnico.cmu.ubibike;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import pt.ulisboa.tecnico.cmu.ubibike.common.CommonWithButtons;
import pt.ulisboa.tecnico.cmu.ubibike.common.MapsCoordinates;

import static com.ubibike.Constants.CLIENT_NAME;
import static com.ubibike.Constants.GET_RIDES_HISTORY;
import static com.ubibike.Constants.MAPS_ZOOM_LEVEL_STATION;
import static com.ubibike.Constants.REQUEST_TYPE;
import static com.ubibike.Constants.RIDES_HISTORY_LIST;
import static com.ubibike.Constants.SERVER_IP;
import static com.ubibike.Constants.SERVER_PORT;

public class UbiRideMapsActivity extends WifiDirectActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
        // <lat, long>
        ArrayList<LatLng> coordinatesPerRide = null;

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
//
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
//
//
//        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
//            @Override
//            public void onMyLocationChange(Location location) {
//
//                Log.i("called", "onLocationChanged");
//
//
//                //when the location changes, update the map by zooming to the location
//                CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(),location.getLongitude()));
//                mMap.moveCamera(center);
//
//                CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);
//                mMap.animateCamera(zoom);
//                /**
//                 * moved to CommonWithButtons
//                 */
////                coordinatesPerRide.put(String.valueOf(location.getLatitude()), // latitude
////                                        String.valueOf(location.getLongitude())); // longitude
////                Log.d("latitude maps ", location.getLatitude()+"");
////                Log.d("longitude maps ", location.getLongitude()+"");
////
////                // FIXME: 26-Apr-16 not sure if we can directly change coordinatesPerRide without a set
////                ((UbiBikeApplication) getApplication()).setCoordinatesPerRide(coordinatesPerRide);
////                // TODO: 26-Apr-16 tratar das verificações dos beacons aqui
////                // TODO: 26-Apr-16 por agora temos que criar uma ligação WIFI-DIRECT por activity
//
//            }
//        });

    }







}
