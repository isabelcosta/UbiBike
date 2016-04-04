package pt.ulisboa.tecnico.cmu.ubibike;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScoreHistory extends AppCompatActivity {

    private String bikerName;
    private List<String> scoreHisArray;
    private Button refreshButton;
    private ArrayAdapter scoreAdapter;
    private String serverIp = "10.0.3.2";
    private String bikerScore;
    private Button pointsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_history);
        this.bikerName = ((UbiBikeApplication) getApplication()).getUsername();

        TextView bikersNameTextView = (TextView)findViewById(R.id.biker_name);;
        bikersNameTextView.setText(bikerName);


        ListView scoreHistory = (ListView) findViewById(R.id.peers_list_view);

        scoreHisArray = new ArrayList<>();


        //@TODO create adapter to adapt item view to get green light and peers name
        scoreAdapter = new ArrayAdapter<String>(
                getApplicationContext(),
                R.layout.score_history_list_item,
                R.id.score_list_view_item,
                scoreHisArray
        );

        scoreHistory.setAdapter(scoreAdapter);


        refreshButton = (Button) findViewById(R.id.refresh_points); // reference to the send button

        // Button press event listener
        refreshButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                GetPoints getClientsTask = new GetPoints();
                getClientsTask.execute();
                scoreAdapter.notifyDataSetChanged();
                pointsButton = (Button) findViewById(R.id.biker_score);
                pointsButton.setText(bikerScore);
            }
        });




    }

    private class GetPoints extends AsyncTask<Void, Void, Void> {
        private DataOutputStream dataOutputStream;
        private DataInputStream dataInputStream;
        private Socket socket;
        private JSONObject json;
        private boolean success;
        private String serverMessage;

        @Override
        protected Void doInBackground(Void... params) {

            try {

                json = new JSONObject();
                json.put("type", "get points");
                json.put("client name", bikerName);

                socket = new Socket(serverIp, 4444);

                dataOutputStream = new DataOutputStream(
                        socket.getOutputStream());

                dataInputStream = new DataInputStream(socket.getInputStream());

                // transfer JSONObject as String to the server
                dataOutputStream.writeUTF(json.toString());

                // Thread will wait till server replies
                String response = dataInputStream.readUTF();
                // FIXME: 04-Apr-16 solve response
                if (response == null) {
                    success = false;
                } else {
                    success = true;
                }

                final JSONObject jsondata;
                jsondata = new JSONObject(response);

                bikerScore = jsondata.getString("points");

                scoreHisArray.add(jsondata.getString("points origin"));

                serverMessage = "ok!";


//                new BufferedWriter(new OutputStreamWriter(mySocketOutputStream, "UTF-8")));

                socket.close();


            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
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

    public void launchClick(View v) {
        Intent intent = null;
        Boolean execute = true;

        switch(v.getId()) {
            case R.id.menu_bottom_home:
                intent = new Intent(ScoreHistory.this, UserDashboard.class);
                intent.putExtra("bikerName",bikerName);
                break;

            case R.id.menu_bottom_ubiconnect:
                intent = new Intent(ScoreHistory.this, FindPeersActivity.class);

                break;

            case R.id.menu_bottom_options:
                intent = new Intent(ScoreHistory.this, OptionsMenu.class);
//                execute = false;
                break;

            //            Points History

            case R.id.biker_score:
                execute = false;
                break;
//          Ubibike Logo
            case R.id.ubibikeLogo:
                intent = new Intent(ScoreHistory.this, UserDashboard.class);
                break;
        }
        if (execute){
            startActivityForResult(intent, 0);
        }
    }
}
