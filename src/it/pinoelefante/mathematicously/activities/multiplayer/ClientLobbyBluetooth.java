package it.pinoelefante.mathematicously.activities.multiplayer;

import it.pinoelefante.mathematicously.R;
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
import it.pinoelefante.mycustomviews.PActivity;
import it.pinoelefante.mycustomviews.PButton;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ClientLobbyBluetooth extends PActivity {
	private static final int REQUEST_ENABLE_BT = 1;
	private BluetoothAdapter btAdp;
	private SharedPreferences prefs;
	private AlertDialog wait_dialog;
	private String nickname;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		btAdp = BluetoothAdapter.getDefaultAdapter();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		setContentView(R.layout.activity_client_lobby_bt);
		disegna();
		avviaDispositivoBT();
	}
	private EditText nickname_et;
	private ListView listView_bt_devices;
	private PButton backButton;
	private ArrayAdapter<String> list_devices;
	private Map<String, BluetoothDevice> mac_device;
	private void disegna(){
		int dimButt = 0;
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
			dimButt = calcolaDimensioniW(10);
		else
			dimButt = calcolaDimensioniH(10);
		
		backButton = (PButton) findViewById(R.id.back_BTC);
		backButton.setSize(dimButt, dimButt);
		
		nickname_et = (EditText) findViewById(R.id.Nickname_BTC);
		nickname = prefs.getString("nickname", "");
		nickname_et.setText(nickname);
		
		listView_bt_devices = (ListView) findViewById(R.id.bt_list_devices);
		list_devices = new ArrayAdapter<String>(getApplicationContext(), R.layout.bt_row, R.id.textViewList);
		listView_bt_devices.setAdapter(list_devices);
		mac_device = new HashMap<String,BluetoothDevice>();
		listView_bt_devices.setOnItemClickListener(new ListView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String opponent_address = (String) parent.getItemAtPosition(position);
				final String mac_address = opponent_address.split("\n")[1];
				connectToMac(mac_address);
			}
		});
	}
	public void back(View v){
		onBackPressed();
	}
	private void avviaDispositivoBT(){
		registraBroadcast();
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(btFoundDevice, filter);
		if(!btAdp.isEnabled()){
			abilitaBluetooth();
		}
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
				
			}
			else {
				Toast.makeText(getApplicationContext(), "operazione cancellata - attiva bluetooth", Toast.LENGTH_SHORT).show();
				onBackPressed();
			}
		}
	}
	private BroadcastReceiver bt_state;
	private void registraBroadcast() {
		bt_state = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				switch(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)){
					case -1:
						break;
					case BluetoothAdapter.STATE_ON:
						Toast.makeText(getApplicationContext(), "operazione start discovery", Toast.LENGTH_SHORT).show();
						btAdp.startDiscovery();
						break;
					case BluetoothAdapter.STATE_OFF:
						Toast.makeText(getApplicationContext(), "bluetooth off", Toast.LENGTH_SHORT).show();
						list_devices.clear();
						mac_device.clear();
						abilitaBluetooth();
						break;
				}
			}
		};
		registerReceiver(bt_state, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
	}
	public Map<String,Listener> getConnectionListener(){
		Map<String,Listener> lists = new HashMap<String,Listener>();
		Listener clientDisconnected = new Listener() {
			public void execute(String... p) {
				runOnUiThread(new Runnable() {
					public void run() {
						btAdp.cancelDiscovery();
						Toast.makeText(getApplicationContext(), "Disconnessione bt", Toast.LENGTH_SHORT).show();
					}
				});
			}
		};
		lists.put("clientDisconnected", clientDisconnected);
		
		Listener clientConnected = new Listener() {
			public void execute(String... p) {
				runOnUiThread(new Runnable() {
					public void run() {
						btAdp.cancelDiscovery();
						Toast.makeText(getApplicationContext(), "Connessione bt avvenuta", Toast.LENGTH_SHORT).show();
					}
				});
			}
		};
		lists.put("clientConnected", clientConnected);
		
		Listener commandArrived = new Listener() {
			public void execute(String... p) {
				runOnUiThread(new Runnable() {
					public void run() {
						btAdp.cancelDiscovery();
						Toast.makeText(getApplicationContext(), "arrivo comando bt", Toast.LENGTH_SHORT).show();
					}
				});
			}
		};
		lists.put("commandArrived", commandArrived);
		
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
		lists.put("sceltaGioco", sceltaGioco);
		
		Listener onWait = new Listener() {
			public void execute(String... p) {
				runOnUiThread(new Runnable() {
					public void run() {
						AlertDialog.Builder miaAlert = new AlertDialog.Builder(ClientLobbyBluetooth.this);
						miaAlert.setTitle(R.string.attendi_title);
						miaAlert.setMessage(R.string.attendi_selezione_gioco);
						miaAlert.setCancelable(false);
						wait_dialog=miaAlert.create();
						wait_dialog.show();
					}
				});
			}
		};
		lists.put("wait", onWait);
		
		return lists;
	}
	private void removeListeners() {
		ServerCommunication.getInstance().getListeners().removeListener("wait");
	}
	private final BroadcastReceiver btFoundDevice = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        // When discovery finds a device
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	            // Get the BluetoothDevice object from the Intent
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            // Add the name and address to an array adapter to show in a ListView
	            list_devices.add(device.getName()+"\n"+device.getAddress());
	            mac_device.put(device.getAddress(), device);
	        }
	    }
	};
	protected void onDestroy() {
		unregisterReceiver(btFoundDevice);
	};
	private void connectToMac(final String mac_address){
		nickname = nickname_et.getText().toString();
		if(nickname.length()==0){
			Toast.makeText(getApplicationContext(), "Inserire nickname", Toast.LENGTH_SHORT).show();
			return;
		}
		else {
			Editor edit = prefs.edit();
			edit.putString("nickname", nickname);
			edit.commit();
		}
		new Thread(){
			public void run() {
				showToast("Connessione a: "+mac_address);
				BluetoothDevice device = mac_device.get(mac_address);
				try {
					BluetoothSocket socket = device.createRfcommSocketToServiceRecord(ServerBluetooth.ITU_GENERATED_UUID);
					showToast("Connessione a: "+mac_address+" avvenuta");
					PartialListenerContainer c = new PartialListenerContainer();
					c.addListener(getConnectionListener());
					ServerCommunication.instance(socket, c);
				}
				catch (IOException e) {
					showToast("Connessione a: "+mac_address+" fallita");
					e.printStackTrace();
				}
			}
			private void showToast(final String m){
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(getApplicationContext(), m, Toast.LENGTH_SHORT).show();
					}
				});
			}
		}.start();
	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}
}
