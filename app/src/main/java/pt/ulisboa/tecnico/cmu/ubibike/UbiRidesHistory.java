package pt.ulisboa.tecnico.cmu.ubibike;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import pt.ulisboa.tecnico.cmu.ubibike.common.MapsCoordinates;

import static com.ubibike.Constants.*;


public class UbiRidesHistory extends WifiDirectActivity {

    private ListView trajectoryList;
    private ArrayAdapter<String> arraylistAdapter;
    private String bikerName;
    private List<String> trajectoriesArray;
    private HashMap<String, ArrayList<MapsCoordinates>> ridesHistory = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stations_list);

        this.bikerName = ((UbiBikeApplication) getApplication()).getUsername();

        // HEADER
        // biker name
        TextView bikerNameTextView = (TextView)findViewById(R.id.biker_name);
        bikerNameTextView.setText(bikerName);


        trajectoryList = (ListView) findViewById(R.id.trajectory_list_view);

        trajectoriesArray = new ArrayList<>();

        arraylistAdapter = new ArrayAdapter<>(
                getApplicationContext(),
                R.layout.activity_stations_list_item,     //listView item layout
                R.id.trajectory_list_view_item,             //listView id
                trajectoriesArray
        );

        trajectoryList.setAdapter(arraylistAdapter);

        // refresh the stations list
        handler.postAtTime(timeTask, SystemClock.uptimeMillis() + 100);

        //listAdapter.notifyDataSetChanged();

        trajectoryList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long row)
            {
                Intent intent = new Intent(UbiRidesHistory.this, UbiRidesHistoryMapsActivity.class);

                String bikeStationLocation = (String) adapter.getItemAtPosition(position);

                String bikeStationLocationParsed = bikeStationLocation.replace(position+1 + "- ","");

                // todo remove log
                Log.i("bikeStationLocPars", bikeStationLocationParsed);


                intent.putExtra(INTENT_RIDE_NUMBER, String.valueOf(position));

                startActivity(intent);
                //String value = (String)adapter.getItemAtPosition(position);
                // assuming string and if you want to get the value on click of list item
                // do what you intend to do on click of listview row
            }
        });
    }


    private Runnable timeTask = new Runnable() {
        public void run() {

            GetRidesHistory getRidesHistoryTask = new GetRidesHistory();

            // task.execute().get() is used to wait for the task to be executed
            // so we can update the user score and score history
            try {
                getRidesHistoryTask.execute().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            arraylistAdapter.notifyDataSetChanged();



        }
    };


    private class GetRidesHistory extends AsyncTask<Void, Void, Void> {
        private DataOutputStream dataOutputStream;
        private DataInputStream dataInputStream;
        private JSONObject json;

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
                json.put(REQUEST_TYPE, GET_RIDES_HISTORY);
                json.put(CLIENT_NAME, bikerName);

                dataOutputStream = new DataOutputStream(
                        socket.getOutputStream());

                dataInputStream = new DataInputStream(
                        socket.getInputStream());

                // transfer JSONObject as String to the server
                dataOutputStream.writeUTF(json.toString());

                // Thread will wait till server replies
                String response = dataInputStream.readUTF();


                final JSONObject jsondata;
                jsondata = new JSONObject(response);


                String stationsListString = jsondata.getString(RIDES_HISTORY_LIST);

                // instanciate SAXBuilder to parse the String to XML
                SAXBuilder builder = new SAXBuilder();
                // create a stream to be converted to a JDOM document
                InputStream stream = new ByteArrayInputStream(stationsListString.getBytes("UTF-8"));
                // create JDOM document from stream
                Document stationsList = builder.build(stream);

                // get root node to travel the XML file and get each station attributes
                Element rootNode = stationsList.getRootElement();
                List list = rootNode.getChildren("rides");


                ArrayList<MapsCoordinates> trajectoryPoints = new ArrayList<>();
                // clear the array before updating it with the stations
                // to avoid duplicates

                trajectoriesArray.clear();

                for (int i = 0; i < list.size(); i++) {

                    Element node = (Element) list.get(i);
                    List coordList = node.getChildren("coordinates");
                    for (int a = 0; a < coordList.size(); a++) {
                        Element coord = (Element) coordList.get(a);
                        double latitude = Double.parseDouble(coord.getChildText("latitude"));
                        double longitude = Double.parseDouble(coord.getChildText("longitude"));
                        Log.d(i + " coord lt ln", coord + " "+latitude + " " + longitude);
                        trajectoryPoints.add(new MapsCoordinates(latitude,longitude));

                    }
//                    System.out.println(i+" traj" + trajectoryPoints);
                    ridesHistory.put(i+"", trajectoryPoints);
                    trajectoriesArray.add(i, "Ride #"+i);
                    trajectoryPoints = new ArrayList<>();
                }
                app.setRidesHistory(ridesHistory);


                socket.close();


            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (JDOMException e) {
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
    }

}
