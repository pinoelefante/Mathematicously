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
import android.util.Log;

public class SfidaTempoActivity extends FourAnswerActivity {
	private final static int DURATA_PARTITA = 60;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tipo_partita = Giochi.TIPO_SFIDA_CONTRO_IL_TEMPO;
		timer = new Timer(DURATA_PARTITA);
		timer.addPauseListener(getTimerListener());
		progress.setMax(DURATA_PARTITA * 1000);
		avviaGioco();
	}

	@Override
	public void avviaGioco() {
		progress.setProgress(timer.getCurrentTime());
		prossimaDomanda();
		scriviDomanda(domande.get(0));
		timer.avviaTimer();
	}

	@Override
	public void prossimaDomanda() {
		Domanda d = generaDomanda();
		domande.add(d);
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
	public void scriviDomanda(Domanda d) {
		ArrayList<Integer> ordine = generaOrdine(4);
		domanda.setText(d.getDomanda());
		r1.setText(d.getRisposta(ordine.get(0)) + "");
		r2.setText(d.getRisposta(ordine.get(1)) + "");
		r3.setText(d.getRisposta(ordine.get(2)) + "");
		r4.setText(d.getRisposta(ordine.get(3)) + "");
		cliccabile = true;
	}

	@Override
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
		return GeneratoreMedio.generaDomanda();
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
					runOnUiThread(new Runnable(){
						public void run() {
							progress.setProgress(timer.getCurrentTime());
						}
					});
				}
				
				@Override
				public void onTimerInterrupt() {
					runOnUiThread(new Runnable() {
						public void run() {
							finePartita();
						}
					});
				}
				
				@Override
				public void onTimerEnd() {
					runOnUiThread(new Runnable() {
						public void run() {
							cliccabile = false;
							finePartita();
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

	private float time_spent_total = 0;
	@Override
	public float getResponseTime() {
		float time = DURATA_PARTITA-time_spent_total-timer.getCurrentTimeF();
		time_spent_total+=time;
		Log.d("time_spent", time+"");
		return time;
	}
}
