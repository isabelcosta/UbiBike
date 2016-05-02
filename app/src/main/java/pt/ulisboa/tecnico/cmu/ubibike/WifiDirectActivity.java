package pt.ulisboa.tecnico.cmu.ubibike;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
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
    private Button ubiconnectButtonView;


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
        setContentView(R.layout.activity_wifi_direct);

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

        ubiconnectButtonView = (Button) findViewById(R.id.menu_bottom_ubiconnect);


        runTimeTask();

    }

    protected void runTimeTask() {
        handler.postAtTime(timeTask, SystemClock.uptimeMillis() + 2000);
    }

    private Runnable timeTask = new Runnable() {
        public void run() {

            Intent intent = new Intent(getApplicationContext(), SimWifiP2pService.class);
            if(!app.ismBound()) {
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                mBound = true;
                app.setmBound(mBound);
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


        app.setmManager(mManager);
        app.setmChannel(mChannel);
        app.setmService(mService);
        app.setmBound(mBound);
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

            try {
                mSrvSocket = new SimWifiP2pSocketServer(
                        Integer.parseInt(getString(R.string.port)));
                app.setmSrvSocket(mSrvSocket);
            } catch (IOException e) {
                e.printStackTrace();
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

            // TODO: 27-Apr-16 ANOTHER USER HAS STARTED A CONVERSATION WITH YOU (ISABEL, vê isto)
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
                if (unreadMessages.containsKey(personName)){
                        // adicionar "m" às mensagens por ler
                    if (unreadMessages.get(personName) != null) {
                        unreadMessages.get(personName).add(m);
                    } else {
                        ArrayList<Message> msgList = new ArrayList<>();
                        msgList.add(m);
                        unreadMessages.put(personName,msgList);
                    }
                } else {
                    ArrayList<Message> msgList = new ArrayList<>();
                    msgList.add(m);
                    unreadMessages.put(personName,msgList);
                }

                Log.d("URM size", unreadMessages.size()+"");
                Toast.makeText(WifiDirectActivity.this, "New Message from " + personName,
                        Toast.LENGTH_SHORT).show();

                String ubiconnectText = "UBICONNECT (" + unreadMessages.size() + ")";
                ubiconnectButtonView = (Button) findViewById(R.id.menu_bottom_ubiconnect);

                ubiconnectButtonView.setText(ubiconnectText);
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
        }
    }

	/*
	 * Listeners associated to Termite
	 */

    @Override
    public void onPeersAvailable(SimWifiP2pDeviceList peers) {

        // TODO: 23-Apr-16 Check if repeated/deleted peers problem on the peers list comes from here
        // clean peers arrays
//        allPeersArray.clear();
//        peersAdapter.clear();
//        peersIPsArrayList.clear();

        // compile list of devices in range
//        for (SimWifiP2pDevice device : peers.getDeviceList()) {
//            String devstr = device.deviceName + " - " + device.getVirtIp();
//            Message m = new Message();
//            m.setBody(devstr);
//            m.setUserId(device.deviceName);
//            allPeersArray.add(m);
//            peersNamesArrayList.add(device.deviceName);
//            peersIPsArrayList.add(device.getVirtIp());
//        }

//        peersAdapter.notifyDataSetChanged();

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
                json.put(POINTS_TO_ADD, points);
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

}