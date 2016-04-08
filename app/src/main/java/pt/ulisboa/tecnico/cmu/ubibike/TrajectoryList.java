package pt.ulisboa.tecnico.cmu.ubibike;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.ulisboa.tecnico.cmu.ubibike.common.CommonWithButtons;

public class TrajectoryList extends CommonWithButtons {

    private ListView trajectoryList;
    private ArrayAdapter<String> arraylistAdapter;
    private String bikerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trajectory_list);

        this.bikerName = ((UbiBikeApplication) getApplication()).getUsername();

        // HEADER
        // biker name
        TextView manufacturerTextView = (TextView)findViewById(R.id.biker_name);
        manufacturerTextView.setText(bikerName);

        trajectoryList = (ListView) findViewById(R.id.trajectory_list_view);

        List<String> trajectoriesArray = new ArrayList<String>(Arrays.asList(DummyData.getTrajectories()));

        arraylistAdapter = new ArrayAdapter<String>(
                getApplicationContext(),
                R.layout.activity_trajectory_list_item,     //listView item layout
                R.id.trajectory_list_view_item,             //listView id
                trajectoriesArray
        );

        trajectoryList.setAdapter(arraylistAdapter);
        //listAdapter.notifyDataSetChanged();

        trajectoryList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long row)
            {
                Intent intent = new Intent(TrajectoryList.this, TrajectoryMapsActivity.class);
                startActivity(intent);
                //String value = (String)adapter.getItemAtPosition(position);
                // assuming string and if you want to get the value on click of list item
                // do what you intend to do on click of listview row
            }
        });
    }


//    public void launchClick(View v) {
//        Intent intent = null;
//        Boolean execute = true;
//
//        switch(v.getId()) {
//            case R.id.menu_bottom_home:
//                intent = new Intent(TrajectoryList.this, UserDashboard.class);
//                intent.putExtra("bikerName",bikerName);
//                break;
//
//            case R.id.menu_bottom_ubiconnect:
//                intent = new Intent(TrajectoryList.this, FindPeersActivity.class);
//                execute = false;
//                break;
//
//            case R.id.menu_bottom_options:
//                intent = new Intent(TrajectoryList.this, OptionsMenu.class);
//                intent.putExtra("bikerName",bikerName);
//                break;
//
////            Points History
//
//            case R.id.biker_score:
//                intent = new Intent(TrajectoryList.this, ScoreHistory.class);
//                intent.putExtra("bikerName",bikerName);
//                break;
////          Ubibike Logo
//            case R.id.ubibikeLogo:
//                intent = new Intent(TrajectoryList.this, UserDashboard.class);
//                intent.putExtra("bikerName",bikerName);
//                break;
//        }
//        if (execute){
//            startActivityForResult(intent, 0);
//        }
//    }
}
