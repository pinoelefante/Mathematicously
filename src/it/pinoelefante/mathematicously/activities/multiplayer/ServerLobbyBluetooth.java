package it.pinoelefante.mathematicously.activities.multiplayer;

import it.pinoelefante.mathematicously.R;
import it.pinoelefante.mathematicously.server.ServerBluetooth;
import it.pinoelefante.mycustomviews.PActivity;
import it.pinoelefante.mycustomviews.PButton;

import java.io.IOException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.EditText;
import android.widget.Toast;

public class ServerLobbyBluetooth extends PActivity {
	private static final int REQUEST_ENABLE_BT = 1;
	private BluetoothAdapter bt;
	private PButton backButton;
	private EditText nicknameET;
	private String nickname;
	private SharedPreferences prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bt = BluetoothAdapter.getDefaultAdapter();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		setContentView(R.layout.activity_server_lobby_bt);
		if(bt == null){
			Toast.makeText(getApplicationContext(), "Bluetooth non supportato", Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(getApplicationContext(), "Avvio Server Bluetooth", Toast.LENGTH_SHORT).show();
			disegna();
			avviaServer();
		}
	}
	private void disegna(){
		int dimButt = 0;
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
			dimButt = calcolaDimensioniW(10);
		else
			dimButt = calcolaDimensioniH(10);
		
		nicknameET = (EditText) findViewById(R.id.NicknameBT);
		nickname = prefs.getString("nickname", "Server");
		nicknameET.setText(nickname);
		
		backButton = (PButton) findViewById(R.id.back_BTS);
		backButton.setSize(dimButt, dimButt);
	}
	private void avviaServer() {
		if(!bt.isEnabled())
			abilitaBluetooth();
		else {
			registraBroadcast();
			setDiscoverable();
			avviaServerSocket();
		}
	}
	private BroadcastReceiver bt_state, bt_discoverable;
	private void registraBroadcast() {
		bt_state = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				switch(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)){
					case -1:
						break;
					case BluetoothAdapter.STATE_ON:
						setDiscoverable();
						break;
					case BluetoothAdapter.STATE_OFF:
						stopServerSocket();
						break;
				}
			}
		};
		bt_discoverable = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				switch(intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1)){
					case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
						avviaServerSocket();
						Toast.makeText(getApplicationContext(), "discoverable", Toast.LENGTH_SHORT).show();
						break;
					case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
						Toast.makeText(getApplicationContext(), "connectable", Toast.LENGTH_SHORT).show();
						break;
					case BluetoothAdapter.SCAN_MODE_NONE:
						Toast.makeText(getApplicationContext(), "none", Toast.LENGTH_SHORT).show();
						break;
				}
			}
		};
		registerReceiver(bt_state, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
		registerReceiver(bt_discoverable, new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
	}
	private void stopServerSocket(){
		ServerBluetooth.getInstance().stopServer();
		server.cancel(true);
	}
	AsyncTask<Void,Void,Void> server;
	private void avviaServerSocket() {
		server = new AsyncTask<Void,Void,Void>() {
			protected Void doInBackground(Void... params) {
				try {
					BluetoothServerSocket ssocket = bt.listenUsingRfcommWithServiceRecord("Mathematicously", ServerBluetooth.ITU_GENERATED_UUID);
					ServerBluetooth.getInstance().setServerSocket(ssocket);
					ServerBluetooth.getInstance().avviaServer();
					Toast.makeText(getApplicationContext(), "Server bt Avviato", Toast.LENGTH_LONG).show();
				} 
				catch (IOException e) {
					Toast.makeText(getApplicationContext(), "Server bt errore avvio", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
				return null;
			}
		};
		server.execute();
	}

	private void setDiscoverable() {
		Intent discoverableIntent = new
		Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		startActivity(discoverableIntent);
	}

	private void abilitaBluetooth() {
		Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == REQUEST_ENABLE_BT){
			if(resultCode == RESULT_OK){
				registraBroadcast();
			}
		}
		else {
			Toast.makeText(getApplicationContext(), "operazione cancellata - attiva bluetooth", Toast.LENGTH_SHORT).show();
		}
	}
	@Override
	protected void onDestroy() {
		stopServerSocket();
		unregisterReceiver(bt_discoverable);
		unregisterReceiver(bt_state);
		super.onDestroy();
	}

	/*
	private Server server;
	private BluetoothAdapter bluetoothAdp;
	private SharedPreferences prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bluetoothAdp = BluetoothAdapter.getDefaultAdapter();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		setContentView(R.layout.activity_server_lobby_bt);
		disegna();
	}
	private PButton backButton;
	private EditText nicknameET;
	private TextView attesa_tv;
	private String nickname;
	private void disegna(){
		backButton = (PButton) findViewById(R.id.SL_back);
		
		int dimButt = 0;
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
			dimButt = calcolaDimensioniW(10);
		else
			dimButt = calcolaDimensioniH(10);
		
		nicknameET = (EditText) findViewById(R.id.NicknameBT);
		nickname = prefs.getString("nickname", "Server");
		nicknameET.setText(nickname);
		
		backButton = (PButton) findViewById(R.id.SL_back);
		backButton.setSize(dimButt, dimButt);
		
		attesa_tv = (TextView) findViewById(R.id.statoConnessioneBT);
	}
	
	public void closeServer() {
		if(server!=null)
			server.stopServer();
		if(connection!=null)
			connection.interrupt();
		ServerCommunication comm = ServerCommunication.getInstance();
		if(comm!=null){
			comm.disconnect(true);
		}
	}
	public void startServer(View v){
		if(nicknameET.getText().toString().trim().length()==0){
			Toast.makeText(getApplicationContext(), R.string.nickname_vuoto, Toast.LENGTH_LONG).show();
			return;
		}
		nickname = nicknameET.getText().toString().trim();
		Editor prefs_edit = prefs.edit();
		prefs_edit.putString("nickname", nickname);
		prefs_edit.commit();
		
		v.setVisibility(View.INVISIBLE);
		attesa_tv.setVisibility(View.VISIBLE);
		nicknameET.setEnabled(false);
		
		startServer();
	}
	private Thread connection;
	public void startServer() {
		boolean enable = true;
		if(!bluetoothAdp.isEnabled()){
			enable = bluetoothAdp.enable();
		}
		if(enable){
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			enableBtIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		    startActivity(enableBtIntent);
		}
		else {
			Toast.makeText(getApplicationContext(), "bluetooth non abilitato", Toast.LENGTH_SHORT).show();;
			return;
		}
		
		class ThreadConnection extends Thread {
			public void run(){
				server = ServerBluetooth.getInstance();
				while(!bluetoothAdp.isEnabled()){
					try {
						sleep(100);
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				try {
					BluetoothServerSocket ssocket = bluetoothAdp.listenUsingRfcommWithServiceRecord("Mathematicously", ServerBluetooth.ITU_GENERATED_UUID);
					ServerBluetooth.getInstance().setServerSocket(ssocket);
					server.addListeners(getConnectionListener());
					server.avviaServer();
				}
				catch (IOException e) {
					e.printStackTrace();
					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(getApplicationContext(), "Errore bluetooth server listen", Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		}
		if(enable){
    		connection = new ThreadConnection();
    		connection.start();
		}
	}
	public void back(View v){
		onBackPressed();
	}
	@Override
	public void onBackPressed() {
		closeServer();
		super.onBackPressed();
	}
	public Map<String,Listener> getConnectionListener(){
		Map<String,Listener> lists = new HashMap<String,Listener>();
		Listener clientDisconnected = new Listener() {
			public void execute(String... p) {
				runOnUiThread(new Runnable() {
					public void run() {
						ServerBluetooth.getInstance().stopServer();
						Toast.makeText(getApplicationContext(), R.string.connection_lost, Toast.LENGTH_SHORT).show();
						Intent i = new Intent(getApplicationContext(), FirstPageActivity.class);
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(i);
					}
				});
				
			}
		};
		lists.put("clientDisconnected", clientDisconnected);
		
		Listener clientConnected = new Listener() {
			public void execute(String... p) {
				runOnUiThread(new Runnable() {
					public void run() {
						bluetoothAdp.cancelDiscovery();
						ServerCommunication.getInstance().write("wait");
						Intent i = new Intent(getApplicationContext(), ScegliGiocoMultiplayerActivity.class);
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(i);
					}
				});
			}
		};
		lists.put("clientConnected", clientConnected);
		
		Listener commandArrived = new Listener() {
			public void execute(String... p) {
				runOnUiThread(new Runnable() {
					public void run() {
						
					}
				});
			}
		};
		lists.put("commandArrived", commandArrived);
		
		return lists;
	}
	*/
}
