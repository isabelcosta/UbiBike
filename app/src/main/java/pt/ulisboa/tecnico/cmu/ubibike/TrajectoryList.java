package pt.ulisboa.tecnico.cmu.ubibike;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrajectoryList extends AppCompatActivity {

    private ListView trajectoryList;
    private ArrayAdapter<String> arraylistAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trajectory_list);


        TextView manufacturerTextView = (TextView)findViewById(R.id.biker_name);
        String bikerName = getIntent().getStringExtra("bikerName");
        manufacturerTextView.setText(bikerName);

        trajectoryList = (ListView) findViewById(R.id.trajectory_list_view);

        List<String> trajectoriesArray = new ArrayList<String>(Arrays.asList(DummyData.getTrajectories()));

        arraylistAdapter = new ArrayAdapter<String>(
                getApplicationContext(),
                R.layout.activity_trajectory_list_item,
                R.id.trajectory_list_view_item,
                trajectoriesArray
        );

        trajectoryList.setAdapter(arraylistAdapter);
        //listAdapter.notifyDataSetChanged();
    }
}
