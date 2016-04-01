package pt.ulisboa.tecnico.cmu.ubibike;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FindPeersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peers_list);
        String bikerName = ((UbiBikeApplication) getApplication()).getUsername();

        TextView bikersName = (TextView)findViewById(R.id.biker_name);
        bikersName.setText(bikerName);


        ListView peersList = (ListView) findViewById(R.id.peers_list_view);

        List<String> allPeersArray = new ArrayList<String>(Arrays.asList(DummyData.getPeers()));


        //@TODO create adapter to adapt item view to get green light and peers name
        ArrayAdapter peersAdapter = new ArrayAdapter<String>(
                getApplicationContext(),
                R.layout.activity_peers_list_item,
                R.id.peers_list_view_item,
                allPeersArray
        );

        peersList.setAdapter(peersAdapter);
    }
}
