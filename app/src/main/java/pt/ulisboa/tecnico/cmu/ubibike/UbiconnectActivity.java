package pt.ulisboa.tecnico.cmu.ubibike;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
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
import pt.ulisboa.tecnico.cmu.ubibike.domain.PointsTransfer;


import static com.ubibike.Constants.*;



public class UbiconnectActivity extends CommonWithButtons implements
        SimWifiP2pManager.PeerListListener, SimWifiP2pManager.GroupInfoListener {

    public static final String TAG = "msgsender";

    // Wifi Direct Common Variables
    protected SimWifiP2pManager mManager = null;
    protected SimWifiP2pManager.Channel mChannel = null;
    protected Messenger mService = null;
    protected boolean mBound = false;
    protected SimWifiP2pSocketServer mSrvSocket = null;
    protected SimWifiP2pSocket mCliSocket = null;
    protected SimWifiP2pBroadcastReceiverList mReceiver;
    protected ReceiveCommTask mComm = null;

    private TextView mTextInput;
    //    private TextView mTextOutput;

    private String mMessage = "";
    // <points, origin>
    private HashMap<String, String> mPoints = new HashMap<>();
    private List<Message> allPeersArray;
    private ListView peersList;
    private ChatListAdapter peersAdapter;

    private ArrayList<String> peersNamesArrayList = new ArrayList<>();
    private ArrayList<String> peersIPsArrayList = new ArrayList<>();

    private String userToConnectIp;
    private TextView personView;

    // clientID, messages
    private HashMap<String, ArrayList<String>> exchangedMessagesPerClient = new HashMap<>();
    private String personName;
//    private String bikerScore;
    private Button pointsButton;
    private String myName;
    private Handler handler = new Handler();
    private String connectedUser;
    private boolean decreasePointsResult;
    private boolean addPointsResult;


    private ArrayList<PointsTransfer> pointsExchange = new ArrayList<>();

    public SimWifiP2pManager getManager() {
        return mManager;
    }



    //public Channel getChannel() {
    //	return mChannel;
    //}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize the UI
        setContentView(R.layout.activity_list_users);
        guiSetButtonListeners();
        guiUpdateInitState();
//        pointsButton = (Button) findViewById(R.id.biker_score);
//        bikerScore = app.getBikerScore(true);


        app = ((UbiBikeApplication) getApplication());

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
//                mComm = app.getmComm();

        /**
         *
         *
         */


        //      Change color to current menu
        Button messengerBtn = (Button) findViewById(R.id.menu_bottom_ubiconnect);
        messengerBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        messengerBtn.setTextColor(getResources().getColor(R.color.white));

        // initialize the WDSim API
        SimWifiP2pSocketManager.Init(getApplicationContext());

        // configure List View
        peersList = (ListView) findViewById(R.id.peers_list_view);

        this.bikerName = ((UbiBikeApplication) getApplication()).getUsername();
        myName = ((UbiBikeApplication) getApplication()).getUsername();

        allPeersArray = new ArrayList<>();
        peersAdapter = new ChatListAdapter(UbiconnectActivity.this, myName, allPeersArray);

        peersList.setAdapter(peersAdapter);
//        peersList.setBackgroundColor(Color.DKGRAY);


        // register broadcast receiver
        // TODO: 09-May-16 verificar se bastas fazer isto apenas 1 vez
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
        mReceiver = new SimWifiP2pBroadcastReceiverList(this);
        registerReceiver(mReceiver, filter);

        personView = (TextView)findViewById(R.id.person_name);
        personView.setText("");
        peersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {

                // get Name and Ip of the person that the user is trying to contact
                // TODO: 24-Apr-16 get personName from WIFI-Direct
                personName = peersNamesArrayList.get(position);
                String personIp = peersIPsArrayList.get(position);


                allPeersArray.clear();

                new OutgoingCommTask().executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR,
                        personIp);

                    // set list to NOT clickable
                peersList.setClickable(false);


                // person name
//                String displayMessage = personName + " - " + personIp;
//                String displayMessage = personName;

                HashMap<String, ArrayList<Message>> unreadMessages = app.getUnreadMessages();

                if (unreadMessages.containsKey(personName)){
                    for (Message msg :
                            unreadMessages.get(personName)) {
                        peersAdapter.add(msg);
                        app.decreaseNumberOfUnreadMessages();
                    }
                    unreadMessages.remove(personName);
                    app.setUnreadMessages(unreadMessages);
                }
                String displayMsg = "Connected to - " + personName;
                personView.setText(displayMsg);
                connectedUser = personName;

                peersAdapter.notifyDataSetChanged();

//                // activate text input
//                mTextInput.setEnabled(true);
//                mTextInput.setHint("Type a message..");
//                findViewById(R.id.idSendPointsButton).setEnabled(true);
                setUbiconnectText(app.getNumberOfUnreadMessages());

            }
        });


        // spawn the chat server background task
        new IncommingCommTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR);

        guiUpdateDisconnectedState();

//        runTimeTask();

    }

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

//        if (mBound) {
//            unbindService(mConnection);
//            mBound = false;
//        }

        app.setmManager(mManager);
        app.setmChannel(mChannel);
        app.setmService(mService);
//        app.setmBound(mBound);
        app.setmSrvSocket(mSrvSocket);
        app.setmCliSocket(mCliSocket);
//        app.setmComm(mComm);



    }
    @Override
    public void onDestroy(){
        super.onDestroy();

//        unregisterReceiver(mReceiver);

    }


	/*
	 * Listeners associated to buttons
	 */
//
//    private View.OnClickListener listenerWifiOnButton = new View.OnClickListener() {
//        public void onClick(View v){
//
//            Intent intent = new Intent(v.getContext(), SimWifiP2pService.class);
//            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
//            mBound = true;
//
//            // spawn the chat server background task
//            new IncommingCommTask().executeOnExecutor(
//                    AsyncTask.THREAD_POOL_EXECUTOR);
//
//            guiUpdateDisconnectedState();
//        }
//    };

//    private View.OnClickListener listenerWifiOffButton = new View.OnClickListener() {
//        public void onClick(View v){
//            if (mBound) {
//                unbindService(mConnection);
//                mBound = false;
//                guiUpdateInitState();
//
//                // clear list
//                allPeersArray.clear();
//                peersAdapter.notifyDataSetChanged();
//            }
//
//        }
//    };

    private View.OnClickListener listenerListPeersButton = new View.OnClickListener() {
        public void onClick(View v){
            if (mBound) {
                mManager.requestPeers(mChannel, UbiconnectActivity.this);
            } else {
                Toast.makeText(v.getContext(), "Service not bound",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    private View.OnClickListener listenerSendPointsButton = new View.OnClickListener() {
        public void onClick(View v){
//            if (mBound) {
//                mManager.requestGroupInfo(mChannel, UbiconnectActivity.this);
//            } else {
//                Toast.makeText(v.getContext(), "Service not bound",
//                        Toast.LENGTH_SHORT).show();
//            }

            String points = mTextInput.getText().toString();

            try {

                    // check if the user can send the points (checks with the server (if it's online))
                if (!checkPoints(points)) {
                    return;
                }
                String pointsOriginMessageToReceiver = "received " + points + " points from " + myName;
                String pointsOriginMessageToMe = "sent " + points + " points to " + connectedUser;
                    // invoke the async task that connects to the server and decreases the points

                // TODO: 27-Apr-16 move this code to after the server lost connection
//                decreasePoints(Integer.parseInt(points),pointsOriginMessageToMe);



                JSONObject json = new JSONObject();
                // indicate that the user is sending a message (and it is not giving points)
                json.put(COMMUNICATION_TYPE_WIFI, GIVE_POINTS_WIFI);
                json.put(POINTS_WIFI, points);
                json.put(USER_WIFI, myName);
                json.put(POINTS_ORIGIN, pointsOriginMessageToReceiver);
                json.put(POINTS_ORIGIN_TO_ME, pointsOriginMessageToMe);

                    // create an PointsTransfer object that contains the transaction
                PointsTransfer pts = new PointsTransfer(PointsTransfer.SENT_TO_A_PEER, Integer.parseInt(points), connectedUser, json);
                    // add the transaction to the pointsExchange log
                pointsExchange.add(pts);
//                String bikerScore = app.getBikerScore(false);

                int scoreUpdate = Integer.parseInt(bikerScore) - Integer.parseInt(points);

                setBikerScore(scoreUpdate);

                Log.d("mCliSocket", mCliSocket.toString());
                // set as text the json created
                mCliSocket.getOutputStream().write((json.toString()+"\n").getBytes());

                Log.d("sent points ", points);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            mTextInput.setText("");
            findViewById(R.id.idSendButton).setEnabled(true);
            findViewById(R.id.idSendPointsButton).setEnabled(true);
            findViewById(R.id.idDisconnectButton).setEnabled(true);
        }
    };

    private boolean checkAvailablePoints(int pointsToGive) {
            // assim ele vai perguntar ao server pelos pontos, se o server estiver offline, usa a "cache"
//        int points = Integer.parseInt(app.getBikerScore(false));
        int points = Integer.parseInt(bikerScore);

        return (points - pointsToGive) >= 0;
    }


    private View.OnClickListener listenerSendButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            findViewById(R.id.idSendButton).setEnabled(false);
            String jsonStr = "";
            try {
                Log.d("sent msg ", mTextInput.getText().toString());
                // get text user wrote in the text box
                String message = mTextInput.getText().toString()+"\n";

                JSONObject json = new JSONObject();
                // indicate that the user is sending a message (and it is not giving points)
                json.put(COMMUNICATION_TYPE_WIFI, SEND_MESSAGE_WIFI);
                json.put(MESSAGE_WIFI, message);
                Message m = new Message();
                m.setBody(message);
                m.setUserId(myName);
                allPeersArray.add(m);
                peersAdapter.notifyDataSetChanged();
                // set as text the json created
//                mTextInput.setText(json.toString());
                // get json from the text box (solution created because if
                // this line is executed mCliSocket.getOutputStream().write(json.toString());
                // the code would be stuck there forever)
                mCliSocket.getOutputStream().write( (json.toString()+"\n").getBytes());

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            mTextInput.setText("");
            findViewById(R.id.idSendButton).setEnabled(true);
            findViewById(R.id.idSendPointsButton).setEnabled(true);
            findViewById(R.id.idDisconnectButton).setEnabled(true);
        }
    };

    private View.OnClickListener listenerDisconnectButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            findViewById(R.id.idDisconnectButton).setEnabled(false);
            if (mCliSocket != null) {
                try {
                    mCliSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mCliSocket = null;

            guiUpdateDisconnectedState();
            try {
                sendPointsExchangeToServer();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            while ((!Thread.currentThread().isInterrupted())) {
                try {
                    SimWifiP2pSocket sock = mSrvSocket.accept();
                    if (mCliSocket != null && mCliSocket.isClosed()) {
                        mCliSocket = null;
                        Log.d("esta fechado" ,"fechado");
                    }
                    if (mCliSocket != null) {
                        Log.d(TAG, "Closing accepted socket because mCliSocket still active.");
                        sock.close();
                    } else {
                        Log.d("publicou o pro Inc","fd");
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

            // TODO: 27-Apr-16 ANOTHER USER HAS STARTED A CONVERSATION WITH YOU (ISABEL, vê isto)
            mComm = new ReceiveCommTask();

            mComm.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mCliSocket);
        }
    }

    public class OutgoingCommTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
//            mTextOutput.setText("Connecting...");
            if(app.getUnreadMessages().containsKey(connectedUser)) {
                for (Message m :
                        app.getUnreadMessages().get(connectedUser)) {
                    allPeersArray.add(m);
                }


            }
            peersAdapter.notifyDataSetChanged();

            Toast.makeText(UbiconnectActivity.this, "Connecting..",
                    Toast.LENGTH_SHORT).show();

        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Log.d("mCliSocket Out","10001");
                mCliSocket = new SimWifiP2pSocket(params[0],
                        Integer.parseInt(getString(R.string.port)));
            } catch (UnknownHostException e) {
                return "Unknown Host:" + e.getMessage();
            } catch (IOException e) {
                return "IO error:" + e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
//                findViewById(R.id.idConnectButton).setEnabled(true);
            }
            else {



                try {
                    Log.d("sent info ", myName);

                    JSONObject json = new JSONObject();
                    // indicate that the user is sending a message (and it is not giving points)
                    json.put(COMMUNICATION_TYPE_WIFI, SEND_INFO_WIFI);
                    json.put(NAME_WIFI, myName);



                    // set as text the json created
                    mCliSocket.getOutputStream().write((json.toString()+"\n").getBytes());

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mComm = new ReceiveCommTask();
                mComm.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,mCliSocket);
            }
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
        protected void onPreExecute() {
//            mTextOutput.setText("");


            findViewById(R.id.idSendButton).setEnabled(true);
            findViewById(R.id.idDisconnectButton).setEnabled(true);
//            findViewById(R.id.idConnectButton).setEnabled(false);

            // activate text input
            mTextInput.setEnabled(true);
            mTextInput.setHint("Type a message..");
            findViewById(R.id.idSendPointsButton).setEnabled(true);

//            // clear list
//            allPeersArray.clear();
//            peersAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onProgressUpdate(String... values) {

            if (isMessageExchange(values[0])) {
//                mTextOutput.append(mMessage+"\n");
                Message m = new Message();
                m.setBody(mMessage);
                m.setUserId(personName);
                allPeersArray.add(m);
                exchangedMessagesPerClient.get(personName).add(mMessage);
                peersAdapter.notifyDataSetChanged();
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
            if (mBound) {
                guiUpdateDisconnectedState();
                try {
                    sendPointsExchangeToServer();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                guiUpdateInitState();
                try {
                    sendPointsExchangeToServer();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
                if (pts.getMode() == PointsTransfer.SENT_TO_A_PEER) {
                    int points = pts.getPoints(); // get sent points
                    String pointsOriginMessageToMe = pts.getJson().getString(POINTS_ORIGIN_TO_ME); // get message to be displayed on our app
                    // invoke the async task to tell the server to decrease our points
                    decreasePoints(points, pointsOriginMessageToMe);

                } else if (pts.getMode() == PointsTransfer.EARNED_FROM_A_PEER) {
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
//        StringBuilder peersStr = new StringBuilder();

        // TODO: 23-Apr-16 Check if repeated/deleted peers problem on the peers list comes from here
        // clean peers arrays
        allPeersArray.clear();
        peersAdapter.clear();
        peersIPsArrayList.clear();
        boolean changed = false;
        // compile list of devices in range
        for (SimWifiP2pDevice device : peers.getDeviceList()) {
            String devstr = device.deviceName + " - " + device.getVirtIp();
            Message m = new Message();
            m.setBody(devstr);
            m.setUserId(device.deviceName);
            allPeersArray.add(m);
            peersNamesArrayList.add(device.deviceName);
            peersIPsArrayList.add(device.getVirtIp());
//            peersStr.append(devstr);
            String bikeNumber = device.deviceName.replace("bike","");
            // TODO: 10-May-16 para teste, procura sempre pela bicicleta 1
//            if (bikeNumber.equals(app.getBikeReservedID())){
            if (bikeNumber.equals("1")){
                app.setDetectingBike(true);
                changed = true;
            }
        }
        if (!changed) {
            app.setDetectingBike(false);
        }

        peersAdapter.notifyDataSetChanged();

        // display list of devices in range
//        new AlertDialog.Builder(this)
//                .setTitle("Devices in WiFi Range")
//                .setMessage(peersStr.toString())
//                .setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                    }
//                })
//                .show();
    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices,
                                     SimWifiP2pInfo groupInfo) {

        // compile list of network members
        StringBuilder peersStr = new StringBuilder();
        for (String deviceName : groupInfo.getDevicesInNetwork()) {
            SimWifiP2pDevice device = devices.getByName(deviceName);
            String devstr = "" + deviceName + " (" +
                    ((device == null)?"??":device.getVirtIp()) + ")\n";
            peersStr.append(devstr);
        }

        // display list of network members
        new AlertDialog.Builder(this)
                .setTitle("Devices in WiFi Network")
                .setMessage(peersStr.toString())
                .setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

	/*
	 * Helper methods for updating the interface
	 */

    private void guiSetButtonListeners() {

//        findViewById(R.id.idConnectButton).setOnClickListener(listenerConnectButton);
        findViewById(R.id.idDisconnectButton).setOnClickListener(listenerDisconnectButton);
        findViewById(R.id.idSendButton).setOnClickListener(listenerSendButton);
//        findViewById(R.id.idWifiOnButton).setOnClickListener(listenerWifiOnButton);
//        findViewById(R.id.idWifiOffButton).setOnClickListener(listenerWifiOffButton);
        findViewById(R.id.idListPeersButton).setOnClickListener(listenerListPeersButton);
        findViewById(R.id.idSendPointsButton).setOnClickListener(listenerSendPointsButton);
    }

    private void guiUpdateInitState() {

        mTextInput = (TextView) findViewById(R.id.editText1);
        mTextInput.setHint("");
        mTextInput.setEnabled(false);

        bikerName = app.getUsername();
//        pointsButton = (Button) findViewById(R.id.biker_score);

        View dashView = findViewById(R.id.footer_layout);
        pointsButton = (Button) dashView.findViewById(R.id.biker_score);

        bikersNameTextView  = (TextView) findViewById(R.id.biker_name);
        bikersNameTextView.setText(bikerName);

//        mTextOutput = (TextView) findViewById(R.id.editText2);
//        mTextOutput.setEnabled(false);
//        mTextOutput.setText("");

//        findViewById(R.id.idConnectButton).setEnabled(false);
        findViewById(R.id.idDisconnectButton).setEnabled(false);
        findViewById(R.id.idSendButton).setEnabled(false);
//        findViewById(R.id.idWifiOnButton).setEnabled(true);
//        findViewById(R.id.idWifiOffButton).setEnabled(false);
        findViewById(R.id.idListPeersButton).setEnabled(false);
        findViewById(R.id.idSendPointsButton).setEnabled(false);
    }

    private void guiUpdateDisconnectedState() {

//        mTextOutput.setEnabled(true);
//        mTextOutput.setText("");
        mTextInput.setHint("");
        mTextInput.setEnabled(false);

        findViewById(R.id.idSendButton).setEnabled(false);
//        findViewById(R.id.idConnectButton).setEnabled(true);
        findViewById(R.id.idDisconnectButton).setEnabled(false);
//        findViewById(R.id.idWifiOnButton).setEnabled(false);
//        findViewById(R.id.idWifiOffButton).setEnabled(true);
        findViewById(R.id.idListPeersButton).setEnabled(true);
        findViewById(R.id.idSendPointsButton).setEnabled(false);


        // clear list
        allPeersArray.clear();
        peersAdapter.notifyDataSetChanged();
        // remove connected user name
        personView.setText("");

        peersList.setClickable(false);

    }

    /**
     * if it's a message exchange it will return true
     * if it's a points exchange it will rerturn false
     * @param receivedMessage
     * @return	true - message
     * 			false - points
     */
//    @Override
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

                if (exchangedMessagesPerClient.containsKey(personName) ){
                    // get all the messages exhanged with client personName
                    for (String msg :
                            exchangedMessagesPerClient.get(personName)) {
                        Message m = new Message();
                        m.setBody(msg);
                        m.setUserId(personName);
                        allPeersArray.add(m);
                    }
                    peersAdapter.notifyDataSetChanged();

                    // if person is new, create an entry in exchangedMessagesPerClient
                } else {
                    exchangedMessagesPerClient.put(personName ,new ArrayList<String>());
                }
                personView.setText("Connected to - " + personName);
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

//    @Override
    protected void applyReceivedPoints(String points, String pointsOrigin, String pointsSender, JSONObject json) {
        // TODO: 27-Apr-16 move this line to after the connection with the peer is terminated
//        addPoints(Integer.parseInt(points), pointsOrigin, pointsSender);

            // create an PointsTransfer object that contains the transaction
        PointsTransfer pts = new PointsTransfer(PointsTransfer.EARNED_FROM_A_PEER, Integer.parseInt(points), connectedUser, json);
            // add the transaction to the pointsExchange log
        pointsExchange.add(pts);

        int scoreUpdate = Integer.parseInt(bikerScore) + Integer.parseInt(points);

        setBikerScore(scoreUpdate);

    }


    private boolean checkPoints(String points) {
        int pointsInt = 0;
        try {
            pointsInt = Integer.parseInt(points);
        } catch ( NumberFormatException e) {
            Toast.makeText(UbiconnectActivity.this, "Not a number!!",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
            // check if the user has enough points to give
        boolean hasPoints = checkAvailablePoints(pointsInt);

        if (!hasPoints) {
            Toast.makeText(UbiconnectActivity.this, "Not enough points available!!",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        Toast.makeText(UbiconnectActivity.this, "Seems to have enough points..",
                Toast.LENGTH_SHORT).show();

        return true;
    }

    private void decreasePoints(int points, String pointsOrigin) {

        DecreasePoints decreasePointsTask = new DecreasePoints(points, pointsOrigin);
        // task.execute().get() is used to wait for the task to be executed
        // so we can update the user score and score history
        try {
//            decreasePointsTask.execute().get();
            decreasePointsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private class DecreasePoints extends AsyncTask<Void, Void, Void> {
        private DataOutputStream dataOutputStream;
        private DataInputStream dataInputStream;
        private JSONObject json;
        private Socket socket;
        private int points;
        private String pointsOrigin;

        public DecreasePoints(int points, String pointsOrigin) {
            this.points = points;
            this.pointsOrigin = pointsOrigin;
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
                json.put(REQUEST_TYPE, REMOVE_POINTS);
                json.put(CLIENT_NAME, myName);
                json.put(POINTS_TO_DECREASE, String.valueOf(points));
                json.put(POINTS_ORIGIN, pointsOrigin);

                dataOutputStream = new DataOutputStream(
                        socket.getOutputStream());

                dataInputStream = new DataInputStream(
                        socket.getInputStream());

                // transfer JSONObject as String to the server
                dataOutputStream.writeUTF(json.toString());

                // Thread will wait till server replies
                String response = dataInputStream.readUTF();

                if (!response.equals(POINTS_REMOVED)) {
                    decreasePointsResult = false;
                } else {
                    decreasePointsResult = true;
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

            if(decreasePointsResult) {
                setBikerScore(Integer.parseInt(app.getBikerScore(false)));

                Toast.makeText(UbiconnectActivity.this, "Points decreased",
                        Toast.LENGTH_SHORT).show();
            } else
            {
                Toast.makeText(UbiconnectActivity.this, "Could NOT decrease the points",
                        Toast.LENGTH_SHORT).show();
            }

        }

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

    private class AddPoints extends AsyncTask<Void, Void, Void> {
        private DataOutputStream dataOutputStream;
        private DataInputStream dataInputStream;
        private JSONObject json;
        private Socket socket;
        private int points;
        private String pointsOrigin;
        private String senderOfPoints;

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
                json.put(CLIENT_NAME, myName);
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
                    mPoints.put(String.valueOf(points), pointsOrigin);

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
                setBikerScore(Integer.parseInt((app.getBikerScore(false))));

                Toast.makeText(UbiconnectActivity.this, "Points added",
                        Toast.LENGTH_SHORT).show();
            } else
            {
                Toast.makeText(UbiconnectActivity.this, "Could NOT add the points",
                        Toast.LENGTH_SHORT).show();
            }

        }

    }

}