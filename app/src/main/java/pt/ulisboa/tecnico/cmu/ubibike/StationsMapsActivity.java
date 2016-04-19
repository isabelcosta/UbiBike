package pt.ulisboa.tecnico.cmu.ubibike;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import pt.ulisboa.tecnico.cmu.ubibike.common.CommonWithButtons;

import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.ADD_POINTS;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.ADD_POINTS_TEST_125;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.ADD_POINTS_TEST_125_ORIGIN;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.BIKE_NOT_RESERVED_HAS_RESERVE;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.BIKE_NOT_RESERVED_HAS_RESERVE_TOAST_MSG;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.BIKE_NOT_RESERVED_NO_BIKES_AVAILABLE;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.BIKE_NOT_RESERVED_NO_BIKES_AVAILABLE_TOAST_MSG;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.BIKE_RESERVED;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.BIKE_RESERVED_TOAST_MSG;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.CLIENT_NAME;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.CLIENT_POINTS;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.INTENT_LOCATION_MESSAGE;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.INTENT_LATITUDE;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.INTENT_LONGITUDE;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.MAPS_ZOOM_LEVEL_STATION;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.POINTS_ORIGIN;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.REQUEST_TYPE;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.RESERVE_BIKE;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.SERVER_IP;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.SERVER_PORT;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.STATION_NAME;

public class StationsMapsActivity extends CommonWithButtons implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String bikerName;
    private LatLng location;
    private double latitude;
    private double longitude;
    private String locationMessage;
    private Button reserverButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stations_maps);
        this.bikerName = ((UbiBikeApplication) getApplication()).getUsername();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        TextView bikerNameTextView = (TextView)findViewById(R.id.biker_name);
        bikerNameTextView.setText(bikerName);

        reserverButton = (Button) findViewById(R.id.reserve_bike_button);



        latitude = Double.parseDouble(getIntent().getStringExtra(INTENT_LATITUDE));
        longitude = Double.parseDouble(getIntent().getStringExtra(INTENT_LONGITUDE));
        locationMessage = getIntent().getStringExtra(INTENT_LOCATION_MESSAGE);


        // Button Press Event Listeners
        reserverButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                ReserveBike reserveBikeTask = new ReserveBike();
                try {
                    reserveBikeTask.execute().get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

            }
        });


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


    private class ReserveBike extends AsyncTask<Void, Void, Void> {
        private DataOutputStream dataOutputStream;
        private DataInputStream dataInputStream;
        private JSONObject json;
        private String toastResult;

        @Override
        protected Void doInBackground(Void... params) {
            Socket socket;
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
            } catch (IOException e) {
                return null;
            }

            try {

                json = new JSONObject();
                json.put(REQUEST_TYPE, RESERVE_BIKE);
                json.put(STATION_NAME, locationMessage);
                json.put(CLIENT_NAME, bikerName);


                dataOutputStream = new DataOutputStream(
                        socket.getOutputStream());

                dataInputStream = new DataInputStream(
                        socket.getInputStream());

                // transfer JSONObject as String to the server
                dataOutputStream.writeUTF(json.toString());

                // Thread will wait till server replies
                String response = dataInputStream.readUTF();

                switch (Integer.parseInt(response)) {
                    case BIKE_RESERVED :
                        toastResult = BIKE_RESERVED_TOAST_MSG;
                        break;

                    case BIKE_NOT_RESERVED_HAS_RESERVE :
                        toastResult = BIKE_NOT_RESERVED_HAS_RESERVE_TOAST_MSG;
                        break;

                    case BIKE_NOT_RESERVED_NO_BIKES_AVAILABLE :
                        toastResult = BIKE_NOT_RESERVED_NO_BIKES_AVAILABLE_TOAST_MSG;
                        break;
                }


                socket.close();


            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {

                // close socket
                if (socket != null) {
                    try {
                        Log.i("close", "closing the socket");
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // close input stream
                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // close output stream
                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;

        }
        @Override
        protected void onPostExecute(Void res) {
            super.onPostExecute(res);
//                 TODO Update the UI thread with the final result
            Toast.makeText(getApplicationContext(),
                    toastResult, Toast.LENGTH_LONG).show();
        }
    }

}
