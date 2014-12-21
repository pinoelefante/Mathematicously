package it.pinoelefante.mathematicously.activities;

import it.pinoelefante.mathematicously.R;
import it.pinoelefante.mathematicously.activities.multiplayer.ClientLobbyBluetooth;
import it.pinoelefante.mathematicously.activities.multiplayer.ClientLobbyWiFi;
import it.pinoelefante.mathematicously.activities.multiplayer.ServerLobbyBluetooth;
import it.pinoelefante.mathematicously.activities.multiplayer.ServerLobbyWiFi;
import it.pinoelefante.mycustomviews.PActivity;
import it.pinoelefante.mycustomviews.PButton;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

public class SfidaAmicoActivity extends PActivity {
	private BluetoothAdapter bluetoothAdapter;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (VERSION.SDK_INT >= 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		setContentView(R.layout.activity_multiplayer);
		disegna();
	}
	
	private CheckBox bt, wifi;
	private PButton btnBT, btnWifi;
	private void disegna(){
		bt = (CheckBox) findViewById(R.id.chkBT);
		wifi = (CheckBox) findViewById(R.id.chkWifi);
		btnBT = (PButton) findViewById(R.id.selectBluetooth);
		btnBT.setBackgroundResource(R.drawable.style_bluetooth);
		btnWifi = (PButton) findViewById(R.id.selectWifi);
		btnWifi.setBackgroundResource(R.drawable.style_wifi);
		if(bluetoothAdapter==null)
			btnBT.setEnabled(false);
		int dimButton = 0;
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			dimButton = calcolaDimensioniW(20);
		}
		else {
			dimButton = calcolaDimensioniH(20);
		}
		btnBT.setSize(dimButton, dimButton);
		btnWifi.setSize(dimButton, dimButton);
	}
	
	public void attivaBT(View v){
		boolean btAbilitato = bt.isChecked();
		if(btAbilitato){
			bt.setChecked(false);
			wifi.setChecked(false);
		}
		else {
			bt.setChecked(true);
			wifi.setChecked(false);
		}
	}
	
	public void attivaWifi(View v){
		boolean wifiAbilitato = wifi.isChecked();
		if(wifiAbilitato){
			wifi.setChecked(false);
			bt.setChecked(false);
		}
		else {
			wifi.setChecked(true);
			bt.setChecked(false);
		}
	}
	public void hostGame(View v){
		boolean w = wifi.isChecked();
		boolean b = bt.isChecked();
		Toast.makeText(getApplicationContext(), w +" "+b, Toast.LENGTH_LONG).show();
		if(w && b){
			Toast.makeText(getApplicationContext(), "Non puoi selezionare due reti", Toast.LENGTH_SHORT).show();
			return;
		}
		if(!w && !b){
			Toast.makeText(getApplicationContext(), "Devi selezionare una rete", Toast.LENGTH_SHORT).show();
			return;
		}
		Intent i = null;
		if(w){
			i = new Intent(getApplicationContext(), ServerLobbyWiFi.class);
		}
		else if(b){
			i = new Intent(getApplicationContext(), ServerLobbyBluetooth.class);
		}
		startActivity(i);
	}
	public void joinGame(View v){
		boolean w = wifi.isChecked();
		boolean b = bt.isChecked();
		if(w && b){
			Toast.makeText(getApplicationContext(), "Non puoi selezionare due reti", Toast.LENGTH_SHORT).show();
			return;
		}
		if(!w && !b){
			Toast.makeText(getApplicationContext(), "Devi selezionare una rete", Toast.LENGTH_SHORT).show();
			return;
		}
		if(b && bluetoothAdapter!=null && !bluetoothAdapter.isEnabled()){
			bluetoothAdapter.enable();
		}
		Intent i = null;
		if(w){
			i = new Intent(getApplicationContext(), ClientLobbyWiFi.class);
		}
		else if(b){
			i = new Intent(getApplicationContext(), ClientLobbyBluetooth.class);
		}
		startActivity(i);
	}
}
