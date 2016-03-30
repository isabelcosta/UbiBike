package pt.ulisboa.tecnico.cmu.ubibike;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class TrajectoryMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String bikerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trajectory_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        TextView manufacturerTextView = (TextView)findViewById(R.id.biker_name);;
        bikerName = getIntent().getStringExtra("bikerName");
        manufacturerTextView.setText(bikerName);


    }

     public void launchClick(View v) {
        Intent intent = null;
        Boolean execute = true;

        switch(v.getId()) {
            case R.id.menu_bottom_home:
                intent = new Intent(TrajectoryMapsActivity.this, UserDashboard.class);
                intent.putExtra("bikerName",bikerName);
                break;

            case R.id.menu_bottom_messenger:
                intent = new Intent(TrajectoryMapsActivity.this, Chat.class);
                intent.putExtra("bikerName", bikerName);

                break;

            case R.id.menu_bottom_options:
                intent = new Intent(TrajectoryMapsActivity.this, OptionsMenu.class);
                intent.putExtra("bikerName", bikerName);

//                execute = false;
                break;

            //            Points History

            case R.id.biker_score:
                intent = new Intent(TrajectoryMapsActivity.this, ScoreHistory.class);
                intent.putExtra("bikerName",bikerName);
                break;

//          Ubibike Logo
            case R.id.ubibikeLogo:
                intent = new Intent(TrajectoryMapsActivity.this, UserDashboard.class);
                intent.putExtra("bikerName",bikerName);
                break;
        }
        if (execute){
            startActivityForResult(intent, 0);
        }
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
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
