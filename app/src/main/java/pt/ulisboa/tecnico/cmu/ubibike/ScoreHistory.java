package pt.ulisboa.tecnico.cmu.ubibike;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
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

import pt.ulisboa.tecnico.cmu.ubibike.common.Common;

public class ScoreHistory extends AboutMenu {

    private String bikerName;
    private List<String> scoreHisArray;
    private Button refreshButton;
    private ArrayAdapter scoreAdapter;
    private String serverIp = "10.0.3.2";
    private String bikerScore;
    private Button pointsButton;
    private Handler handler;
    private String score_history;
    private TextView bikersNameTextView;
    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_history);

        try {
            socket = new Socket(serverIp, 4444);
        } catch (IOException e) {
            e.printStackTrace();
        }


        // Initializations

        scoreHisArray = new ArrayList<>();
        scoreAdapter = new ArrayAdapter<>(
                getApplicationContext(),
                R.layout.score_history_list_item,
                R.id.score_list_view_item,
                scoreHisArray
        );




        // get biker name
        this.bikerName = ((UbiBikeApplication) getApplication()).getUsername();




        // Restore preferences

        SharedPreferences mPrefs;
        mPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);

        score_history = mPrefs.getString("score_history", "empty");
        bikerScore = mPrefs.getString("biker_score", "0");


        if (!score_history.equals("empty")) {

            String[] pointsOrigin = score_history.split("\t");

            for(String s : pointsOrigin) {
                scoreHisArray.add(s);
            }
            scoreAdapter.notifyDataSetChanged();

        }




        // Views

        pointsButton = (Button) findViewById(R.id.biker_score);
        bikersNameTextView = (TextView)findViewById(R.id.biker_name);
        ListView scoreHistory = (ListView) findViewById(R.id.peers_list_view);
        refreshButton = (Button) findViewById(R.id.refresh_points);




        // Sets
        pointsButton.setText(bikerScore);
        bikersNameTextView.setText(bikerName);
        scoreHistory.setAdapter(scoreAdapter);



        handler = new Handler();
        handler.postAtTime(timeTask, SystemClock.uptimeMillis() + 2000);





        // Button Press Event Listeners
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

    private Runnable timeTask = new Runnable() {
        public void run() {

            GetPoints getClientsTask = new GetPoints();
            getClientsTask.execute();

            scoreAdapter.notifyDataSetChanged();
            pointsButton = (Button) findViewById(R.id.biker_score);
            pointsButton.setText(bikerScore);

        }
    };

    private class AddPoints extends AsyncTask<Void, Void, Void> {
        private DataOutputStream dataOutputStream;
        private DataInputStream dataInputStream;
        private JSONObject json;

        @Override
        protected Void doInBackground(Void... params) {
            try {

                json = new JSONObject();
                json.put("type", "get points history");
                json.put("client name", bikerName);


                dataOutputStream = new DataOutputStream(
                        socket.getOutputStream());

                dataInputStream = new DataInputStream(socket.getInputStream());

                // transfer JSONObject as String to the server
                dataOutputStream.writeUTF(json.toString());

                // Thread will wait till server replies
                String response = dataInputStream.readUTF();


                final JSONObject jsondata;
                jsondata = new JSONObject(response);

                bikerScore = jsondata.getString("points");

                score_history = jsondata.getString("points history");

                String[] pointsOrigin = score_history.split("\t");


                scoreHisArray.clear();

                for(String s : pointsOrigin) {
                    scoreHisArray.add(s);

                }




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


    private class GetPoints extends AsyncTask<Void, Void, Void> {
        private DataOutputStream dataOutputStream;
        private DataInputStream dataInputStream;
        private JSONObject json;
        private boolean success;
        private String serverMessage;

        @Override
        protected Void doInBackground(Void... params) {

            try {

                json = new JSONObject();
                json.put("type", "get points history");
                json.put("client name", bikerName);


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

                score_history = jsondata.getString("points history");

                String[] pointsOrigin = score_history.split("\t");


                scoreHisArray.clear();

                for(String s : pointsOrigin) {
                    scoreHisArray.add(s);

                }


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


    @Override
    protected void onPause(){
        super.onPause();

//Set Preference
        SharedPreferences myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor;
        prefsEditor = myPrefs.edit();

        prefsEditor.putString("score_history", score_history);
        prefsEditor.commit();
    }

//    public void launchClick(View v) {
//        Intent intent = null;
//        Boolean execute = true;
//
//        switch(v.getId()) {
//            case R.id.menu_bottom_home:
//                intent = new Intent(ScoreHistory.this, UserDashboard.class);
//                intent.putExtra("bikerName",bikerName);
//                break;
//
//            case R.id.menu_bottom_ubiconnect:
//                intent = new Intent(ScoreHistory.this, FindPeersActivity.class);
//
//                break;
//
//            case R.id.menu_bottom_options:
//                intent = new Intent(ScoreHistory.this, OptionsMenu.class);
////                execute = false;
//                break;
//
//            //            Points History
//
//            case R.id.biker_score:
//                execute = false;
//                break;
////          Ubibike Logo
//            case R.id.ubibikeLogo:
//                intent = new Intent(ScoreHistory.this, UserDashboard.class);
//                break;
//        }
//        if (execute){
//            startActivityForResult(intent, 0);
//        }
//    }
}
