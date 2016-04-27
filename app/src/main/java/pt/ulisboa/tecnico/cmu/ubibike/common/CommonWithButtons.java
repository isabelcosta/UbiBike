package pt.ulisboa.tecnico.cmu.ubibike.common;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import java.util.HashMap;

import pt.ulisboa.tecnico.cmu.ubibike.UbiconnectActivity;
import pt.ulisboa.tecnico.cmu.ubibike.OptionsMenu;
import pt.ulisboa.tecnico.cmu.ubibike.R;
import pt.ulisboa.tecnico.cmu.ubibike.ScoreHistory;
import pt.ulisboa.tecnico.cmu.ubibike.UbiBikeApplication;
import pt.ulisboa.tecnico.cmu.ubibike.UserDashboard;

import static com.ubibike.Constants.*;

/**
 * Created by vicente on 07-Apr-16.
 */
public class CommonWithButtons extends AppCompatActivity {

    protected String bikerName;
    protected String bikerScore;
    protected Button pointsButton;
    protected TextView bikersNameTextView;
    protected Handler handler = new Handler();
    HashMap<String, String> coordinatesPerRide = null;
    protected MyLocationListener locationListener;
    protected LocationManager lm;
    protected UbiBikeApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_history);

        app = ((UbiBikeApplication) getApplication());

        // HEADER
        // biker name
        bikerName = app.getUsername();


        // Restore preferences
        bikerScore = app.getBikerScore(true);
        coordinatesPerRide = app.getCoordinatesPerRide();

        // Views
        pointsButton = (Button) findViewById(R.id.biker_score);
        bikersNameTextView = (TextView) findViewById(R.id.biker_name);


        // Sets
        pointsButton.setText(bikerScore);
        bikersNameTextView.setText(bikerName);


        // Get user current points and refresh Views
        // TODO: 09-Apr-16 make UbiBikeApplication check score
        handler.postAtTime(requestScoreTask, SystemClock.uptimeMillis() + 100);


        // get location updates
        getLocationUpdates();


    }

    private Runnable requestScoreTask = new Runnable() {
        public void run() {


            bikerScore = app.getBikerScore(false);

            pointsButton = (Button) findViewById(R.id.biker_score);
            pointsButton.setText(bikerScore);

            // every 5 minutes calls the server to check for updates
            // todo perguntar ao prof opiniao sobre isto (se ha maneira/vantagem em ser o server a iniciar a comunicacao)
//            handler.postAtTime(requestScoreTask, SystemClock.uptimeMillis() + 18000 );


        }
    };


//    private class GetPoints extends AsyncTask<Void, Void, Void> {
//        private DataOutputStream dataOutputStream;
//        private DataInputStream dataInputStream;
//        private JSONObject json;
//
//        @Override
//        protected Void doInBackground(Void... params) {
//
//
//            try {
//                socket = new Socket(SERVER_IP, SERVER_PORT);
//            } catch (IOException e) {
//                return null;
//            }
//
//            try {
//                json = new JSONObject();
//                json.put(REQUEST_TYPE, GET_POINTS);
//                json.put(CLIENT_NAME, bikerName);
//
//
//                dataOutputStream = new DataOutputStream(
//                        socket.getOutputStream());
//
//                dataInputStream = new DataInputStream(
//                        socket.getInputStream());
//
//                // transfer JSONObject as String to the server
//                dataOutputStream.writeUTF(json.toString());
//
//                // Thread will wait till server replies
//                String response = dataInputStream.readUTF();
//
//
//                final JSONObject jsondata;
//                jsondata = new JSONObject(response);
//
//                bikerScore = jsondata.getString(POINTS);
//
//
//
//            } catch (UnknownHostException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (JSONException e) {
//                e.printStackTrace();
//            } finally {
//
//                // close socket
//                if (socket != null) {
//                    try {
//                        Log.i("close", "closing the socket");
//                        socket.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                // close input stream
//                if (dataInputStream != null) {
//                    try {
//                        dataInputStream.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                // close output stream
//                if (dataOutputStream != null) {
//                    try {
//                        dataOutputStream.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//            return null;
//        }
//    }


    @Override
    protected void onPause(){
        super.onPause();

//Set Preference

        UbiBikeApplication app = ((UbiBikeApplication) getApplication());
        app.saveBikerScore(bikerScore, true);
    }

    public void launchClick(View v) {
        Intent intent = null;
        Boolean execute = true;

        switch(v.getId()) {
            case R.id.menu_bottom_home:
                intent = new Intent(this, UserDashboard.class);
                intent.putExtra("bikerName",bikerName);
                break;

            case R.id.menu_bottom_ubiconnect:
                intent = new Intent(this, UbiconnectActivity.class);
                intent.putExtra("bikerName",bikerName);
                break;

            case R.id.menu_bottom_options:
                intent = new Intent(this, OptionsMenu.class);
                intent.putExtra("bikerName",bikerName);
                break;

//            Points History

            case R.id.biker_score:
                intent = new Intent(this, ScoreHistory.class);
                intent.putExtra("bikerName",bikerName);
                break;

//          Ubibike Logo
            case R.id.ubibikeLogo:
                intent = new Intent(this, UserDashboard.class);
                intent.putExtra("bikerName",bikerName);
                break;
        }
        if (execute){
            startActivityForResult(intent, 0);
        }
    }
    private final class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            // called when the listener is notified with a location update from the GPS
            String lat = String.valueOf(location.getLatitude());    // latitude
            String lng = String.valueOf(location.getLongitude());   // longitude

            coordinatesPerRide.put(lat, lng);
            Log.d("latitude maps ", lat);
            Log.d("longitude maps ", lng);

            // FIXME: 26-Apr-16 not sure if we can directly change coordinatesPerRide without a set
            app.setCoordinatesPerRide(coordinatesPerRide);

        }

        @Override
        public void onProviderDisabled(String provider) {
            // called when the GPS provider is turned off (user turning off the GPS on the phone)
        }

        @Override
        public void onProviderEnabled(String provider) {
            // called when the GPS provider is turned on (user turning on the GPS on the phone)
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // called when the status of the GPS provider changes
        }
    }

    private void getLocationUpdates() {
        locationListener = new MyLocationListener();
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // check permissions
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
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_CHECKING_PERIOD, GPS_CHECKING_DISTANCE, locationListener);
    }

}
