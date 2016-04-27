package pt.ulisboa.tecnico.cmu.ubibike;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import pt.ulisboa.tecnico.cmu.ubibike.common.CommonWithButtons;

import static com.ubibike.Constants.*;



public class ScoreHistory extends CommonWithButtons {

    private String bikerName;
    private List<String> scoreHisArray;
    private Button refreshButton;
    private ArrayAdapter scoreAdapter;
    private Button pointsButton;
    private Handler handler = new Handler();
    private String scoreHistory;
    private TextView bikersNameTextView;
    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_history);



        // Initializations

        scoreHisArray = new ArrayList<>();
        scoreHisArray.clear();

        scoreAdapter = new ArrayAdapter<>(
                getApplicationContext(),
                R.layout.score_history_list_item,
                R.id.score_list_view_item,
                scoreHisArray
        );




        // get biker name
        this.bikerName = ((UbiBikeApplication) getApplication()).getUsername();




        // Restore preferences

        UbiBikeApplication app = ((UbiBikeApplication) getApplication());

        scoreHistory = app.getBikerScoreHistory();


        if (!scoreHistory.equals(PREF_SCORE_HISTORY_DEFAULT)) {

            String[] pointsOrigin = scoreHistory.split("\t");

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



        // Get user current points and refresh Views
        // TODO: 09-Apr-16 make UbiBikeApplication check score periodically
//        handler.postAtTime(timeTask, SystemClock.uptimeMillis() + 100);


        // Button Press Event Listeners
        refreshButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                AddPoints addPointsTask = new AddPoints();
                try {
                    addPointsTask.execute().get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                // Get user current points and refresh Views
                handler.postAtTime(timeTask, SystemClock.uptimeMillis() + 1000);

            }
        });



    }

    private Runnable timeTask = new Runnable() {
        public void run() {

            GetPoints getClientsTask = new GetPoints();

            // task.execute().get() is used to wait for the task to be executed
            // so we can update the user score and score history
            try {
                getClientsTask.execute().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

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
                socket = new Socket(SERVER_IP, SERVER_PORT);
            } catch (IOException e) {
                return null;
            }

            try {

                json = new JSONObject();
                json.put(REQUEST_TYPE, ADD_POINTS);
                json.put(POINTS_TO_ADD, ADD_POINTS_TEST_10);
                json.put(POINTS_ORIGIN, ADD_POINTS_TEST_10_ORIGIN);
                json.put(CLIENT_NAME, bikerName);
                // TODO: 27-Apr-16 so funciona quando é a joana a querer adicionar
                json.put(USER_WIFI, "joao");


                dataOutputStream = new DataOutputStream(
                        socket.getOutputStream());

                dataInputStream = new DataInputStream(
                        socket.getInputStream());

                // transfer JSONObject as String to the server
                dataOutputStream.writeUTF(json.toString());

                // Thread will wait till server replies
                final String response = dataInputStream.readUTF();


                ScoreHistory.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(ScoreHistory.this, response, Toast.LENGTH_SHORT).show();
                    }
                });

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

        @Override
        protected Void doInBackground(Void... params) {


            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
            } catch (IOException e) {
                return null;
            }

            try {

                json = new JSONObject();
                json.put(REQUEST_TYPE, GET_POINTS_HISTORY);
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

                bikerScore = jsondata.getString(POINTS);
                scoreHistory = jsondata.getString(POINTS_HISTORY);

                String[] pointsOrigin = scoreHistory.split("\t");


                scoreHisArray.clear();

                for(String s : pointsOrigin) {
                    scoreHisArray.add(s);

                }



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

        //Set Preferences
        UbiBikeApplication app = ((UbiBikeApplication) getApplication());
        app.saveBikerScoreHistory(scoreHistory);
        app.saveBikerScore(bikerScore, false);
    }

    @Override
    public void launchClick(View v) {

        Intent intent = null;
        Boolean execute;

        switch(v.getId()) {

           case R.id.biker_score:
                execute = false;
                break;
            default:
                super.launchClick(v);
                return;

        }
        if (execute){
            startActivityForResult(intent, 0);
        }
    }

}
