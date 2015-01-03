package it.pinoelefante.mathematicously.activities.multiplayer;

import it.pinoelefante.mathematicously.R;
import it.pinoelefante.mathematicously.activities.FirstPageActivity;
import it.pinoelefante.mathematicously.server.Listener;
import it.pinoelefante.mathematicously.server.ServerBluetooth;
import it.pinoelefante.mathematicously.server.ServerCommunication;
import it.pinoelefante.mycustomviews.PActivity;
import it.pinoelefante.mycustomviews.PButton;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.EditText;
import android.widget.Toast;

public class ServerLobbyBluetooth extends PActivity {
	private BluetoothAdapter  bt;
	private PButton		   backButton;
	private EditText		  nicknameET;
	private String			nickname;
	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bt = BluetoothAdapter.getDefaultAdapter();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		setContentView(R.layout.activity_server_lobby_bt);
		if (bt == null) {
			Toast.makeText(getApplicationContext(), "Bluetooth non supportato", Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(getApplicationContext(), "Avvio Server Bluetooth", Toast.LENGTH_SHORT).show();
			disegna();
			avviaServer();
		}
	}

	private void disegna() {
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
		registraBroadcast();
		if (!bt.isEnabled()){
			setDiscoverable();
		}
		else {
			registraBroadcast();
			if(bt.getScanMode()!=BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
				setDiscoverable();
			else
				avviaServerSocket();
		}
	}

	private BroadcastReceiver bt_state, bt_discoverable;

	private void registraBroadcast() {
		bt_state = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
					case -1:
						break;
					case BluetoothAdapter.STATE_ON:
						if (bt.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
							setDiscoverable();
						break;
					case BluetoothAdapter.STATE_OFF:
						stopServerSocket();
						break;
					case BluetoothAdapter.STATE_CONNECTED:
						Toast.makeText(getApplicationContext(), "BT connection", Toast.LENGTH_SHORT).show();
						break;
					case BluetoothAdapter.STATE_DISCONNECTED:
						Toast.makeText(getApplicationContext(), "BT disconnection", Toast.LENGTH_SHORT).show();
						break;
				}
			}
		};
		bt_discoverable = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				switch (intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1)) {
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
		registerReceiver(bt_discoverable, new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
		registerReceiver(bt_state, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
	}

	private void stopServerSocket() {
		ServerBluetooth.getInstance().stopServer();
	}
	private void avviaServerSocket() {
		try {
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(getApplicationContext(), "listen", Toast.LENGTH_LONG).show();
				}
			});
			BluetoothServerSocket ssocket = bt.listenUsingRfcommWithServiceRecord("Mathematicously", ServerBluetooth.ITU_GENERATED_UUID);
			ServerBluetooth s_bt = ServerBluetooth.getInstance();
			s_bt.addListeners(getConnectionListener());
			s_bt.setServerSocket(ssocket);
			s_bt.avviaServer();
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(getApplicationContext(), "Server bt Avviato", Toast.LENGTH_LONG).show();
				}
			});
			
		}
		catch (IOException e) {
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(getApplicationContext(), "Server bt errore avvio", Toast.LENGTH_LONG).show();
				}
			});
			
			e.printStackTrace();
		}
		/*
		class ThreadStartServer extends Thread {
			public void run() {
				
			}
		}
		new ThreadStartServer().start();
		*/
	}

	private void setDiscoverable() {
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		startActivity(discoverableIntent);
	}
/*
	private void abilitaBluetooth() {
		Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
	}
*/
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			stopServerSocket();
			onBackPressed();
		}
	}

	@Override
	protected void onDestroy() {
		stopServerSocket();
		unregisterReceiver(bt_discoverable);
		unregisterReceiver(bt_state);
		super.onDestroy();
	}
	public Map<String,Listener> getConnectionListener(){
		Map<String,Listener> lists = new HashMap<String,Listener>();
		Listener clientDisconnected = new Listener() {
			public void execute(String... p) {
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(getApplicationContext(), "Disconnessione client", Toast.LENGTH_SHORT).show();
						ServerBluetooth.getInstance().stopServer();
						stopServerSocket();
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
						Toast.makeText(getApplicationContext(), "Client connesso", Toast.LENGTH_SHORT).show();
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
						Toast.makeText(getApplicationContext(), "Client - comando arrivato", Toast.LENGTH_SHORT).show();
					}
				});
			}
		};
		lists.put("commandArrived", commandArrived);
		
		return lists;
	}
}
