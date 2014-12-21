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

public class MemoryActivity extends FourAnswerActivity {
	protected final static int NUMERO_DOMANDE = 5;
	private final static int DURATA_DOMANDA = 15;
	private final static int DURATA_VISUALIZZAZIONE_RISPOSTE = 3;
	protected Timer			timer;
	private Timer timerHide;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tipo_partita = Giochi.TIPO_MEMORY;
		memoryGame = true;
		cliccabile = false;
		progress.setMax(DURATA_DOMANDA * 1000);
		avviaGioco();
	}

	@Override
	public void avviaGioco() {
		prossimaDomanda();
		scriviDomanda(domande.get(0));
	}

	@Override
	public void prossimaDomanda() {
		if (n_domande < NUMERO_DOMANDE) {
			Domanda d = generaDomanda();
			domande.add(d);
		}
		else {
			if (timer != null)
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

	private ArrayList<Integer> ordine;

	@Override
	public void scriviDomanda(final Domanda d) {
		progress.setProgress(progress.getMax());

		if (timer != null)
			timer.stop();

		timer = new Timer(DURATA_DOMANDA);
		timer.addPauseListener(getTimerListenerPartita());
		
		if(timerHide!=null && !timerHide.isExpired())
			timerHide.stop();
		
		timerHide=new Timer(DURATA_VISUALIZZAZIONE_RISPOSTE);
		timerHide.addPauseListener(getTimerListenerHide());

		ordine = generaOrdine(4);
		domanda.setText(d.getDomanda());
		for (int i = 0; i < ordine.size(); i++) {
			switch (i) {
				case 0:
					if (ordine.get(i) == 0)
						rispostaEsattaMemory = r1;
					r1.setText(d.getRisposta(ordine.get(i)) + "");
					break;
				case 1:
					if (ordine.get(i) == 0)
						rispostaEsattaMemory = r2;
					r2.setText(d.getRisposta(ordine.get(i)) + "");
					break;
				case 2:
					if (ordine.get(i) == 0)
						rispostaEsattaMemory = r3;
					r3.setText(d.getRisposta(ordine.get(i)) + "");
					break;
				case 3:
					if (ordine.get(i) == 0)
						rispostaEsattaMemory = r4;
					r4.setText(d.getRisposta(ordine.get(i)) + "");
					break;
			}
		}
		timerHide.avviaTimer();
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
		}
		return GeneratoreFacile.generaDomanda();
	}

	@Override
	public boolean timerIsInPause() {
		return timer.isPaused();
	}

	@Override
	public void rispostaEsatta(Domanda d) {
		// prossimaDomanda();
	}

	@Override
	public void rispostaErrata(Domanda d) {
		// prossimaDomanda();
	}

	private TimerListener listener_hide, listener_partita;

	private TimerListener getTimerListenerHide() {
		if (listener_hide == null) {
			listener_hide = new TimerListener() {
				public void onTimerSleepEnd() {
					Log.d("timerHide", "sleep");
				}

				public void onTimerInterrupt() {}

				public void onTimerEnd() {
					runOnUiThread(new Runnable() {
						public void run() {
							r1.setText("");
							r2.setText("");
							r3.setText("");
							r4.setText("");
							cliccabile = true;
							if (timer != null)
								timer.avviaTimer();
						}
					});
				}

				public void onScheduledPauseEnd() {}
			};
		}
		return listener_hide;
	}

	private TimerListener getTimerListenerPartita() {
		if (listener_partita == null) {
			listener_partita = new TimerListener() {
				public void onTimerSleepEnd() {
					runOnUiThread(new Runnable() {
						public void run() {
							progress.setProgress(timer.getCurrentTime());
						}
					});
				}

				public void onTimerInterrupt() {}

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

				public void onScheduledPauseEnd() {
					runOnUiThread(new Runnable() {
						public void run() {
							resetButtonsColor();
							cliccabile = false;
							scriviDomanda(domande.get(n_domande-1));
						}
					});
				}
			};
		}
		return listener_partita;
	}

	@Override
	public float getResponseTime() {
		return DURATA_DOMANDA - timer.getCurrentTimeF();
	}
}
