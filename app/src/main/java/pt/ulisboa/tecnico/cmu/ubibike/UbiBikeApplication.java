package pt.ulisboa.tecnico.cmu.ubibike;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Messenger;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmu.ubibike.common.MapsCoordinates;
import pt.ulisboa.tecnico.cmu.ubibike.domain.PointsTransfer;

import static com.ubibike.Constants.*;


public class UbiBikeApplication extends Application {

    private String _username;
    private String _bikerScore;
    private boolean _status;
    private SharedPreferences prefs;
    private Editor editor;

    /**
     *
     * WIFI DIRECT
     */


    public SharedPreferences getPrefs() {
        return prefs;
    }

    public void setPrefs(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public Editor getEditor() {
        return editor;
    }

    public void setEditor(Editor editor) {
        this.editor = editor;
    }

    public SimWifiP2pManager getmManager() {
        return mManager;
    }

    public void setmManager(SimWifiP2pManager mManager) {
        this.mManager = mManager;
    }

    public SimWifiP2pManager.Channel getmChannel() {
        return mChannel;
    }

    public void setmChannel(SimWifiP2pManager.Channel mChannel) {
        this.mChannel = mChannel;
    }

    public Messenger getmService() {
        return mService;
    }

    public void setmService(Messenger mService) {
        this.mService = mService;
    }

    public SimWifiP2pSocketServer getmSrvSocket() {
        return mSrvSocket;
    }

    public void setmSrvSocket(SimWifiP2pSocketServer mSrvSocket) {
        this.mSrvSocket = mSrvSocket;
    }

    public SimWifiP2pSocket getmCliSocket() {
        return mCliSocket;
    }

    public void setmCliSocket(SimWifiP2pSocket mCliSocket) {
        this.mCliSocket = mCliSocket;
    }

    public WifiDirectActivity.ReceiveCommTask getmComm() {
        return mComm;
    }

    public void setmComm(WifiDirectActivity.ReceiveCommTask mComm) {
        this.mComm = mComm;
    }

    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private Messenger mService = null;
    private boolean mBound = false;
    private SimWifiP2pSocketServer mSrvSocket = null;
    private SimWifiP2pSocket mCliSocket = null;
    private WifiDirectActivity.ReceiveCommTask mComm = null;
    private SimWifiP2pBroadcastReceiverList mReceiver;

    public int getNumberOfUnreadMessages() {
        return numberOfUnreadMessages;
    }

    public void setNumberOfUnreadMessages(int numberOfUnreadMessages) {
        this.numberOfUnreadMessages = numberOfUnreadMessages;
    }

    public void increaseNumberOfUnreadMessages() {
        this.numberOfUnreadMessages ++;
    }

    public void decreaseNumberOfUnreadMessages() {
        this.numberOfUnreadMessages --;
    }

    private int numberOfUnreadMessages = 0;

    public HashMap<String, ArrayList<Message>> getUnreadMessages() {
        return unreadMessages;
    }

    public void setUnreadMessages(HashMap<String, ArrayList<Message>> unreadMessages) {
        this.unreadMessages = unreadMessages;
    }

    /**
     *
     *  Unread Messages
     *
     */
    public HashMap<String, ArrayList<Message>> unreadMessages = new HashMap<>();


    public boolean ismBound() {return mBound;}

    public void setmBound(boolean mBound) {this.mBound = mBound;}

    private ArrayList<PointsTransfer> pointsExchange = new ArrayList<>();


    public HashMap<String, ArrayList<MapsCoordinates>> getRidesHistory() {
        return ridesHistory;
    }

    public void setRidesHistory(HashMap<String, ArrayList<MapsCoordinates>> ridesHistory) {
        this.ridesHistory = ridesHistory;
    }

    private HashMap<String, ArrayList<MapsCoordinates>> ridesHistory = new HashMap<>();


    public String getBikeReservedID() {
        return bikeReservedID;
    }

    public void setBikeReservedID(String bikeReservedID) {
        this.bikeReservedID = bikeReservedID;
    }

    private String bikeReservedID = "0";


    public HashMap<String, MapsCoordinates> getBikeStations() {
        return bikeStations;
    }

    private HashMap<String, MapsCoordinates> bikeStations = new HashMap<>();

    public boolean isDetectingBike() {
        return isDetectingBike;
    }

    public void setDetectingBike(boolean detectingBike) {
        isDetectingBike = detectingBike;
    }

    private boolean isDetectingBike = false;




    // <lat, long>
    private ArrayList<LatLng> coordinatesPerRide = new ArrayList<>();
    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private ArrayList<String> DUMMY_CREDENTIALS;

    /**
     * Constants
     */
    // Shared preferences file name
    public static final String SHARED_PREFERENCE_FILENAME = "UbibikeSP";
    public static final String SP_USERNAME = "Username";
    public static final String SP_IS_USER_LOGGED = "IsLogged";

    /**
     * Constructor
     */
    public UbiBikeApplication() {
        String[] credentialsArray = {
                "foo@example.com:hello", "bar@example.com:world",
                "isabel:costa", "pedro:dias", "vicente:rocha"
        };
        DUMMY_CREDENTIALS = new ArrayList<>(Arrays.asList(credentialsArray));
        String alameda = "Alameda Station";

        double latitudeAlameda = 38.737104;
        double longitudeAlameda= -9.140560;

        bikeStations.put(alameda, new MapsCoordinates(latitudeAlameda,longitudeAlameda));
        String campoPequeno = "Campo Pequeno Station";

        double latitudeCampPeq = 38.743096;
        double longitudeCampPeq = -9.148070;

        bikeStations.put(campoPequeno, new MapsCoordinates(latitudeCampPeq, longitudeCampPeq));
        String picoas = "Picoas Station";

        double latitudePicoas = 38.731033;
        double longitudePicoas = -9.147309;

        bikeStations.put(picoas, new MapsCoordinates(latitudePicoas, longitudePicoas));


    }



    public ArrayList<LatLng> getCoordinatesPerRide() {
        return coordinatesPerRide;
    }

    public void setCoordinatesPerRide(ArrayList<LatLng> coordinatesPerRide) {
        this.coordinatesPerRide = coordinatesPerRide;
    }

    //Getters

    public String getUsername() {
        return _username;
    }

    public boolean isStatus() {
        return _status;
    }


    public List<String> getDummyCredentials() {
        return DUMMY_CREDENTIALS;
    }

    //Setters

    public void setUsername(String username, boolean fromSharedPreferences) {
        if (fromSharedPreferences){
            // creating an shared Preference file for the information to be stored
            prefs = getApplicationContext().getSharedPreferences(SHARED_PREFERENCE_FILENAME, MODE_PRIVATE);
            this._username = prefs.getString(SP_USERNAME, null);
        } else {
            this._username = username;
        }
    }

    public void setStatus(boolean status) {
        this._status = status;
    }


    public void addCredentials(String credentials) {
        this.DUMMY_CREDENTIALS.add(credentials);
    }

    public List<String> getDummyCredentials(String credentials) {
        return this.DUMMY_CREDENTIALS;
    }

    public void sendTrajectory(long distance, String date, long duration) {

    }


    public ArrayList<PointsTransfer> getPointsExchange() {
        return pointsExchange;
    }

    public void setPointsExchange(ArrayList<PointsTransfer> pointsExchange) {
        this.pointsExchange = pointsExchange;
    }

    public void logout() {
        SharedPreferences pref = getSharedPreferences(SHARED_PREFERENCE_FILENAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        // Clearing all user data from Shared Preferences
        editor.clear();
        editor.commit();

        Intent intent = new Intent(this, LoginActivity.class);

        // Closing all the Activities
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }

    public void saveCredentials(String username) {

        // creating an shared Preference file for the information to be stored
        prefs = getApplicationContext().getSharedPreferences(SHARED_PREFERENCE_FILENAME, MODE_PRIVATE);

        // get editor to edit in file
        editor = prefs.edit();

        // as now we have information in string. Lets stored them with the help of editor
        editor.putString(SP_USERNAME, username);
        editor.putBoolean(SP_IS_USER_LOGGED, true);
        editor.commit();
    }

    // Check for login
    public boolean isUserLoggedIn(){
        // creating an shared Preference file for the information to be stored
        prefs = getApplicationContext().getSharedPreferences(SHARED_PREFERENCE_FILENAME, MODE_PRIVATE);
        Boolean isLogged = prefs.getBoolean(SP_IS_USER_LOGGED, false);
        return isLogged;
    }

    public void saveBikerScore(String bikerScore, boolean fromSharedPreferences) {

        if (fromSharedPreferences){

            // creating an shared Preference file for the information to be stored
            prefs = getApplicationContext().getSharedPreferences(SHARED_PREFERENCE_FILENAME, MODE_PRIVATE);

            // get editor to edit in file
            editor = prefs.edit();

            // as now we have information in string. Lets stored them with the help of editor
            editor.putString(PREF_BIKER_SCORE, bikerScore);

            //todo consider using apply()
            editor.commit();

        } else {
            _bikerScore = bikerScore;
        }
    }


    public String getBikerScore(boolean fromSharedPreferences) {

        if (fromSharedPreferences){

            prefs = getApplicationContext().getSharedPreferences(SHARED_PREFERENCE_FILENAME, MODE_PRIVATE);

        String bikerScore = prefs.getString(PREF_BIKER_SCORE, _bikerScore);

        return bikerScore;
        }
        else
        {

            GetPoints getPointsTask = new GetPoints();
            // task.execute().get() is used to wait for the task to be executed
            // so we can update the user score and score history
            try {
                getPointsTask.execute().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return _bikerScore;
        }
    }

    public void saveBikerScoreHistory(String bikerScoreHistory) {

        // creating an shared Preference file for the information to be stored
        prefs = getApplicationContext().getSharedPreferences(SHARED_PREFERENCE_FILENAME, MODE_PRIVATE);

        // get editor to edit in file
        editor = prefs.edit();

        // as now we have information in string. Lets stored them with the help of editor
        editor.putString(PREF_SCORE_HISTORY, bikerScoreHistory);

        //todo consider using apply()
        editor.commit();
    }


    public String getBikerScoreHistory() {

        prefs = getApplicationContext().getSharedPreferences(SHARED_PREFERENCE_FILENAME, MODE_PRIVATE);

        String bikerScoreHistory = prefs.getString(PREF_SCORE_HISTORY, PREF_SCORE_HISTORY_DEFAULT);

        return bikerScoreHistory;
    }

    public void cleanBikerScoreHistory() {

        // creating an shared Preference file for the information to be stored
        prefs = getApplicationContext().getSharedPreferences(SHARED_PREFERENCE_FILENAME, MODE_PRIVATE);

        // get editor to edit in file
        editor = prefs.edit();

        // remove the score history
        editor.remove(PREF_SCORE_HISTORY);

        //todo consider using apply()
        editor.commit();
    }

    public SimWifiP2pBroadcastReceiverList getmReceiver() {
        return mReceiver;
    }

    public void setmReceiver(SimWifiP2pBroadcastReceiverList mReceiver) {
        this.mReceiver = mReceiver;
    }

    private class GetPoints extends AsyncTask<Void, Void, Void> {
            private DataOutputStream dataOutputStream;
            private DataInputStream dataInputStream;
            private JSONObject json;
            private Socket socket;

            @Override
            protected Void doInBackground(Void... params) {


                try {
                    socket = new Socket();
                    InetAddress[] iNetAddress = InetAddress.getAllByName(SERVER_IP);
                    SocketAddress address = new InetSocketAddress(iNetAddress[0], SERVER_PORT);

                    socket.setSoTimeout(5000); //timeout for all other I/O operations, 10s for example
                    socket.connect(address, 10000); //timeout for attempting connection, 20 s

//                    socket = new Socket(SERVER_IP, SERVER_PORT);
                } catch (IOException e) {
                    return null;
                }

                try {
                    json = new JSONObject();
                    json.put(REQUEST_TYPE, GET_POINTS);
                    json.put(CLIENT_NAME, _username);


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

                    _bikerScore = jsondata.getString(POINTS);



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
                            System.out.print("closing the socket");
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



///*
//    /**
//     * Gets username of logged user from Shared Preferences
//     * @return username
//     */
///*
//    public String getUsernameFromSP(){
//        // creating an shared Preference file for the information to be stored
//        prefs = getApplicationContext().getSharedPreferences(SHARED_PREFERENCE_FILENAME, MODE_PRIVATE);
//        String username = prefs.getString(SP_USERNAME, null);
//        return username;
//    }
//*/
///*
//    public void bottomMenuClickAction(Activity activity){
//        if(!(activity.getClass().isInstance(UserDashboard.class))){
//            Button homeBtn = (Button) activity.findViewById(R.id.menu_bottom_home);
//            homeBtn.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    Intent intent = new Intent(getApplicationContext(), UserDashboard.class);
//                    startActivity(intent);
//                }
//            });
//        }
//
//        if(!(activity.getClass().isInstance(Chat.class))){
//            Button chatBtn = (Button) activity.findViewById(R.id.menu_bottom_messenger);
//            chatBtn.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    Intent intent = new Intent(getApplicationContext(), Chat.class);
//                    startActivity(intent);
//                }
//            });
//        }
//
//        if(!(activity.getClass().isInstance(OptionsMenu.class))){
//            Button optionsBtn = (Button) activity.findViewById(R.id.menu_bottom_options);
//            optionsBtn.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                    Intent intent = new Intent(getApplicationContext(), OptionsMenu.class);
//                    startActivity(intent);
//                }
//            });
//        }
//    }*/


}
