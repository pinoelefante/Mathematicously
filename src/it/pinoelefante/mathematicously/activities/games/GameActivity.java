package it.pinoelefante.mathematicously.activities.games;

import it.pinoelefante.mathematicously.activities.FinePartitaActivity;
import it.pinoelefante.mathematicously.activities.multiplayer.FinePartitaMultiActivity;
import it.pinoelefante.mathematicously.constants.Difficolta;
import it.pinoelefante.mathematicously.database.MyEntry;
import it.pinoelefante.mathematicously.generator.Domanda;
import it.pinoelefante.mathematicously.server.Listener;
import it.pinoelefante.mathematicously.server.Parser;
import it.pinoelefante.mathematicously.server.ServerCommunication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public abstract class GameActivity extends Activity {
	protected ArrayList<Domanda> domande;
	protected int difficolta, n_domande = 0;
	protected Random rand;
	protected boolean isServer, isGameOver, isOpponentOver;
	protected String nickname, tipo_partita, nickname_avversario;
	protected ArrayList<MyEntry<Boolean,Float>> datiAvversario;
	protected AlertDialog wait_dialog;
	protected Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		domande = new ArrayList<Domanda>();
		difficolta = getIntent().getIntExtra("difficolta", Difficolta.CASUALE);
		isServer = getIntent().getBooleanExtra("server", false);
		nickname = getIntent().getStringExtra("nickname");
		rand = new Random(System.currentTimeMillis());
	}
	
	protected Map<String,Listener> getGameListener() {
		Map<String,Listener> map = new HashMap<String, Listener>();
		Listener prossimaDomanda = new Listener() {
			public void execute(String... p) {
				Integer n = Integer.parseInt(p[0]);
				Domanda d = null;
				while(domande==null){
					try {
					Thread.sleep(50);
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (n>=domande.size()) {
					d = generaDomanda();
					domande.add(n, d);
				}
				else {
					d = domande.get(n);
				}
				ServerCommunication.getInstance().write("domandaResponse " + d.getDomanda() + " " + d.getDifficolta() + " " + d.getRisposta(0) + " " + d.getRisposta(1) + " " + d.getRisposta(2) + " " + d.getRisposta(3));
			}
		};
		map.put("getDomanda", prossimaDomanda);
		
		Listener domandaResponse = new Listener() {
			public void execute(String... p) {
				Domanda d = Parser.parseDomanda(p[0]);
				domande.add(d);
				
			}
		};
		map.put("domandaResponse", domandaResponse);
		
		Listener finePartitaRequest = new Listener() {
			public void execute(final String... p) {
				runOnUiThread(new Runnable() {
					public void run() {
						String m = p[0];
						String[] l=m.split("\\s+");
						nickname_avversario = l[1];
						datiAvversario = new ArrayList<MyEntry<Boolean,Float>>();
						for(int i=2;i<l.length;i=i+2){
							Boolean r = Boolean.parseBoolean(l[i]);
							Float t = Float.parseFloat(l[i+1]);
							MyEntry<Boolean,Float> item = new MyEntry<Boolean, Float>(r, t);
							datiAvversario.add(item);
						}
						boolean vinto = calcolaVittoria();
						String resp = "finishResponse "+nickname+" "+(!vinto);
						ServerCommunication.getInstance().write(resp);
						removeListeners();
						Intent i=new Intent(getApplicationContext(), FinePartitaMultiActivity.class);
						i.putExtra("stato_partita", vinto);
						i.putExtra("nome_avversario", nickname_avversario);
						i.putExtra("difficolta", difficolta);
						i.putExtra("tipo_partita", tipo_partita);
						i.putExtra("esatte", contaEsatte());
						i.putExtra("totale", n_domande);
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						if(wait_dialog!=null && wait_dialog.isShowing())
							wait_dialog.dismiss();
						startActivity(i);
					}
					
				});
			}
		};
		map.put("finishRequest", finePartitaRequest);
		
		Listener finePartitaResponse = new Listener() {
			public void execute(String... p) {
				final String m = p[0];
				runOnUiThread(new Runnable() {
					public void run() {
						String[] cmd = m.split(" ");
						removeListeners();
						Intent i = new Intent(getApplicationContext(), FinePartitaMultiActivity.class);
						i.putExtra("nome_avversario", cmd[1]);
						i.putExtra("stato_partita", Boolean.parseBoolean(cmd[2]));
						i.putExtra("difficolta", difficolta);
						i.putExtra("tipo_partita", tipo_partita);
						i.putExtra("esatte", contaEsatte());
						i.putExtra("totale", n_domande);
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(i);
					}
				});
			}
		};
		map.put("finishResponse", finePartitaResponse);
		
		Listener requestPartitaOver = new Listener() {
			public void execute(String... p) {
				ServerCommunication.getInstance().write("responseIsOver "+isGameOver);
			}
		};
		map.put("requestIsOver", requestPartitaOver);
				
		Listener setPartitaOver = new Listener() {
			public void execute(String... p) {
				String m = p[0];
				String[] cmd = m.split("\\s+");
				Log.d("setPartitaOver", "cmd length="+cmd.length);
				for(int i=0;i<cmd.length;i++){
					Log.d("setPartitaOver", cmd[i]);
				}
				isOpponentOver = Boolean.parseBoolean(cmd[1]);
			}
		};		
		map.put("responseIsOver", setPartitaOver);
		return map;
	}
	protected void removeListeners(){
		ServerCommunication.getInstance().getListeners().removeListeners(
				"getDomanda",
				"responseIsOver",
				"requestIsOver",
				"finishResponse",
				"finishRequest",
				"domandaResponse"
		);
	}
	protected ArrayList<Integer> generaOrdine(int length) {
		ArrayList<Integer> dom = new ArrayList<Integer>();
		for (int i = 0; i < length; i++) {
			dom.add(i);
		}
		ArrayList<Integer> ordine = new ArrayList<Integer>();
		while (!dom.isEmpty()) {
			rand.setSeed(System.currentTimeMillis());
			Integer d = dom.remove(rand.nextInt(dom.size()));
			ordine.add(d);
		}
		return ordine;
	}
	@Override
	public void onBackPressed() {

	}
	public abstract void avviaGioco();

	public abstract void prossimaDomanda();

	public void finePartita(){
		isGameOver = true;
		Intent i = new Intent(getApplicationContext(), FinePartitaActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.putExtra("difficolta", difficolta);
		i.putExtra("domande", domande);
		i.putExtra("tipo_partita", tipo_partita);
		startActivity(i);
	}
	
	protected int contaEsatte(){
		int esatte = 0;
		for(int i=0;i<n_domande;i++){
			esatte+=(domande.get(i).isRispostaEsatta()?1:0);
		}
		return esatte;
	}
	
	private boolean calcolaVittoria(){
		int rispServer = 0, rispClient = 0;
		for(int i=0;i<domande.size();i++){
			Domanda d = domande.get(i);
			MyEntry<Boolean,Float> dc = (i>=datiAvversario.size()?null:datiAvversario.get(i));
			if(d.isRispostaEsatta()){
				if(dc==null || !dc.getKey()){
					rispServer++;
				}
				else {
					if(dc.getKey()){
						if(d.getTempoRisposta()<dc.getValue())
							rispServer++;
						else
							rispClient++;
					}
				}
			}
			else {
				if(dc!=null && dc.getKey()){
					rispClient++;
				}
			}
		}
		return rispServer>=rispClient;
	}
	
	public abstract Domanda generaDomanda();
	
	public abstract void pausaTimer();

	public abstract void scriviDomanda(Domanda d);

	public abstract boolean timerIsInPause();

	public abstract void rispostaEsatta(Domanda d);

	public abstract void rispostaErrata(Domanda d);
	
	public abstract float getResponseTime();
}
