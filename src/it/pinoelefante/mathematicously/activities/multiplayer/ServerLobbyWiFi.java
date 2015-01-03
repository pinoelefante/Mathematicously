package it.pinoelefante.mathematicously.activities.multiplayer;

import it.pinoelefante.mathematicously.R;
import it.pinoelefante.mathematicously.activities.FirstPageActivity;
import it.pinoelefante.mathematicously.server.Listener;
import it.pinoelefante.mathematicously.server.Server;
import it.pinoelefante.mathematicously.server.ServerCommunication;
import it.pinoelefante.mathematicously.server.ServerWiFi;
import it.pinoelefante.mycustomviews.PActivity;
import it.pinoelefante.mycustomviews.PButton;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ServerLobbyWiFi extends PActivity {
	private WifiManager wifiManager;
	private String nickname;
	private Server server;
	private SharedPreferences prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		setContentView(R.layout.activity_server_lobby_wifi);
		disegna();
	}
	private EditText nicknameET, ipaddressET;
	private PButton backButton;
	private TextView attesa_tv;
	
	private void disegna(){
		int dimButt = 0;
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
			dimButt = calcolaDimensioniW(10);
		else
			dimButt = calcolaDimensioniH(10);
		
		nicknameET = (EditText) findViewById(R.id.NicknameWF);
		nickname = prefs.getString("nickname", "Server");
		nicknameET.setText(nickname);
		
		ipaddressET = (EditText) findViewById(R.id.ServerLobbyIpAddress);
		ipaddressET.setText(getWifiIP());
		
		backButton = (PButton) findViewById(R.id.backWFS);
		backButton.setSize(dimButt, dimButt);
		
		attesa_tv = (TextView) findViewById(R.id.statoConnessioneWifi);
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
		
		if(!isWifiEnabled()){
			Toast.makeText(getApplicationContext(), "Abilitare la rete WiFi", Toast.LENGTH_LONG).show();
			return;
		}
		String ipAddress = getWifiIP(); 
		if(ipAddress==null || ipAddress.length()==0){
			Toast.makeText(getApplicationContext(), "Ottenimento ip in corso", Toast.LENGTH_LONG).show();
			return;
		}
		ipaddressET.setText(ipAddress);
		v.setVisibility(View.INVISIBLE);
		attesa_tv.setVisibility(View.VISIBLE);
		nicknameET.setEnabled(false);
		startServer();
	}
	
	private Thread connection;
	public void startServer(){
		class ThreadConnection extends Thread {
			public void run(){
				server = ServerWiFi.getInstance();
				server.addListeners(getConnectionListener());
				server.avviaServer();
			}
		}
		connection = new ThreadConnection();
		connection.start();
	}
	@Override
	public void onBackPressed() {
		closeServer();
		super.onBackPressed();
	}
	
	public void closeServer(){
		if(server!=null)
			server.stopServer();
		if(connection!=null)
			connection.interrupt();
	}
	private String getWifiIP(){
	    int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

	    // Convert little-endian to big-endianif needed
	    if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
	        ipAddress = Integer.reverseBytes(ipAddress);
	    }

	    byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

	    String ipAddressString;
	    try {
	        ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
	    } 
	    catch (UnknownHostException ex) {
	        Log.e("WIFIIP", "Unable to get host address.");
	        ipAddressString = "";
	    }
	    return ipAddressString;
	}
	private boolean isWifiEnabled(){
		if(wifiManager==null){
			wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			if(wifiManager==null)
				return false;
		}
		return wifiManager.isWifiEnabled();
	}
	public void back(View v){
		onBackPressed();
	}
	
	public Map<String,Listener> getConnectionListener(){
		Map<String,Listener> lists = new HashMap<String,Listener>();
		Listener clientDisconnected = new Listener() {
			public void execute(String... p) {
				runOnUiThread(new Runnable() {
					public void run() {
						ServerWiFi.getInstance().stopServer();
						closeServer();
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
