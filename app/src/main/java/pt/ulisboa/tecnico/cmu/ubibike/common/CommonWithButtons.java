package pt.ulisboa.tecnico.cmu.ubibike.common;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import pt.ulisboa.tecnico.cmu.ubibike.FindPeersActivity;
import pt.ulisboa.tecnico.cmu.ubibike.OptionsMenu;
import pt.ulisboa.tecnico.cmu.ubibike.R;
import pt.ulisboa.tecnico.cmu.ubibike.ScoreHistory;
import pt.ulisboa.tecnico.cmu.ubibike.UbiBikeApplication;
import pt.ulisboa.tecnico.cmu.ubibike.UserDashboard;

import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.CLIENT_NAME;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.GET_POINTS;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.MY_PREFS;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.POINTS;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.PREF_BIKER_SCORE;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.PREF_BIKER_SCORE_DEFAULT;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.REQUEST_TYPE;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.SERVER_IP;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.SERVER_PORT;

/**
 * Created by vicente on 07-Apr-16.
 */
public class CommonWithButtons extends AppCompatActivity {

    protected String bikerName;
    protected String bikerScore;
    protected Button pointsButton;
    protected TextView bikersNameTextView;
    protected Handler handler = new Handler();
    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_history);


        // HEADER
        // biker name
        bikerName = ((UbiBikeApplication) getApplication()).getUsername();


        // Restore preferences

        UbiBikeApplication app = ((UbiBikeApplication) getApplication());
        bikerScore = app.getBikerScore();



        // Views

        pointsButton = (Button) findViewById(R.id.biker_score);
        bikersNameTextView = (TextView)findViewById(R.id.biker_name);



        // Sets
        pointsButton.setText(bikerScore);
        bikersNameTextView.setText(bikerName);


        // Get user current points and refresh Views
        // TODO: 09-Apr-16 make UbiBikeApplication check score periodically
//        handler.postAtTime(timeTask, SystemClock.uptimeMillis() + 100);


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

            pointsButton = (Button) findViewById(R.id.biker_score);
            pointsButton.setText(bikerScore);

            // every 5 minutes calls the server to check for updates
            // todo perguntar ao prof opiniao sobre isto (se ha maneira/vantagem em ser o server a iniciar a comunicacao
            handler.postAtTime(timeTask, SystemClock.uptimeMillis() + 300000 );


        }
    };


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
                json.put(REQUEST_TYPE, GET_POINTS);
                json.put(CLIENT_NAME, bikerName);


                dataOutputStream = new DataOutputStream(
                        socket.getOutputStream());

                dataInputStream = new DataInputStream(socket.getInputStream());

                // transfer JSONObject as String to the server
                dataOutputStream.writeUTF(json.toString());

                // Thread will wait till server replies
                String response = dataInputStream.readUTF();


                final JSONObject jsondata;
                jsondata = new JSONObject(response);

                bikerScore = jsondata.getString(POINTS);



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

        UbiBikeApplication app = ((UbiBikeApplication) getApplication());
        app.saveBikerScore(bikerScore);
    }

    public void launchClick(View v) {
        Intent intent = null;
        Boolean execute = true;

        switch(v.getId()) {
            case R.id.menu_bottom_home:
                intent = new Intent(this, UserDashboard.class);
                intent.putExtra("bikerName",bikerName);
                break;

            case R.id.menu_bottom_ubiconnect:
                intent = new Intent(this, FindPeersActivity.class);
                intent.putExtra("bikerName",bikerName);
                break;

            case R.id.menu_bottom_options:
                intent = new Intent(this, OptionsMenu.class);
                intent.putExtra("bikerName",bikerName);
                break;

//            Points History

            case R.id.biker_score:
                intent = new Intent(this, ScoreHistory.class);
                intent.putExtra("bikerName",bikerName);
                break;

//          Ubibike Logo
            case R.id.ubibikeLogo:
                intent = new Intent(this, UserDashboard.class);
                intent.putExtra("bikerName",bikerName);
                break;
        }
        if (execute){
            startActivityForResult(intent, 0);
        }
    }

}
