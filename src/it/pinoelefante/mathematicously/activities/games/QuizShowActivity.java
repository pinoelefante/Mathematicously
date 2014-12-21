package it.pinoelefante.mathematicously.activities.games;

import it.pinoelefante.mathematicously.constants.Difficolta;
import it.pinoelefante.mathematicously.constants.Giochi;
import it.pinoelefante.mathematicously.generator.Domanda;
import it.pinoelefante.mathematicously.generator.GeneratoreDifficile;
import it.pinoelefante.mathematicously.generator.GeneratoreFacile;
import it.pinoelefante.mathematicously.generator.GeneratoreMedio;
import it.pinoelefante.mathematicously.utilities.timer.Timer;
import it.pinoelefante.mathematicously.utilities.timer.TimerListener;

import java.util.ArrayList;

import android.os.Bundle;

public class QuizShowActivity extends FourAnswerActivity {
	protected final static int NUMERO_DOMANDE = 10;
	private final static int DURATA_DOMANDA = 15;
	protected Timer			timer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tipo_partita = Giochi.TIPO_QUIZ_SHOW;
		progress.setMax(DURATA_DOMANDA * 1000);
		avviaGioco();
	}

	@Override
	public void avviaGioco() {
		prossimaDomanda();
		scriviDomanda(domande.get(domande.size()-1));
	}

	@Override
	public void prossimaDomanda() {
		if (n_domande < NUMERO_DOMANDE) {
			Domanda d = generaDomanda();
			domande.add(d);
		}
		else {
			if(timer!=null)
				timer.stop();
			finePartita();
		}
		n_domande++;
	}

	@Override
	public void finePartita() {
		if(timer!=null)
			timer.stop();
		super.finePartita();
	}

	@Override
	public void pausaTimer() {
		timer.setPause(500);
	}

	@Override
	public void scriviDomanda(final Domanda d) {
		progress.setProgress(progress.getMax());
		
		if(timer!=null)
			timer.stop();
		
		timer = new Timer(DURATA_DOMANDA);
		timer.addPauseListener(getTimerListener());
		
		ArrayList<Integer> ordine = generaOrdine(4);
		domanda.setText(d.getDomanda());
		r1.setText(d.getRisposta(ordine.get(0)) + "");
		r2.setText(d.getRisposta(ordine.get(1)) + "");
		r3.setText(d.getRisposta(ordine.get(2)) + "");
		r4.setText(d.getRisposta(ordine.get(3)) + "");
		cliccabile = true;
		timer.avviaTimer();
	}

	@Override
	public Domanda generaDomanda() {
		switch (rand.nextInt(2) + 1) {
			case Difficolta.MEDIO:
				return GeneratoreMedio.generaDomanda();
			case Difficolta.DIFFICILE:
				return GeneratoreDifficile.generaDomanda();
		}
		return GeneratoreFacile.generaDomanda();
	}

	@Override
	public boolean timerIsInPause() {
		return timer.isPaused();
	}

	@Override
	public void rispostaEsatta(Domanda d) {
		// TODO Auto-generated method stub

	}

	@Override
	public void rispostaErrata(Domanda d) {
		// TODO Auto-generated method stub

	}
	private TimerListener listener;
	private TimerListener getTimerListener(){
		if(listener == null){
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
				public void onTimerInterrupt() {}
				
				@Override
				public void onTimerEnd() {
					runOnUiThread(new Runnable() {
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
		return DURATA_DOMANDA - timer.getCurrentTimeF();
	}
}