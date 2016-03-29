package pt.ulisboa.tecnico.cmu.ubibike;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrajectoryList extends AppCompatActivity {

    ArrayAdapter<String> listAdapter;
    ListView trajectoryList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trajectory_list);

        trajectoryList = (ListView) findViewById(R.id.trajectory_list_view);

        List<String> trajectoriesArray = new ArrayList<String>(Arrays.asList(DummyData.getTrajectories()));

        listAdapter = new ArrayAdapter<String>(
                getApplicationContext(),
                R.layout.activity_trajectory_list_item,
                R.id.trajectory_list_view_item,
                trajectoriesArray
        );


        trajectoryList.setAdapter(listAdapter);
        //listAdapter.notifyDataSetChanged();
    }
}
