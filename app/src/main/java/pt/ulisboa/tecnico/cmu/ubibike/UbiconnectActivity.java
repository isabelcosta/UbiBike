package pt.ulisboa.tecnico.cmu.ubibike;

import android.app.Activity;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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


import static com.ubibike.Constants.*;



public class UbiconnectActivity extends CommonWithButtons implements
        SimWifiP2pManager.PeerListListener, SimWifiP2pManager.GroupInfoListener {

    public static final String TAG = "msgsender";

    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private Messenger mService = null;
    private boolean mBound = false;
    private SimWifiP2pSocketServer mSrvSocket = null;
    private SimWifiP2pSocket mCliSocket = null;
    private ReceiveCommTask mComm = null;
    private TextView mTextInput;
    //    private TextView mTextOutput;
    private SimWifiP2pBroadcastReceiverList mReceiver;

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
    // TODO: 24-Apr-16 get personName from Wifi-Direct
    private String personName;
    // TODO: 24-Apr-16 get myName from Wifi-Direct
    private String myName;
    private Handler handler = new Handler();


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
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
        mReceiver = new SimWifiP2pBroadcastReceiverList(this);
        registerReceiver(mReceiver, filter);

        personView = (TextView)findViewById(R.id.person_name);

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


                // person name
                String displayMessage = personName + " - " + personIp;

                personView.setText(displayMessage);
                // if person exists on chat history, get exchanged messages
                if (exchangedMessagesPerClient.containsKey(personName) ){
                    // get all the messages exhanged with client personName
                    for (String msg :
                            exchangedMessagesPerClient.get(personName)) {
                        Message m = new Message();
                        m.setBody(msg);
                        m.setUserId(personName);
                        allPeersArray.add(m);
                    }
                    // if person is new, create an entry in exchangedMessagesPerClient
                } else {
                    exchangedMessagesPerClient.put(personName ,new ArrayList<String>());
                }
                peersAdapter.notifyDataSetChanged();

            }
        });


        handler.postAtTime(timeTask, SystemClock.uptimeMillis() + 2000);


    }

    private Runnable timeTask = new Runnable() {
        public void run() {

            Intent intent = new Intent(UbiconnectActivity.this, SimWifiP2pService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            mBound = true;

            // spawn the chat server background task
            new IncommingCommTask().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR);

            guiUpdateDisconnectedState();


        }
    };

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }





	/*
	 * Listeners associated to buttons
	 */

    private View.OnClickListener listenerWifiOnButton = new View.OnClickListener() {
        public void onClick(View v){

            Intent intent = new Intent(v.getContext(), SimWifiP2pService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            mBound = true;

            // spawn the chat server background task
            new IncommingCommTask().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR);

            guiUpdateDisconnectedState();
        }
    };

    private View.OnClickListener listenerWifiOffButton = new View.OnClickListener() {
        public void onClick(View v){
            if (mBound) {
                unbindService(mConnection);
                mBound = false;
                guiUpdateInitState();
            }
        }
    };

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
                Log.d("sent points ", points);
                // get text user wrote in the text box
                if (!checkPoints(points)) {
                    return;
                }

                JSONObject json = new JSONObject();
                // indicate that the user is sending a message (and it is not giving points)
                json.put(COMMUNICATION_TYPE_WIFI, GIVE_POINTS_WIFI);
                json.put(POINTS_WIFI, points);
                json.put(USER_WIFI, "Ze To");

                // set as text the json created
                mCliSocket.getOutputStream().write((json.toString()+"\n").getBytes());

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

            try {
                mSrvSocket = new SimWifiP2pSocketServer(
                        Integer.parseInt(getString(R.string.port)));
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
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
            mComm = new ReceiveCommTask();

            mComm.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mCliSocket);
        }
    }

    public class OutgoingCommTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
//            mTextOutput.setText("Connecting...");

        }

        @Override
        protected String doInBackground(String... params) {
            try {
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
            mTextInput.setHint("");
            mTextInput.setText("");

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
            } else {
                guiUpdateInitState();
            }
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
        findViewById(R.id.idWifiOnButton).setOnClickListener(listenerWifiOnButton);
        findViewById(R.id.idWifiOffButton).setOnClickListener(listenerWifiOffButton);
        findViewById(R.id.idListPeersButton).setOnClickListener(listenerListPeersButton);
        findViewById(R.id.idSendPointsButton).setOnClickListener(listenerSendPointsButton);
    }

    private void guiUpdateInitState() {

        mTextInput = (TextView) findViewById(R.id.editText1);
        mTextInput.setHint("Type a message..");
        mTextInput.setEnabled(false);

//        mTextOutput = (TextView) findViewById(R.id.editText2);
//        mTextOutput.setEnabled(false);
//        mTextOutput.setText("");

//        findViewById(R.id.idConnectButton).setEnabled(false);
        findViewById(R.id.idDisconnectButton).setEnabled(false);
        findViewById(R.id.idSendButton).setEnabled(false);
        findViewById(R.id.idWifiOnButton).setEnabled(true);
        findViewById(R.id.idWifiOffButton).setEnabled(false);
        findViewById(R.id.idListPeersButton).setEnabled(false);
        findViewById(R.id.idSendPointsButton).setEnabled(false);
    }

    private void guiUpdateDisconnectedState() {

        mTextInput.setEnabled(true);
        mTextInput.setHint("Type a message..");
//        mTextOutput.setEnabled(true);
//        mTextOutput.setText("");

        findViewById(R.id.idSendButton).setEnabled(false);
//        findViewById(R.id.idConnectButton).setEnabled(true);
        findViewById(R.id.idDisconnectButton).setEnabled(false);
        findViewById(R.id.idWifiOnButton).setEnabled(false);
        findViewById(R.id.idWifiOffButton).setEnabled(true);
        findViewById(R.id.idListPeersButton).setEnabled(true);
        findViewById(R.id.idSendPointsButton).setEnabled(false);


        // clear list
        allPeersArray.clear();
        peersAdapter.notifyDataSetChanged();
        // remove connected user name
        personView.setText("");
    }

    /**
     * if it's a message exchange it will return true
     * if it's a points exchange it will rerturn false
     * @param receivedMessage
     * @return	true - message
     * 			false - points
     */
    private boolean isMessageExchange(String receivedMessage) {
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
                // get the user that send the points
                String origin = jsondata.getString(USER_WIFI);
                // put the pair <points,origin> on the mPoints that keeps the history of the score
                mPoints.put(points,origin);
                Log.d("received pts ", points);

                // // TODO: 24-Apr-16 change applyReceivedPoints
                applyReceivedPoints(points);
                return false;
            } else if (type.equals(SEND_INFO_WIFI)) {
                personName = jsondata.getString(NAME_WIFI);

                // TODO: 24-Apr-16 show messages only when user A chooses to see messages from B
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
                personView.setText(personName);
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }


    private void applyReceivedPoints(String points) {
        personView.setText(points);
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
        // TODO: 24-Apr-16 check user's points
        boolean hasPoints = true;

        if (pointsInt > 100) {
            Toast.makeText(UbiconnectActivity.this, "Not enough points available!!",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

}