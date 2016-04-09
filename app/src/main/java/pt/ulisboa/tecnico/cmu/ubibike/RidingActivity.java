package pt.ulisboa.tecnico.cmu.ubibike;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

import pt.ulisboa.tecnico.cmu.ubibike.common.CommonWithButtons;

import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.SERVER_IP;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.SERVER_PORT;

public class RidingActivity extends CommonWithButtons {

    private TextView isRiding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riding);

        this.bikerName = ((UbiBikeApplication) getApplication()).getUsername();


        // Views
        bikersNameTextView = (TextView)findViewById(R.id.biker_name);
        isRiding = (TextView) findViewById(R.id.is_riding);


        String ridingCheckResult = "no";

        IsRidingCheck ridingCheckTask = new IsRidingCheck();
        try {
            ridingCheckResult = ridingCheckTask.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        String ridingString;
        if (ridingCheckResult.equals("yes")) {
            ridingString = bikerName + " IS RIDING";
        } else {
            ridingString = bikerName + " IS NOT RIDING";
        }


        // Sets
        bikersNameTextView.setText(bikerName);
        isRiding.setText(ridingString);


    }

        private class IsRidingCheck extends AsyncTask<Void, Void, String> {
            private DataOutputStream dataOutputStream;
            private DataInputStream dataInputStream;
            private Socket socket;
            private JSONObject json;
            protected String toastResult;
            private String result;

            @Override
            protected String doInBackground(Void... params) {
                try {
                    socket = new Socket(SERVER_IP, SERVER_PORT);
                } catch (IOException e) {

                    toastResult = bikerName + " is not riding";
                    return "no";
                }

                try {

                    json = new JSONObject();
                    json.put("type", "is riding");
                    json.put("client name", bikerName);


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


                    result = jsondata.getString("is riding");

                    if (result.equals("yes")) {
                        toastResult = bikerName + " is riding";
                    } else {
                        toastResult = bikerName + " is not riding";

                    }



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

                return result;

            }

            @Override
            protected void onPostExecute(String res) {
                super.onPostExecute(res);
//                 TODO Update the UI thread with the final result
                Toast.makeText(getApplicationContext(),
                        toastResult, Toast.LENGTH_SHORT).show();
            }
        }


}
