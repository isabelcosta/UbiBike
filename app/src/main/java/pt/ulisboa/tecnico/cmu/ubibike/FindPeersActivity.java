package pt.ulisboa.tecnico.cmu.ubibike;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.ulisboa.tecnico.cmu.ubibike.common.CommonWithButtons;

public class FindPeersActivity extends CommonWithButtons {

    private String bikerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peers_list);
        String bikerName = ((UbiBikeApplication) getApplication()).getUsername();

        this.bikerName = ((UbiBikeApplication) getApplication()).getUsername();

        // HEADER
        // biker name
        TextView manufacturerTextView = (TextView)findViewById(R.id.biker_name);
        manufacturerTextView.setText(bikerName);


        //      Change color to current menu
        Button messengerBtn = (Button) findViewById(R.id.menu_bottom_ubiconnect);
        messengerBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        messengerBtn.setTextColor(getResources().getColor(R.color.white));

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

        peersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Intent intent = new Intent(FindPeersActivity.this, GivePointsOrChat.class);

                String person = (String) parent.getItemAtPosition(position);

                intent.putExtra("person", person);
                startActivity(intent);
            }
        });

    }

//    public void launchClick(View v) {
//        Intent intent = null;
//        Boolean execute = true;
//
//        switch(v.getId()) {
//            case R.id.menu_bottom_home:
//                intent = new Intent(FindPeersActivity.this, UserDashboard.class);
//                intent.putExtra("bikerName",bikerName);
//                break;
//
//            case R.id.menu_bottom_ubiconnect:
//                intent = new Intent(FindPeersActivity.this, FindPeersActivity.class);
//                execute = false;
//
//                break;
//
//            case R.id.menu_bottom_options:
//                intent = new Intent(FindPeersActivity.this, OptionsMenu.class);
//                intent.putExtra("bikerName", bikerName);
//
////                execute = false;
//                break;
//
//            //            Points History
//
//            case R.id.biker_score:
//                intent = new Intent(FindPeersActivity.this, ScoreHistory.class);
//                break;
//
////          Ubibike Logo
//            case R.id.ubibikeLogo:
//                intent = new Intent(FindPeersActivity.this, UserDashboard.class);
//                break;
//        }
//        if (execute){
//            startActivityForResult(intent, 0);
//        }
//    }

}
