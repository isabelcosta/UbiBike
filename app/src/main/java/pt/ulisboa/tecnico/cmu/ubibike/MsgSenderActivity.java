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
import android.os.IBinder;
import android.os.Messenger;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.Channel;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.GroupInfoListener;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.PeerListListener;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmu.ubibike.common.CommonWithButtons;

public class MsgSenderActivity extends CommonWithButtons implements
		PeerListListener, GroupInfoListener {

    public static final String TAG = "msgsender";

    private SimWifiP2pManager mManager = null;
    private Channel mChannel = null;
    private Messenger mService = null;
	private boolean mBound = false;
	private SimWifiP2pSocketServer mSrvSocket = null;
	private SimWifiP2pSocket mCliSocket = null;
	private ReceiveCommTask mComm = null;
	private TextView mTextInput;
	private TextView mTextOutput;
    private SimWifiP2pBroadcastReceiver mReceiver;
	private String person;


	public SimWifiP2pManager getManager() {
		return mManager;
	}

	//public Channel getChannel() {
	//	return mChannel;
	//}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.bikerName = ((UbiBikeApplication) getApplication()).getUsername();




		// initialize the UI
		setContentView(R.layout.main);
		guiSetButtonListeners();
		guiUpdateInitState();

		// HEADER
			// biker name
		TextView bikersName = (TextView)findViewById(R.id.biker_name);
		bikersName.setText(bikerName);


		// person name
		person = getIntent().getStringExtra("person");
		TextView chatPerson = (TextView)findViewById(R.id.chat_person);
		chatPerson.setText(person);

		// initialize the WDSim API
		SimWifiP2pSocketManager.Init(getApplicationContext());
		
		// register broadcast receiver
		IntentFilter filter = new IntentFilter();
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
		mReceiver = new SimWifiP2pBroadcastReceiver(this);
		registerReceiver(mReceiver, filter);
	}

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

	/*
	 * Listeners associated to buttons
	 */

	private OnClickListener listenerWifiOnButton = new OnClickListener() {
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

	private OnClickListener listenerWifiOffButton = new OnClickListener() {
        public void onClick(View v){
            if (mBound) {
                unbindService(mConnection);
                mBound = false;
                guiUpdateInitState();
            }
        }
	};

	private OnClickListener listenerInRangeButton = new OnClickListener() {
        public void onClick(View v){
        	if (mBound) {
                mManager.requestPeers(mChannel, MsgSenderActivity.this);
        	} else {
            	Toast.makeText(v.getContext(), "Service not bound",
            		Toast.LENGTH_SHORT).show();
            }
        }
	};

	private OnClickListener listenerInGroupButton = new OnClickListener() {
        public void onClick(View v){
        	if (mBound) {
                mManager.requestGroupInfo(mChannel, MsgSenderActivity.this);
        	} else {
            	Toast.makeText(v.getContext(), "Service not bound",
            		Toast.LENGTH_SHORT).show();
            }
              }
	};

	private OnClickListener listenerConnectButton = new OnClickListener() {
		@Override
		public void onClick(View v) {
			findViewById(R.id.idConnectButton).setEnabled(false);
			new OutgoingCommTask().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR,
                    mTextInput.getText().toString());
		}
	};

    private OnClickListener listenerSendButton = new OnClickListener() {
        @Override
        public void onClick(View v) {
			findViewById(R.id.idSendButton).setEnabled(false);
			try {
				String message = mTextInput.getText().toString()+"\n";
//				JSONObject json = new JSONObject();
//				// indicate that the user is sending a message (and it is not giving points)
//				json.put(COMMUNICATION_TYPE_WIFI, SEND_MESSAGE_WIFI);
//				json.put(MESSAGE_WIFI, message);


				mCliSocket.getOutputStream().write( (message).getBytes());
			} catch (IOException e) {
				e.printStackTrace();
//			} catch (JSONException e) {
//				e.printStackTrace();
			}
			mTextInput.setText("");
			findViewById(R.id.idSendButton).setEnabled(true);
			findViewById(R.id.idDisconnectButton).setEnabled(true);
        }
    };

    private OnClickListener listenerDisconnectButton = new OnClickListener() {
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
			mTextOutput.setText("Connecting...");
            SystemClock.sleep(2000);
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

				mTextOutput.setText("123"+result);
				findViewById(R.id.idConnectButton).setEnabled(true);
			}
			else {
				mComm = new ReceiveCommTask();
				mComm.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,mCliSocket);
			}
		}
	}


	public class ReceiveCommTask extends AsyncTask<SimWifiP2pSocket, String, Void> {
		SimWifiP2pSocket s;
        String receivedMessage = null;

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
                Log.i("st 23 ", st);
            } catch (IOException e) {
				Log.d("Error reading socket:", e.getMessage());
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			mTextOutput.setText("");
			findViewById(R.id.idSendButton).setEnabled(true);
			findViewById(R.id.idDisconnectButton).setEnabled(true);
			findViewById(R.id.idConnectButton).setEnabled(false);
			mTextInput.setHint("");
			mTextInput.setText("");

		}

		@Override
		protected void onProgressUpdate(String... values) {
            receivedMessage += values;

		}

		@Override
		protected void onPostExecute(Void result) {

            // create the json object from the String
            JSONObject jsondata = null;
            String message = null;
            try {
                jsondata = new JSONObject(receivedMessage);

                // get the type of message to know what the other user wants
                String type = jsondata.getString(REQUEST_TYPE);
                // if the type is a message, display the message on the screen
                if (type.equals(SEND_MESSAGE_WIFI)) {
                    message = jsondata.getString(MESSAGE_WIFI);
                    // if the type is a give points, connect to the server and update my points
                } else if (type.equals(GIVE_POINTS_WIFI)) {
                    // TODO: 22-Apr-16 implement this
                    // get the points received
                    jsondata.getString(POINTS_WIFI);
                    // get the user that send the points
                    jsondata.getString(USER_WIFI);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mTextOutput.setText("78456");
            mTextOutput.append(message+"QQQDW"+"\n");

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
		StringBuilder peersStr = new StringBuilder();
		
		// compile list of devices in range
		for (SimWifiP2pDevice device : peers.getDeviceList()) {
			String devstr = "" + device.deviceName + " (" + device.getVirtIp() + ":" + device.getRealIp() + ")\n";
			peersStr.append(devstr);
		}

		// display list of devices in range
		new AlertDialog.Builder(this)
	    .setTitle("Devices in WiFi Range")
	    .setMessage(peersStr.toString())
	    .setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	        }
	     })
	     .show();
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
		
		findViewById(R.id.idConnectButton).setOnClickListener(listenerConnectButton);
		findViewById(R.id.idDisconnectButton).setOnClickListener(listenerDisconnectButton);
		findViewById(R.id.idSendButton).setOnClickListener(listenerSendButton);
		findViewById(R.id.idWifiOnButton).setOnClickListener(listenerWifiOnButton);
		findViewById(R.id.idWifiOffButton).setOnClickListener(listenerWifiOffButton);
		findViewById(R.id.idInRangeButton).setOnClickListener(listenerInRangeButton);
		findViewById(R.id.idInGroupButton).setOnClickListener(listenerInGroupButton);
	}

	private void guiUpdateInitState() {
		
		mTextInput = (TextView) findViewById(R.id.editText1);
		mTextInput.setHint("type remote virtual IP (192.168.0.0/16)");
		mTextInput.setEnabled(false);
		
		mTextOutput = (TextView) findViewById(R.id.editText2);
        mTextOutput.setBackgroundColor(1);
		mTextOutput.setEnabled(false);
		mTextOutput.setText("1");

		findViewById(R.id.idConnectButton).setEnabled(false);
		findViewById(R.id.idDisconnectButton).setEnabled(false);
		findViewById(R.id.idSendButton).setEnabled(false);
		findViewById(R.id.idWifiOnButton).setEnabled(true);
		findViewById(R.id.idWifiOffButton).setEnabled(false);
		findViewById(R.id.idInRangeButton).setEnabled(false);
		findViewById(R.id.idInGroupButton).setEnabled(false);
	}

	private void guiUpdateDisconnectedState() {
		
		mTextInput.setEnabled(true);
		mTextInput.setHint("type remote virtual IP (192.168.0.0/16)");
		mTextOutput.setEnabled(true);
		mTextOutput.setText("");
		
		findViewById(R.id.idSendButton).setEnabled(false);
		findViewById(R.id.idConnectButton).setEnabled(true);
		findViewById(R.id.idDisconnectButton).setEnabled(false);
		findViewById(R.id.idWifiOnButton).setEnabled(false);
		findViewById(R.id.idWifiOffButton).setEnabled(true);
		findViewById(R.id.idInRangeButton).setEnabled(true);            
		findViewById(R.id.idInGroupButton).setEnabled(true);
	}
}
