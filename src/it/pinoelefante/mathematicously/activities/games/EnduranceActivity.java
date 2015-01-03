package it.pinoelefante.mathematicously.activities.games;

import it.pinoelefante.mathematicously.R;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.TextView;

public class EnduranceActivity extends FourAnswerActivity {
	private int			  livelloUtente;
	private final static int DURATA_PARTITA   = 60;
	private final static int LIVELLO_INIZIALE = 3;
	private TextView		 overlayText;
	private Animation		ani;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tipo_partita = Giochi.TIPO_ENDURANCE;
		timer = new Timer(DURATA_PARTITA);
		timer.addPauseListener(getTimeListener());
		ani = new AlphaAnimation(0.0f, 1.0f);
		ani.setAnimationListener(new AnimationListener() {
			public void onAnimationEnd(Animation animation) {
				overlayText.setText("");
			}

			public void onAnimationStart(Animation animation) {}

			public void onAnimationRepeat(Animation animation) {}
		});
		ani.setDuration(1000L);
		progress.setMax(DURATA_PARTITA * 1000);
		livelloUtente = LIVELLO_INIZIALE;
		overlayText = (TextView) findViewById(R.id.gioco_overlay_text);
		avviaGioco();
	}

	@Override
	public void avviaGioco() {
		progress.setProgress(timer.getCurrentTime());
		prossimaDomanda();
		Domanda d = domande.get(domande.size()-1);
		scriviDomanda(d);
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
		switch (livelloUtente) {
			case 0:
			case 1:
				return GeneratoreFacile.generaDomanda();
			case 2:
			case 3:
			case 4:
				return GeneratoreMedio.generaDomanda();
			case 5:
			case 6:
				return GeneratoreDifficile.generaDomanda();
			case 7:
				switch (rand.nextInt(2)) {
					case 1:
						return GeneratoreMedio.generaDomanda();
					case 2:
						return GeneratoreDifficile.generaDomanda();
				}
		}
		return GeneratoreDifficile.generaDomanda();
	}

	@Override
	public boolean timerIsInPause() {
		return timer.isPaused();
	}

	@Override
	public void rispostaEsatta(Domanda d) {
		if (!timer.isExpired()) {
			if (livelloUtente < 7)
				livelloUtente++;

			switch (d.getDifficolta()) {
				case Difficolta.FACILE:
					timer.aggiungiTempo(3);
					time_added += 3;
					showOverlay("+3 sec. Liv." + livelloUtente);
					break;
				case Difficolta.MEDIO:
					timer.aggiungiTempo(4);
					time_added += 4;
					showOverlay("+4 sec. Liv." + livelloUtente);
					break;
				case Difficolta.DIFFICILE:
					timer.aggiungiTempo(5);
					time_added += 5;
					showOverlay("+5 sec. Liv." + livelloUtente);
					break;
			}
		}
		if (livelloUtente < 7)
			livelloUtente++;
	}

	@Override
	public void rispostaErrata(Domanda d) {
		if (!timer.isExpired()) {
			if (livelloUtente > 0)
				livelloUtente--;
			switch (d.getDifficolta()) {
				case Difficolta.FACILE:
					timer.aggiungiTempo(-5);
					time_added += -5;
					showOverlay("-5 sec. Liv." + livelloUtente);
					break;
				case Difficolta.MEDIO:
					timer.aggiungiTempo(-4);
					time_added += -4;
					showOverlay("-4 sec. Liv." + livelloUtente);
					break;
				case Difficolta.DIFFICILE:
					timer.aggiungiTempo(-3);
					time_added += -3;
					showOverlay("-3 sec. Liv." + livelloUtente);
					break;
			}
		}
	}

	private void showOverlay(String text) {
		overlayText.startAnimation(ani);
		overlayText.setText(text);
	}
	private TimerListener listener;
	private TimerListener getTimeListener(){
		if(listener == null){
			listener = new TimerListener() {
				
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
	
	private int time_added = 0;
	private int time_spent_total = 0;
	@Override
	public float getResponseTime() {
		float time = DURATA_PARTITA + time_added - time_spent_total - timer.getCurrentTimeF();
		time = Math.abs(time);
		time_spent_total += time;
		return time;
	}
}
