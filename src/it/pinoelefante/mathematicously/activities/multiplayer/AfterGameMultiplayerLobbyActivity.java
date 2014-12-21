package it.pinoelefante.mathematicously.activities.multiplayer;

import it.pinoelefante.mathematicously.R;
import it.pinoelefante.mathematicously.server.Listener;
import it.pinoelefante.mathematicously.server.ServerCommunication;
import it.pinoelefante.mycustomviews.PActivity;
import it.pinoelefante.mycustomviews.PButton;

import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class AfterGameMultiplayerLobbyActivity extends PActivity {
	private boolean isServer, respOpponent, myResp, opponentIsIn;
	private ServerCommunication comm;
	private AlertDialog wait_dialog;
	private boolean isIResp, isOppResp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		comm = ServerCommunication.getInstance();
		isServer = comm.isServer();
		comm.addListeners(getListeners());
		
		setContentView(R.layout.activity_after_game_multiplayer_lobby);
		disegna();
		
		AlertDialog.Builder miaAlert = new AlertDialog.Builder(AfterGameMultiplayerLobbyActivity.this);
		miaAlert.setTitle(R.string.attendi_title);
		miaAlert.setMessage(R.string.attendi_messaggio);
		miaAlert.setCancelable(false);
		wait_dialog = miaAlert.create();		
		wait_dialog.show();
		
		class ThreadWait extends Thread {
			public void run(){
				while(!opponentIsIn){
					comm.write("aftergameRequestIsIn");
					try {
						sleep(2000);
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				wait_dialog.dismiss();
			}
		}
		Thread t = new ThreadWait();
		t.start();
	}
	private ImageView img_server, img_client;
	private PButton btnYes, btnNo;
	private void disegna(){
		img_server = (ImageView) findViewById(R.id.aftergame_risp_server);
		img_client = (ImageView) findViewById(R.id.aftergame_risp_client);
		btnYes = (PButton) findViewById(R.id.lobby_aftergame_si);
		btnNo = (PButton) findViewById(R.id.lobby_aftergame_no);
		int dimButton = 0;
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			dimButton = calcolaDimensioniW(10);
		}
		else {
			dimButton = calcolaDimensioniH(10);
		}
		btnYes.setSize(dimButton, dimButton);
		btnNo.setSize(dimButton, dimButton);
	}
	public void lobby_no(View v){
		if(isServer){
			img_server.setBackgroundResource(R.drawable.lobby_no);
		}
		else {
			img_client.setBackgroundResource(R.drawable.lobby_no);
		}
		comm.write("continue false");
		comm.disconnect(true);
	}
	public void lobby_yes(View v){
		if(isServer){
			img_server.setBackgroundResource(R.drawable.lobby_yes);
		}
		else {
			img_client.setBackgroundResource(R.drawable.lobby_yes);
		}
		myResp=true;
		isIResp=true;
		comm.write("continue true");
		if(!isOppResp)
			wait_dialog.show();
		else {
			if(myResp && respOpponent){
				if(isServer){
					Intent i = new Intent(getApplicationContext(), ScegliGiocoMultiplayerActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					removeListeners();
					startActivity(i);
				}
				else {
					removeListeners();
					wait_dialog.setMessage(getString(R.string.attendi_selezione_gioco));
					if(!wait_dialog.isShowing())
						wait_dialog.show();
				}
			}
			else {
				comm.disconnect(true);
			}
		}
	}
	@Override
	public void onBackPressed() {
		comm.disconnect(true);
	}
	private Map<String,Listener> getListeners(){
		Map<String,Listener> map = new HashMap<String,Listener>();
		
		Listener onRequestIsIn = new Listener() {
			public void execute(String... p) {
				comm.write("aftergameResponseIsIn true");
			}
		};
		map.put("aftergameRequestIsIn", onRequestIsIn);
		
		Listener onResponseIsIn = new Listener() {
			public void execute(String... p) {
				if(p!=null && p.length!=0){
					opponentIsIn = Boolean.parseBoolean(p[0]);
					runOnUiThread(new Runnable() {
						public void run() {
							if(opponentIsIn && wait_dialog!=null && wait_dialog.isShowing()){
								wait_dialog.dismiss();
							}
						}
					});
				}
			}
		};
		map.put("aftergameResponseIsIn", onResponseIsIn);
		
		Listener onOptionSelected = new Listener() {
			public void execute(String... p) {
				respOpponent = Boolean.parseBoolean(p[0]);
				isOppResp = true;
				runOnUiThread(new Runnable() {
					public void run() {
						if(isServer){
							img_client.setBackgroundResource(respOpponent?R.drawable.lobby_yes:R.drawable.lobby_no);
						}
						else {
							img_server.setBackgroundResource(respOpponent?R.drawable.lobby_yes:R.drawable.lobby_no);
						}
						if(!respOpponent){
							comm.disconnect(true);
						}
						if(isIResp && isOppResp){
							if(myResp && respOpponent){
								if(isServer){
									Intent i = new Intent(getApplicationContext(), ScegliGiocoMultiplayerActivity.class);
									i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									removeListeners();
									startActivity(i);
								}
								else {
									removeListeners();
									wait_dialog.setMessage(getString(R.string.attendi_selezione_gioco));
									wait_dialog.show();
								}
							}
							else {
								comm.disconnect(true);
							}
						}
					}
				});
			}
		};
		map.put("aftergameOptionSelected", onOptionSelected);
		
		return map;
	}
	private void removeListeners(){
		comm.getListeners().removeListeners(
			"aftergameOptionSelected",
			"aftergameResponseIsIn",
			"aftergameRequestIsIn"
		);
	}
}
