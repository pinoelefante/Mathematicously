package it.pinoelefante.mathematicously.activities.games;

import it.pinoelefante.mathematicously.R;
import it.pinoelefante.mathematicously.activities.FinePartitaActivity;
import it.pinoelefante.mathematicously.constants.Difficolta;
import it.pinoelefante.mathematicously.constants.Giochi;
import it.pinoelefante.mathematicously.generator.Domanda;
import it.pinoelefante.mathematicously.generator.GeneratoreDifficile;
import it.pinoelefante.mathematicously.generator.GeneratoreFacile;
import it.pinoelefante.mathematicously.generator.GeneratoreMedio;
import it.pinoelefante.mathematicously.utilities.timer.Timer;
import it.pinoelefante.mathematicously.utilities.timer.TimerListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class VeroFalsoActivity extends TwoAnswerActivity {
	protected final static int NUMERO_DOMANDE = 10;
	private final static int DURATA_DOMANDA = 15;
	private Animation		ani;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		true_false_game = true;
		tipo_partita = Giochi.TIPO_TRUE_FALSE;
		disegna();
		ani = new AlphaAnimation(0.0f, 1.0f);
		ani.setAnimationListener(new AnimationListener() {
			public void onAnimationEnd(Animation animation) {
				overlay.setText("");
			}
			public void onAnimationStart(Animation animation) {}
			public void onAnimationRepeat(Animation animation) {}
		});
		ani.setDuration(1000L);
		progress.setMax(DURATA_DOMANDA * 1000);
		avviaGioco();
	}

	public void avviaGioco() {
		prossimaDomanda();
		scriviDomanda(domande.get(domande.size()-1));
	}

	public void prossimaDomanda() {
		if (n_domande < NUMERO_DOMANDE) {
			Domanda d = generaDomanda();
			domande.add(d);
			n_domande++;
		}
		else {
			if(timer!=null)
				timer.stop();
			finePartita();
		}
	}

	public void finePartita() {
		if(timer!=null)
			timer.stop();
		Intent i = new Intent(getApplicationContext(), FinePartitaActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.putExtra("difficolta", difficolta);
		i.putExtra("domande", domande);
		i.putExtra("tipo_partita", Giochi.TIPO_TRUE_FALSE);
		startActivity(i);
	}

	public void pausaTimer() {
		timer.setPause(500);
	}

	public void scriviDomanda(final Domanda d) {
		if(timer!=null)
			timer.stop();
		
		timer = new Timer(DURATA_DOMANDA);
		timer.addPauseListener(getTimerListener());
		
		int index = rand.nextInt(2);
		domanda.setText(d.getDomanda()+" = "+d.getRisposta(index));
		cliccabile = true;
		timer.avviaTimer();
	}

	public Domanda generaDomanda() {
		switch (difficolta) {
			case Difficolta.FACILE:
				return GeneratoreFacile.generaDomanda();
			case Difficolta.MEDIO:
				return GeneratoreMedio.generaDomanda();
			case Difficolta.DIFFICILE:
				return GeneratoreDifficile.generaDomanda();
			case Difficolta.CASUALE:
				switch (rand.nextInt(3)) {
					case Difficolta.FACILE:
						return GeneratoreFacile.generaDomanda();
					case Difficolta.MEDIO:
						return GeneratoreMedio.generaDomanda();
					case Difficolta.DIFFICILE:
						return GeneratoreDifficile.generaDomanda();
				}
		}
		return GeneratoreFacile.generaDomanda();
	}

	public boolean timerIsInPause() {
		return timer.isPaused();
	}

	public void rispostaEsatta(Domanda d) {
		showOverlay(ani,getString(R.string.risposta_esatta));
		//Toast.makeText(getApplicationContext(), "Risposta esatta", Toast.LENGTH_SHORT).show();
	}

	public void rispostaErrata(Domanda d) {
		showOverlay(ani,getString(R.string.risposta_errata));
		//Toast.makeText(getApplicationContext(), "Risposta errata", Toast.LENGTH_SHORT).show();
	}
	private TimerListener listener;
	private TimerListener getTimerListener(){
		if(listener==null){
			listener = new TimerListener() {
				
				@Override
				public void onTimerSleepEnd() {
					runOnUiThread(new Runnable() {
						public void run() {
							progress.setProgress(timer.getCurrentTime());
						}
					});
				}
				
				@Override
				public void onTimerInterrupt() {
					
				}
				
				@Override
				public void onTimerEnd() {
					runOnUiThread(new Runnable(){
						public void run() {
    						cliccabile = false;
    						prossimaDomanda();
    						timer.stop();
    						scriviDomanda(domande.get(n_domande-1));
						}
					});
				}
				
				@Override
				public void onScheduledPauseEnd() {
					runOnUiThread(new Runnable() {
						public void run() {
							resetButtonsColor();
							timer.stop();
							scriviDomanda(domande.get(n_domande-1));
						}
					});
				}
			};
		}
		return listener;
	}

	@Override
	public float getResponseTime() {
		return DURATA_DOMANDA-timer.getCurrentTimeF();
	}
}
