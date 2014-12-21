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

import android.app.AlertDialog;
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
import android.widget.Toast;

public class ClientLobbyWiFi extends PActivity {
	private AlertDialog		 wait_dialog;
	private String			  nickname;
	private SharedPreferences   prefs;
	private WifiManager		 wifiManager;
	private ServerCommunication comm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		setContentView(R.layout.activity_client_lobby_wifi);
		disegna();
	}

	private EditText nicknameET, ipaddressET;
	private PButton  backButton, connectBtn;

	private void disegna() {
		int dimButt = 0;
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
			dimButt = calcolaDimensioniW(10);
		else
			dimButt = calcolaDimensioniH(10);

		nicknameET = (EditText) findViewById(R.id.nicknameWFC);
		nickname = prefs.getString("nickname", "Client");
		nicknameET.setText(nickname);

		ipaddressET = (EditText) findViewById(R.id.ipaddressWFC);
		ipaddress = prefs.getString("last_server_ip", defaultIpAddress());
		ipaddressET.setText(ipaddress);

		backButton = (PButton) findViewById(R.id.backWFC);
		backButton.setSize(dimButt, dimButt);

		connectBtn = (PButton) findViewById(R.id.wifi_connect_server);
	}

	private String defaultIpAddress() {
		if (wifiManager.isWifiEnabled()) {
			String myIp = getWifiIP();
			if (myIp != null) {
				String addr = "";
				String[] ns = myIp.split("\\.");
				for (int i = 0; i < ns.length - 1; i++) {
					addr += ns[i] + ".";
				}
				return addr;
			}
		}
		return "0.0.0.0";
	}

	private String getWifiIP() {
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

	private String ipaddress;

	public void connettiWiFi(View v) {
		if (!wifiManager.isWifiEnabled()) {
			Toast.makeText(getApplicationContext(), R.string.wifi_non_abilitato, Toast.LENGTH_SHORT).show();
			return;
		}
		if (nicknameET.getText().toString().trim().length() == 0) {
			Toast.makeText(getApplicationContext(), R.string.nickname_vuoto, Toast.LENGTH_LONG).show();
			return;
		}
		nickname = nicknameET.getText().toString();
		if (!isValidIpAddress(ipaddressET.getText().toString())) {
			Toast.makeText(getApplicationContext(), R.string.ip_address_non_valido, Toast.LENGTH_LONG).show();
			return;
		}
		ipaddress = ipaddressET.getText().toString();

		Editor prefs_edit = prefs.edit();
		prefs_edit.putString("nickname", nickname);
		prefs_edit.putString("last_server_ip", ipaddress);
		prefs_edit.commit();

		connetti();
	}

	private boolean isValidIpAddress(String ip) {
		if (ip == null || ip.length() == 0 || ip.length() > 15)
			return false;
		String[] values = ip.split("\\.");
		if (values.length != 4)
			return false;
		for (int i = 0; i < values.length; i++) {
			try {
				Integer n = Integer.parseInt(values[i]);
				if (i == 0) {
					if (n <= 0 || n > 255)
						return false;
				}
				else if (n < 0 || n > 255)
					return false;
			}
			catch (Exception e) {
				return false;
			}
		}
		return true;
	}

	public void connetti() {
		if (ServerCommunication.getInstance() == null) {
			ServerCommunication.instance(ipaddress, ServerWiFi.SERVER_PORT, getConnectionListener());
			comm = ServerCommunication.getInstance();
			connectBtn.setText(R.string.disconnetti);
			nicknameET.setEnabled(false);
			ipaddressET.setEnabled(false);
		}
		else {
			comm.forceDisconnect();
			connectBtn.setText(R.string.connetti);
			nicknameET.setEnabled(true);
			ipaddressET.setEnabled(true);
		}
	}

	private Map<String, Listener> getConnectionListener() {

		Map<String, Listener> map = new HashMap<String, Listener>();
		Listener clientDisconnected = new Listener() {
			public void execute(String... p) {
				runOnUiThread(new Runnable() {
					public void run() {
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
				runOnUiThread(new Runnable() {
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
						switch (gioco) {
							case Giochi.ENDURANCE:
								i = new Intent(getApplicationContext(), EnduranceActivityMulti.class);
								break;
							case Giochi.MEMORY:
								i = new Intent(getApplicationContext(), MemoryActivityMulti.class);
								break;
							case Giochi.QUIZ_SHOW:
								i = new Intent(getApplicationContext(), QuizShowActivityMulti.class);
								break;
							case Giochi.RIFLESSI:
								i = new Intent(getApplicationContext(), RiflessiActivityMulti.class);
								break;
							case Giochi.SCRIVI_LA_RISPOSTA:
								i = new Intent(getApplicationContext(), ScriviRispostaActivityMulti.class);
								break;
							case Giochi.SFIDA_CONTRO_IL_TEMPO:
								i = new Intent(getApplicationContext(), SfidaTempoActivityMulti.class);
								break;
							case Giochi.TRUE_FALSE:
								i = new Intent(getApplicationContext(), VeroFalsoActivityMulti.class);
								break;
						}
						if (i != null) {
							Log.d("CLobby", "avvio activity");
							i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							i.putExtra("difficolta", difficolta);
							i.putExtra("nickname", nickname);
							i.putExtra("server", false);
							removeListeners();
							if (wait_dialog != null && wait_dialog.isShowing())
								wait_dialog.dismiss();
							startActivity(i);
						}
						else
							Log.d("CLobby", "errore activity");
					}
				});
			}
		};
		map.put("sceltaGioco", sceltaGioco);

		Listener onWait = new Listener() {
			public void execute(String... p) {
				runOnUiThread(new Runnable() {
					public void run() {
						AlertDialog.Builder miaAlert = new AlertDialog.Builder(ClientLobbyWiFi.this);
						miaAlert.setTitle(R.string.attendi_title);
						miaAlert.setMessage(R.string.attendi_selezione_gioco);
						miaAlert.setCancelable(false);
						wait_dialog = miaAlert.create();
						wait_dialog.show();
					}
				});
			}
		};
		map.put("wait", onWait);

		return map;
	}

	private void removeListeners() {
		ServerCommunication.getInstance().getListeners().removeListener("wait");
	}
}
