package it.pinoelefante.mathematicously.activities.multiplayer.games;

import it.pinoelefante.mathematicously.R;
import it.pinoelefante.mathematicously.activities.games.EnduranceActivity;
import it.pinoelefante.mathematicously.constants.Giochi;
import it.pinoelefante.mathematicously.generator.Domanda;
import it.pinoelefante.mathematicously.server.ServerCommunication;
import android.app.AlertDialog;
import android.os.Bundle;

public class EnduranceActivityMulti extends EnduranceActivity {
	private ServerCommunication comm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		comm = ServerCommunication.getInstance();
		comm.addListeners(getGameListener());
		super.onCreate(savedInstanceState);
		tipo_partita = Giochi.TIPO_ENDURANCE_MULTI;
		context = EnduranceActivityMulti.this;
	}

	@Override
	public void prossimaDomanda() {
		if (isServer) {
			if (n_domande >= domande.size()) {
				Domanda d = generaDomanda();
				domande.add(d);
			}
		}
		else {
			comm.write("getDomanda " + n_domande);
			while (n_domande >= domande.size()) {
				try {
					Thread.sleep(100);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		n_domande++;
	}

	@Override
	public void finePartita() {
		if (timer != null)
			timer.stop();
		isGameOver = true;
		
		if(wait_dialog == null){
			AlertDialog.Builder miaAlert = new AlertDialog.Builder(context);
			miaAlert.setTitle(R.string.attendi_title);
			miaAlert.setMessage(R.string.attendi_messaggio);
			miaAlert.setCancelable(false);
			wait_dialog = miaAlert.create();
		}
		if(!isOpponentOver)
			wait_dialog.show();
		
		class ThreadFinePartita extends Thread {
			public void run(){
				while (!isOpponentOver) {
					try {
						comm.write("requestIsOver");
						Thread.sleep(2000);
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				runOnUiThread(new Runnable() {
					public void run() {
						wait_dialog.dismiss();
					}
				});
				if (!isServer) {
					String serverM = "finishRequest " + nickname;
					for (int i = 0; i < domande.size(); i++) {
						Domanda d = domande.get(i);
						serverM += " " + d.isRispostaEsatta() + " " + d.getTempoRisposta();
					}
					comm.write(serverM);
				}
			}
		}
		Thread f = new ThreadFinePartita();
		f.start();
	}
}
