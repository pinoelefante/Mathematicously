package it.pinoelefante.mathematicously.activities.multiplayer;

import it.pinoelefante.mathematicously.R;
import it.pinoelefante.mathematicously.activities.FirstPageActivity;
import it.pinoelefante.mathematicously.activities.multiplayer.games.EnduranceActivityMulti;
import it.pinoelefante.mathematicously.activities.multiplayer.games.MemoryActivityMulti;
import it.pinoelefante.mathematicously.activities.multiplayer.games.QuizShowActivityMulti;
import it.pinoelefante.mathematicously.activities.multiplayer.games.RiflessiActivityMulti;
import it.pinoelefante.mathematicously.activities.multiplayer.games.ScriviRispostaActivityMulti;
import it.pinoelefante.mathematicously.activities.multiplayer.games.SfidaTempoActivityMulti;
import it.pinoelefante.mathematicously.activities.multiplayer.games.VeroFalsoActivityMulti;
import it.pinoelefante.mathematicously.constants.Giochi;
import it.pinoelefante.mathematicously.server.Listener;
import it.pinoelefante.mathematicously.server.PartialListenerContainer;
import it.pinoelefante.mathematicously.server.ServerBluetooth;
import it.pinoelefante.mathematicously.server.ServerCommunication;
import it.pinoelefante.mathematicously.server.ServerWiFi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ClientLobbyActivity extends Activity {
	private final static int BT_MODE = 1, WIFI_MODE = 2;
	private int rete_host;
	private ServerCommunication comm;
	private AlertDialog wait_dialog;
	private BluetoothAdapter bluetoothAdp;
	private SharedPreferences prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		rete_host = getIntent().getIntExtra("rete", 0);
		setContentView(R.layout.activity_client_lobby_wifi);
		if(rete_host == BT_MODE){
			bt_devices = new ArrayList<BluetoothDevice>();
			bt_found = new ArrayAdapter<String>(getApplicationContext(), R.layout.bt_row, R.id.textViewList);
			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
			registerReceiver(btFoundDevice, filter);
		}
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		avviaClient(rete_host);
	}
	private EditText ipAddress, nickNameET;
	private Button btnConnetti;
	private void disegnaWiFi(){
		String last_ip = prefs.getString("last_server_ip", "");
		ipAddress.setText(last_ip);
		nickNameET = (EditText) findViewById(R.id.CL_Nickname);
		String last_nick = prefs.getString("nickname", "Player "+(System.currentTimeMillis()/1000));
		nickNameET.setText(last_nick);
		btnConnetti = (Button) findViewById(R.id.wifi_connect_server);
	}
	private ListView list_devices;
	private void disegnaBluetooth(){
		nickNameET = (EditText) findViewById(R.id.CL_Nickname);
		String last_nick = prefs.getString("nickname", "Player "+(System.currentTimeMillis()/1000));
		nickNameET.setText(last_nick);
		list_devices = (ListView) findViewById(R.id.bt_list_devices);
		list_devices.setAdapter(bt_found);
		list_devices.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				nickname = nickNameET.getText().toString().trim();
				if(nickname.length()==0){
					Toast.makeText(getApplicationContext(), R.string.nickname_vuoto, Toast.LENGTH_LONG).show();
					return;
				}
				Editor prefs_edit = prefs.edit();
				prefs_edit.putString("nickname", nickname);
				prefs_edit.commit();
				if(position>0){
					class BTConnection extends Thread {
						public void run(){
							BluetoothDevice dev = bt_devices.get(position);
							BluetoothSocket socket = null;
							try {
								socket = dev.createRfcommSocketToServiceRecord(ServerBluetooth.ITU_GENERATED_UUID);
								bluetoothAdp.cancelDiscovery();
								socket.connect();
								PartialListenerContainer listn = new PartialListenerContainer();
								listn.addListener(getConnectionListener());
								ServerCommunication.instance(socket, listn);
							}
							catch (IOException e) {
								e.printStackTrace();
								if(socket!=null){
									try {
										socket.close();
									}
									catch (IOException e1) {
										e1.printStackTrace();
									}
								}
							}
						}
					}
					Thread conn = new BTConnection();
					conn.start();
				}
			}
		});
	}
	private void avviaClient(int rete) {
		switch(rete){
			case BT_MODE:
				avviaClientBluetooth();
				break;
			case WIFI_MODE:
				avviaClientWifi();
				break;
			default:
				onBackPressed();
		}
	}
	private void avviaClientBluetooth(){
		bluetoothAdp = BluetoothAdapter.getDefaultAdapter();
		if(bluetoothAdp!=null){
    		setContentView(R.layout.activity_client_lobby_bt);
    		disegnaBluetooth();
    		bluetoothAdp.startDiscovery();
		}
		else
			Toast.makeText(getApplicationContext(), "Adattatore bluetooth non trovato", Toast.LENGTH_LONG).show();
	}
	private void avviaClientWifi(){
		setContentView(R.layout.activity_client_lobby_wifi);
		disegnaWiFi();
	}
	public void back(View v){
		onBackPressed();
	}
	@Override
	public void onBackPressed() {
		if(!closeClient()){
			Intent i = new Intent(getApplicationContext(), FirstPageActivity.class);
    		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    		startActivity(i);
		}
		else
			super.onBackPressed();
	}

	private boolean closeClient() {
		if(comm!=null){
			comm.disconnect(true);
			if(bluetoothAdp!=null && bluetoothAdp.isEnabled()){
				bluetoothAdp.disable();
			}
			return true;
		}
		return false;
	}
	private String address, nickname;
	public void connettiWifi(View v){
		nickname = nickNameET.getText().toString().trim();
		if(nickname.length()==0){
			Toast.makeText(getApplicationContext(), R.string.nickname_vuoto, Toast.LENGTH_LONG).show();
			return;
		}
		address = ipAddress.getText().toString().trim();
		if(!isValidIpAddress(address)){
			Toast.makeText(getApplicationContext(), "Ip Address non valido", Toast.LENGTH_LONG).show();
			return;
		}
		Editor prefs_edit = prefs.edit();
		prefs_edit.putString("nickname", nickname);
		prefs_edit.putString("last_server_ip", address);
		prefs_edit.commit();
		if(ServerCommunication.getInstance()==null){
			ServerCommunication.instance(address, ServerWiFi.SERVER_PORT, getConnectionListener());
			comm = ServerCommunication.getInstance();
			btnConnetti.setText(R.string.disconnetti);
		}
		else {
			comm.forceDisconnect();
			btnConnetti.setText(R.string.connetti);
		}
	}
	private boolean isValidIpAddress(String ip){
		if(ip==null || ip.length()==0 || ip.length()>15)
			return false;
		String[] values = ip.split("\\.");
		if(values.length!=4)
			return false;
		for(int i=0;i<values.length;i++){
			try {
				Integer n = Integer.parseInt(values[i]);
				if(i==0){
					if(n<=0 || n>255)
						return false;
				}
				else if(n<0 || n>255)
					return false;
			}
			catch(Exception e){
				return false;
			}
		}
		return true;
	}
	private Map<String,Listener> getConnectionListener(){
		
		Map<String,Listener> map = new HashMap<String, Listener>();
		Listener clientDisconnected = new Listener() {
			public void execute(String... p) {
				runOnUiThread(new Runnable() {
					public void run() {
						if(rete_host==BT_MODE){
							bluetoothAdp.disable();
						}
						Toast.makeText(getApplicationContext(), R.string.connection_lost, Toast.LENGTH_SHORT).show();
						Intent i = new Intent(getApplicationContext(), FirstPageActivity.class);
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(i);
					}
				});
			}
		};
		map.put("clientDisconnected", clientDisconnected);
		
		Listener clientConnected = new Listener() {
			public void execute(String... p) {
				bluetoothAdp.cancelDiscovery();
				runOnUiThread(new Runnable(){
					public void run() {}
				});
			}
		};
		map.put("clientConnected", clientConnected);
		
		Listener sceltaGioco = new Listener() {
			public void execute(final String... p) {
				runOnUiThread(new Runnable() {
					public void run() {
						Integer gioco = Integer.parseInt(p[0]);
						Integer difficolta = Integer.parseInt(p[1]);
						
						Intent i = null;
						switch(gioco){
							case Giochi.ENDURANCE:
								i=new Intent(getApplicationContext(), EnduranceActivityMulti.class);
								break;
							case Giochi.MEMORY:
								i=new Intent(getApplicationContext(), MemoryActivityMulti.class);
								break;
							case Giochi.QUIZ_SHOW:
								i=new Intent(getApplicationContext(), QuizShowActivityMulti.class);
								break;
							case Giochi.RIFLESSI:
								i=new Intent(getApplicationContext(), RiflessiActivityMulti.class);
								break;
							case Giochi.SCRIVI_LA_RISPOSTA:
								i=new Intent(getApplicationContext(), ScriviRispostaActivityMulti.class);
								break;
							case Giochi.SFIDA_CONTRO_IL_TEMPO:
								i=new Intent(getApplicationContext(), SfidaTempoActivityMulti.class);
								break;
							case Giochi.TRUE_FALSE:
								i=new Intent(getApplicationContext(), VeroFalsoActivityMulti.class);
								break;
						}
						if(i!=null){
							Log.d("CLobby","avvio activity");
							i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							i.putExtra("difficolta", difficolta);
							i.putExtra("nickname", nickname);
							i.putExtra("server", false);
							removeListeners();
							if(wait_dialog!=null && wait_dialog.isShowing())
								wait_dialog.dismiss();
							startActivity(i);
						}
						else
							Log.d("CLobby","errore activity");
					}
				});
			}
		};
		map.put("sceltaGioco", sceltaGioco);
		
		Listener onWait = new Listener() {
			public void execute(String... p) {
				runOnUiThread(new Runnable() {
					public void run() {
						AlertDialog.Builder miaAlert = new AlertDialog.Builder(ClientLobbyActivity.this);
						miaAlert.setTitle(R.string.attendi_title);
						miaAlert.setMessage(R.string.attendi_selezione_gioco);
						miaAlert.setCancelable(false);
						wait_dialog=miaAlert.create();
						wait_dialog.show();
					}
				});
			}
		};
		map.put("wait", onWait);
		
		return map;
	}
	private void removeListeners(){
		ServerCommunication.getInstance().getListeners().removeListener("wait");
	}
	private ArrayAdapter<String> bt_found;
	private ArrayList<BluetoothDevice> bt_devices;
	private final BroadcastReceiver btFoundDevice = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        // When discovery finds a device
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	            // Get the BluetoothDevice object from the Intent
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            // Add the name and address to an array adapter to show in a ListView
	            bt_found.add(device.getName()+"\n"+device.getAddress());
	            bt_devices.add(device);
	        }
	    }
	};
	protected void onDestroy() {
		unregisterReceiver(btFoundDevice);
	};
}
