package pt.ulisboa.tecnico.cmu.ubibike;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmu.ubibike.common.CommonWithButtons;
import pt.ulisboa.tecnico.cmu.ubibike.common.MapsCoordinates;
import pt.ulisboa.tecnico.cmu.ubibike.domain.PointsTransfer;


import static com.ubibike.Constants.*;



public class WifiDirectActivity extends CommonWithButtons implements
        SimWifiP2pManager.PeerListListener, SimWifiP2pManager.GroupInfoListener {

    public static final String TAG = "msgsender";

    // Wifi Direct Common Variables
    protected SimWifiP2pManager mManager = null;
    protected SimWifiP2pManager.Channel mChannel = null;
    protected Messenger mService = null;
    protected boolean mBound = false;
    protected SimWifiP2pSocketServer mSrvSocket = null;
    protected SimWifiP2pSocket mCliSocket = null;
    protected ReceiveCommTask mComm = null;
    protected SimWifiP2pBroadcastReceiverList mReceiver;
    private String mMessage;
    private String personName;
    private ArrayList<PointsTransfer> pointsExchange = new ArrayList<>();
    private HashMap<String, String> mPoints;

    private MyLocationListener locationListener;

    private boolean isDetectingBike;
    protected boolean mightBeRiding = false;
    protected boolean mightBeEnding = false;
    protected boolean isRiding = false;
    protected LocationManager lm;


    private LatLng previewsCoord = new LatLng(0, 0);
    ArrayList<LatLng> coordinatesPerRide = null;

    public SimWifiP2pManager getManager() {
        return mManager;
    }

    //public Channel getChannel() {
    //	return mChannel;
    //}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize the UI
        setContentView(R.layout.activity_score_history);
        searchForBike();

        app = ((UbiBikeApplication) getApplication());
        isDetectingBike = app.isDetectingBike();

        /**
         *  get common variables WIFI DIRECT
         *
         */

        mManager = app.getmManager();
        mChannel = app.getmChannel();
        mService = app.getmService();
        mBound = app.ismBound();
        mSrvSocket = app.getmSrvSocket();
        mCliSocket = app.getmCliSocket();
        mComm = app.getmComm();
        mReceiver = app.getmReceiver();

        /**
         *
         *
         */


        // initialize the WDSim API
        SimWifiP2pSocketManager.Init(getApplicationContext());

        // register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
        mReceiver = new SimWifiP2pBroadcastReceiverList(this);
        registerReceiver(mReceiver, filter);

        setUbiconnectText(app.getNumberOfUnreadMessages());

        // get location updates
        getLocationUpdates();
        coordinatesPerRide = app.getCoordinatesPerRide();


        runTimeTask();


    }
    private void resetRideVariables() {
        isDetectingBike = false;
        mightBeRiding = false;
        mightBeEnding = false;
        isRiding = false;
    }

    protected void runTimeTask() {
        handler.postAtTime(timeTask, SystemClock.uptimeMillis() + 100);
    }

    private Runnable timeTask = new Runnable() {
        public void run() {
            setUbiconnectText(app.getNumberOfUnreadMessages());

            Intent intent = new Intent(getApplicationContext(), SimWifiP2pService.class);
            if(!app.ismBound()) {
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                mBound = true;
                app.setmBound(true);
            }

            // spawn the chat server background task
            new IncommingCommTask().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR);

        }
    };

    @Override
    public void onPause() {
        super.onPause();

        // TODO: 27-Apr-16 quando o WIFI-DIRECT estiver em todas as actividades, nao queremos fazer unbind

        if (mCliSocket != null) {
            try {
                mCliSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mCliSocket = null;

        if (mSrvSocket != null) {
            try {
                mSrvSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mSrvSocket = null;
// TODO: 02-May-16 maybe replace client socket instead of setting a null.... have  a client socket of the application so it does not closes when switching apps

        app.setmManager(mManager);
        app.setmChannel(mChannel);
        app.setmService(mService);
        // TODO: 02-May-16 tem que estar a true
        Log.d("isBound", mBound+"");
//        app.setmBound(mBound);
        app.setmSrvSocket(mSrvSocket);
        app.setmCliSocket(mCliSocket);
        app.setmComm(mComm);
        app.setmReceiver(mReceiver);

    }
    @Override
    public void onDestroy(){
        super.onDestroy();

        unregisterReceiver(mReceiver);

    }



    protected ServiceConnection mConnection = new ServiceConnection() {
        // callbacks for service binding, passed to bindService()

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mManager = new SimWifiP2pManager(mService);
            mChannel = mManager.initialize(getApplication(), getMainLooper(), null);
            mBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            mManager = null;
            mChannel = null;
            mBound = false;
        }
    };


	/*
	 * Asynctasks implementing message exchange
	 */

    public class IncommingCommTask extends AsyncTask<Void, SimWifiP2pSocket, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            Log.d(TAG, "IncommingCommTask started (" + this.hashCode() + ").");

            mSrvSocket = app.getmSrvSocket();
            if (mSrvSocket == null) {
                try {
                    mSrvSocket = new SimWifiP2pSocketServer(
                            Integer.parseInt(getString(R.string.port)));
                    app.setmSrvSocket(mSrvSocket);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            while ((!Thread.currentThread().isInterrupted())) {
                try {
                    SimWifiP2pSocket sock = mSrvSocket.accept();
                    if (mCliSocket != null && mCliSocket.isClosed()) {
                        mCliSocket = null;
                    }
                    if (mCliSocket != null) {
                        Log.d(TAG, "Closing accepted socket because mCliSocket still active.");
                        sock.close();
                    } else {
                        publishProgress(sock);
                    }
                } catch (IOException e) {
                    Log.d("Error socket:", e.getMessage());
                    break;
                    //e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(SimWifiP2pSocket... values) {
            mCliSocket = values[0];

            // TODO: 27-Apr-16 ANOTHER USER HAS STARTED A CONVERSATION WITH YOU
            mComm = new ReceiveCommTask();

            mComm.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mCliSocket);
        }
    }

    public class ReceiveCommTask extends AsyncTask<SimWifiP2pSocket, String, Void> {
        SimWifiP2pSocket s;

        @Override
        protected Void doInBackground(SimWifiP2pSocket... params) {
            BufferedReader sockIn;
            String st;

            s = params[0];

            try {
                sockIn = new BufferedReader(new InputStreamReader(s.getInputStream()));

                while ((st = sockIn.readLine()) != null) {
                    publishProgress(st);
                }
            } catch (IOException e) {
                Log.d("Error reading socket:", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {

            if (isMessageExchange(values[0])) {
//                mTextOutput.append(mMessage+"\n");
                Message m = new Message();
                m.setBody(mMessage);
                m.setUserId(personName);
                // update unread messages
                HashMap<String, ArrayList<Message>> unreadMessages = app.getUnreadMessages();
                // TODO: 02-May-16 trocar para try catch
                Log.d("person name", personName);
                if (unreadMessages.containsKey(personName)){
                    Log.d("personName state", "contains");
                        // adicionar "m" às mensagens por ler
                    if (unreadMessages.get(personName) != null) {
                        Log.d("personName state", "contains and not null");
                        unreadMessages.get(personName).add(m);
                    } else {
                        Log.d("personName state", "contains but is null");
                        ArrayList<Message> msgList = new ArrayList<>();
                        msgList.add(m);
                        unreadMessages.put(personName,msgList);
                    }
                } else {
                    Log.d("personName state", "does NOT contain");
                    ArrayList<Message> msgList = new ArrayList<>();
                    msgList.add(m);
                    unreadMessages.put(personName,msgList);
                }
                    // increase the number of unread messages
                app.increaseNumberOfUnreadMessages();
                app.setUnreadMessages(unreadMessages);

//                Log.d("UnReadMessages", unreadMessages.size()+"");
                Toast.makeText(WifiDirectActivity.this, "New Message from " + personName,
                        Toast.LENGTH_SHORT).show();

                setUbiconnectText(app.getNumberOfUnreadMessages());

            }
            Log.d("esta no prog ", "progress");

        }

        @Override
        protected void onPostExecute(Void result) {
            if (!s.isClosed()) {
                try {
                    s.close();
                }
                catch (Exception e) {
                    Log.d("Error closing socket:", e.getMessage());
                }
            }
            s = null;
            try {
                sendPointsExchangeToServer();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private void sendPointsExchangeToServer() throws JSONException {

        /**
         *  analisar a pointsExchange
         *
         *  se PointsTransfer.EARNED_FROM_A_PEER
         *     -- invocar o addPoints
         *  se PointsTransfer.SENT_TO_A_PEER,
         *     -- invocar o decreasePoints
         */

        if (!pointsExchange.isEmpty()) {
            for (PointsTransfer pts :
                    pointsExchange) {
                if (pts.getMode() == PointsTransfer.EARNED_FROM_A_PEER) {
                    int points = pts.getPoints(); // get received points
                    JSONObject json = pts.getJson(); // get json

                    String pointsOrigin = json.getString(POINTS_ORIGIN); // get message to be displayed on our app
                    String pointsSender = json.getString(USER_WIFI); // user that sent us the points
                    // invoke the async taks to tell the server to increase our points
                    addPoints(points, pointsOrigin, pointsSender);
                }
            }
            pointsExchange.clear();

        }
    }


	/*
	 * Listeners associated to Termite
	 */

    @Override
    public void onPeersAvailable(SimWifiP2pDeviceList peers) {
        Log.d("requested bikes", "lel");

        // compile list of devices in range
        for (SimWifiP2pDevice device : peers.getDeviceList()) {
            String devstr = device.deviceName + " - " + device.getVirtIp();
            String bikeNumber = device.deviceName.replace("bike","");
            // TODO: 10-May-16 para teste, procura sempre pela bicicleta 1
//            if (bikeNumber.equals(app.getBikeReservedID())){
            if (bikeNumber.equals("1")){
                isDetectingBike = true;
                app.setDetectingBike(true);
                return;
            }
        }
        app.setDetectingBike(false);
        isDetectingBike = false;

    }
    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices,
                                     SimWifiP2pInfo groupInfo) {

//        // compile list of network members
//        StringBuilder peersStr = new StringBuilder();
//        for (String deviceName : groupInfo.getDevicesInNetwork()) {
//            SimWifiP2pDevice device = devices.getByName(deviceName);
//            String devstr = "" + deviceName + " (" +
//                    ((device == null)?"??":device.getVirtIp()) + ")\n";
//            peersStr.append(devstr);
//        }
//
//        // display list of network members
//        new AlertDialog.Builder(this)
//                .setTitle("Devices in WiFi Network")
//                .setMessage(peersStr.toString())
//                .setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                    }
//                })
//                .show();
    }

    /**
     * if it's a message exchange it will return true
     * if it's a points exchange it will rerturn false
     * @param receivedMessage
     * @return	true - message
     * 			false - points
     */
    protected boolean isMessageExchange(String receivedMessage) {
        // create the json object from the String
        JSONObject jsondata = null;
        try {
            jsondata = new JSONObject(receivedMessage);
            // get the type of message to know what the other user wants
            String type = jsondata.getString(COMMUNICATION_TYPE_WIFI);
            // if the type is a message, display the message on the screen
            if (type.equals(SEND_MESSAGE_WIFI)) {
                mMessage = jsondata.getString(MESSAGE_WIFI);
                Log.d("received msg ", mMessage);
                return true;

                // if the type is a give points, connect to the server and update my points
            } else if (type.equals(GIVE_POINTS_WIFI)) {
                // TODO: 22-Apr-16 implement this with chains
                // get the points received
                String points = jsondata.getString(POINTS_WIFI);
                // TODO: 27-Apr-16 user points sender when using digital signatures
                // get the user that send the points
                String pointsSender = jsondata.getString(USER_WIFI);
                // get the origin of the received points
                String origin = jsondata.getString(POINTS_ORIGIN);
                // put the pair <points,origin> on the mPoints that keeps the history of the score

                applyReceivedPoints(points, origin, pointsSender, jsondata);
                Log.d("received pts ", points);

                return false;
            } else if (type.equals(SEND_INFO_WIFI)) {
                personName = jsondata.getString(NAME_WIFI);
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }


    protected void applyReceivedPoints(String points, String pointsOrigin, String pointsSender, JSONObject json) {
        // TODO: 27-Apr-16 move this line to after the connection with the peer is terminated
//        addPoints(Integer.parseInt(points), pointsOrigin, pointsSender);

        // create an PointsTransfer object that contains the transaction
        PointsTransfer pts = new PointsTransfer(PointsTransfer.EARNED_FROM_A_PEER, Integer.parseInt(points), pointsSender, json);
        // add the transaction to the pointsExchange log
        pointsExchange.add(pts);
        int scoreUpdate = Integer.parseInt(bikerScore) + Integer.parseInt(points);
        setBikerScore(scoreUpdate);
    }


    protected void addPoints(int points, String pointsOrigin, String senderOfPoints) {

        AddPoints addPointsTask = new AddPoints(points, pointsOrigin, senderOfPoints);
        // task.execute().get() is used to wait for the task to be executed
        // so we can update the user score and score history
        try {
            addPointsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    protected class AddPoints extends AsyncTask<Void, Void, Void> {
        protected DataOutputStream dataOutputStream;
        protected DataInputStream dataInputStream;
        protected JSONObject json;
        protected Socket socket;
        protected int points;
        protected String pointsOrigin;
        protected String senderOfPoints;
        private boolean addPointsResult;

        public AddPoints(int points, String pointsOrigin, String senderOfPoints) {
            this.points = points;
            this.pointsOrigin = pointsOrigin;
            this.senderOfPoints = senderOfPoints;
        }

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
                json.put(REQUEST_TYPE, ADD_POINTS);
                json.put(CLIENT_NAME, bikerName);
                json.put(POINTS_TO_ADD, String.valueOf(points));
                json.put(POINTS_ORIGIN, pointsOrigin);
                json.put(USER_WIFI, senderOfPoints);

                dataOutputStream = new DataOutputStream(
                        socket.getOutputStream());

                dataInputStream = new DataInputStream(
                        socket.getInputStream());

                // transfer JSONObject as String to the server
                dataOutputStream.writeUTF(json.toString());

                // Thread will wait till server replies
                String response = dataInputStream.readUTF();
                if (!response.equals(POINTS_ADDED)) {
                    addPointsResult = false;
                } else {
                    // TODO: 01-May-16 guardar historico da troca de pontos
                        // chaves nao sao unicas, arranjar outra implementacao
//                    mPoints.put(String.valueOf(points), pointsOrigin);

                    addPointsResult = true;
                }
                // TODO: 27-Apr-16 should we waint for the server response?


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

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if(addPointsResult) {
                pointsButton.setText(app.getBikerScore(false));

                Toast.makeText(getApplicationContext(), "Points added",
                        Toast.LENGTH_SHORT).show();
            } else
            {
                Toast.makeText(getApplicationContext(), "Could NOT add the points",
                        Toast.LENGTH_SHORT).show();
            }

        }

    }

    private final class MyLocationListener implements LocationListener {


        @Override
        public void onLocationChanged(Location location) {
            // called when the listener is notified with a location update from the GPS
            Double lat = location.getLatitude();    // latitude
            Double lng = location.getLongitude();   // longitude

            coordinatesPerRide = app.getCoordinatesPerRide();
//            if (lat != previewsCoord.latitude && lng != previewsCoord.longitude) {
                LatLng newCoords = new LatLng(lat, lng);
                if (isRiding) {
                    coordinatesPerRide.add(newCoords);
                }
                Log.d("latitude maps ", lat+"");
                Log.d("longitude maps ", lng+"");

                app.setCoordinatesPerRide(coordinatesPerRide);

                for (LatLng pt :
                        coordinatesPerRide) {
                    Log.d("pt", pt+"");
                }
                searchForBike();

//                try {
////                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

                if (isNearSomeStation(newCoords)) {
                    Log.d("isNearSomeStation", "--");
                    if (!isRiding) {
                        Log.d("isNearSomeStation", "isNorRiding");
                        if (isDetectingBike) {
                            mightBeRiding = true;
                            Log.d("isNearSomeStation", "isNorRiding,DetectsBike");
                            // talvez guardar a coordenada e depois contabiliza-la se estiver eventualmente a correr
                        } else {
                            Log.d("isNearSomeStation", "isNorRiding NOT DetectingBike");
                        }
                    } else {
                        // TODO: 10-May-16 check detects bike
                        mightBeEnding = true;
                        Log.d("mighBeEnding", "--");

                    }
                } else if (isRiding) {
                    Log.d("isRiding", "");
                    if (mightBeEnding) {
                        Log.d("isRiding", "mighBeEnding");
                        if (!isDetectingBike) {
                            Log.d("isRiding", "mighBeEnding, Ended");
                            resetRideVariables();
                            SendNewRide sendNewRideTask = new SendNewRide();

                            // task.execute().get() is used to wait for the task to be executed
                            // so we can update the user score and score history
                            try {
                                sendNewRideTask.execute().get();
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.d("isRding", "mighBeEnding, detectsBike");
                        }
                    } else {
                        Log.d("isRiding", "NOTmighBeEnding(DidNOTComeAcrossAnyStation");
                    }
                    // if the user is neither riding nor near a station, we need to check if
                    // the user mightBeRiding (was on a station and detected the bike)
                } else if (mightBeRiding) {
                    Log.d("mighBeRiding", "--");

                    // if it detects the bike, he is riding
                    if (isDetectingBike) {
                        Log.d("mighBeRiding", "NOT in STATION,isDetectingBike");
                        isRiding = true;
                        // TODO: 10-May-16 account trajectory
                    } else {
                        Log.d("mighBeRiding", "NOT in STATION,NOTDetectingBike");

                        // as the user is not detecting the bike, is may not be riding
                        mightBeRiding = false;
                    }
                }
                Log.d("terminou", "loc");


//            }
//            previewsCoord = new LatLng(lat, lng);



        }

        @Override
        public void onProviderDisabled(String provider) {
            // called when the GPS provider is turned off (user turning off the GPS on the phone)
        }

        @Override
        public void onProviderEnabled(String provider) {
            // called when the GPS provider is turned on (user turning on the GPS on the phone)
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // called when the status of the GPS provider changes
        }
    }
    protected void searchForBike() {
        Log.d("boundSFB", mBound+"");
        if (mBound) {
            mManager.requestPeers(mChannel, WifiDirectActivity.this);
        } else {
            Toast.makeText(this, "Service not bound",
                    Toast.LENGTH_SHORT).show();
        }
    }

    protected boolean isNearSomeStation (LatLng mylocation) {

        for (MapsCoordinates location :
                app.getBikeStations().values()) {

            if (distance(mylocation.latitude, mylocation.longitude,   location.getLatitude(), location.getLongitude()) < 0.1) {
                return true;
            }

        }
        return false;
    }

    /** calculates the distance between two locations*/
    protected double distance(double lat1, double lng1, double lat2, double lng2) {

        double earthRadius = 3958.75; // in miles, change to 6371 for kilometers

        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double dist = earthRadius * c;

        return dist;
    }



    private void getLocationUpdates() {
        locationListener = new MyLocationListener();
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // check permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_CHECKING_PERIOD, GPS_CHECKING_DISTANCE, locationListener);
    }




    private class SendNewRide extends AsyncTask<Void, Void, Void> {
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
                json.put(REQUEST_TYPE, ADD_RIDE);
                json.put(CLIENT_NAME, bikerName);

                // create XML representing the bike stations
                Element ridesHistoryXML = new Element("newRide");
                Document doc = new Document(ridesHistoryXML);

                ArrayList<LatLng> trajectory = app.getCoordinatesPerRide();

                int i = 0;
                for (LatLng ride:
                        trajectory) {

                    Element coordinates = new Element("coordinate");
                    coordinates.setAttribute(new Attribute("id", String.valueOf(i)));

                    coordinates.addContent(new Element("latitude")
                            .setText(String.valueOf(ride.latitude)));
                    coordinates.addContent(new Element("longitude")
                            .setText(String.valueOf(ride.longitude)));


                    doc.getRootElement().addContent(coordinates);

                    i++;

                }

                // create a string from the xml
                XMLOutputter xmlOutput = new XMLOutputter();
                String rideString = xmlOutput.outputString(doc);
                System.out.println("rides of " + bikerName);
                System.out.println("ride " + rideString);
                // put the xml with the bike stations on the json object
                json.put(RIDE_INFO, rideString);


                dataOutputStream = new DataOutputStream(
                        socket.getOutputStream());

                dataInputStream = new DataInputStream(
                        socket.getInputStream());

                // transfer JSONObject as String to the server
                dataOutputStream.writeUTF(json.toString());

                // Thread will wait till server replies
                final String response = dataInputStream.readUTF();

                // clean the trajectory
                app.setCoordinatesPerRide(new ArrayList<LatLng>());

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
}