package pt.ulisboa.tecnico.cmu.ubibike.common;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import pt.ulisboa.tecnico.cmu.ubibike.R;
import pt.ulisboa.tecnico.cmu.ubibike.UbiBikeApplication;

/**
 * Created by vicente on 05-Apr-16.
 */
public class Common extends AppCompatActivity {

    protected String bikerName;
    protected String serverIp = "10.0.3.2";
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

        SharedPreferences mPrefs;
        mPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);

        bikerScore = mPrefs.getString("biker_score", "0");






        // Views

        pointsButton = (Button) findViewById(R.id.biker_score);
        bikersNameTextView = (TextView)findViewById(R.id.biker_name);



        // Sets
        pointsButton.setText(bikerScore);
        bikersNameTextView.setText(bikerName);


        // Get user current points and refresh Views
        handler.postAtTime(timeTask, SystemClock.uptimeMillis() + 100);


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
                socket = new Socket(serverIp, 4444);

                json = new JSONObject();
                json.put("type", "get points");
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

        prefsEditor.putString("biker_score", bikerScore);
        prefsEditor.commit();
    }

}
