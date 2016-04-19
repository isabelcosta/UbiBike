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

public class UbiRidesHistory extends CommonWithButtons {

    private ListView availableBikesList;
    private ArrayAdapter<String> availableBikesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubiride_history);

        TextView manufacturerTextView = (TextView)findViewById(R.id.biker_name);
        String bikerName = getIntent().getStringExtra("bikerName");
        manufacturerTextView.setText(bikerName);

        availableBikesList = (ListView) findViewById(R.id.find_bike_list_view);

        List<String> availableBikeArray = new ArrayList<String>(Arrays.asList(DummyData.getTrajectories()));

        availableBikesAdapter = new ArrayAdapter<String>(
                getApplicationContext(),
                R.layout.activity_ubiride_history_item,
                R.id.find_bike_item,
                availableBikeArray
        );

        availableBikesList.setAdapter(availableBikesAdapter);
        //listAdapter.notifyDataSetChanged();

        availableBikesList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long row)
            {
                Intent intent = new Intent(UbiRidesHistory.this, UbiRidesHistoryMapsActivity.class);
                startActivity(intent);
                //String value = (String)adapter.getItemAtPosition(position);
                // assuming string and if you want to get the value on click of list item
                // do what you intend to do on click of listview row
            }
        });
    }
}
