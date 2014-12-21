package it.pinoelefante.mathematicously.activities.multiplayer;

import it.pinoelefante.mathematicously.R;
import it.pinoelefante.mathematicously.activities.FirstPageActivity;
import it.pinoelefante.mathematicously.server.Listener;
import it.pinoelefante.mathematicously.server.Server;
import it.pinoelefante.mathematicously.server.ServerBluetooth;
import it.pinoelefante.mathematicously.server.ServerCommunication;
import it.pinoelefante.mycustomviews.PActivity;
import it.pinoelefante.mycustomviews.PButton;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ServerLobbyBluetooth extends PActivity {
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
		backButton = (PButton) findViewById(R.id.CL_back);
		
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
}
