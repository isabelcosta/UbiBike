package pt.ulisboa.tecnico.cmu.ubibike;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FindBike extends AppCompatActivity {

    private ListView availableBikesList;
    private ArrayAdapter<String> availableBikesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_bike);

        TextView manufacturerTextView = (TextView)findViewById(R.id.biker_name);
        String bikerName = getIntent().getStringExtra("bikerName");
        manufacturerTextView.setText(bikerName);

        availableBikesList = (ListView) findViewById(R.id.find_bike_list_view);

        List<String> availableBikeArray = new ArrayList<String>(Arrays.asList(DummyData.getTrajectories()));

        availableBikesAdapter = new ArrayAdapter<String>(
                getApplicationContext(),
                R.layout.activity_find_bike_item,
                R.id.find_bike_item,
                availableBikeArray
        );

        availableBikesList.setAdapter(availableBikesAdapter);
        //listAdapter.notifyDataSetChanged();
    }
}
