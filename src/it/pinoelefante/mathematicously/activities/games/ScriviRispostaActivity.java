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
import it.pinoelefante.mycustomviews.PButton;
import it.pinoelefante.mycustomviews.PLayoutParams;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ScriviRispostaActivity extends GameActivity {
	private PButton			domanda, invio_button;
	private EditText		   risposta;
	private TextView		   overlayText;
	private Animation		  ani;
	protected Timer			  timer;
	private static final int   DURATA_PARTITA = 60;
	private ProgressBar		progress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tipo_partita = Giochi.TIPO_SCRIVI_LA_RISPOSTA;
		ani = new AlphaAnimation(0.0f, 1.0f);
		ani.setAnimationListener(new AnimationListener() {
			public void onAnimationEnd(Animation animation) {
				overlayText.setText("");
			}
			public void onAnimationStart(Animation animation) {}
			public void onAnimationRepeat(Animation animation) {}
		});
		ani.setDuration(1000L);
		timer = new Timer(DURATA_PARTITA);
		timer.addPauseListener(getTimerListener());
		setContentView(R.layout.activity_scrivi_risposta);
		disegna();
		avviaGioco();
	}

	private void disegna() {
		domanda = (PButton) findViewById(R.id.game_domanda);
		domanda.setSizePerc(100, 30, PLayoutParams.LINEARLAYOUT);
		invio_button = (PButton) findViewById(R.id.game_scrivi_risposta_invio);
		overlayText = (TextView) findViewById(R.id.gioco_overlay_text);
		risposta = (EditText) findViewById(R.id.game_scrivi_risposta_risposta);
		risposta.setOnFocusChangeListener(new EditText.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (v == risposta && !hasFocus)
					risposta.requestFocus();
			}
		});
		risposta.setOnKeyListener(new EditText.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				Log.d("keyCode", keyCode+"");
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					click_Enter(invio_button);
				}
				return true;
			}
		});
		progress = (ProgressBar) findViewById(R.id.progressBarSR);
		progress.setBackgroundResource(R.drawable.style_progress_bar);
		progress.setMax(DURATA_PARTITA * 1000);
		progress.setProgress(DURATA_PARTITA * 1000);
	}

	public void click_Enter(View v) {
		if (timer.isExpired())
			return;
		String risp = risposta.getText().toString();
		if (risp.length() > 0) {
			Domanda d = domande.get(domande.size() - 1);
			try {
				Integer r = Integer.parseInt(risp);
				d.setRispostaUtente(r,getResponseTime());
			}
			catch (Exception e) {
				if (d.getRisposta(0) == 0)
					d.setRispostaUtente(-1,getResponseTime());
				else
					d.setRispostaUtente(0,getResponseTime());
			}
			if (d.isRispostaEsatta())
				rispostaEsatta(d);
			else
				rispostaErrata(d);
			pausaTimer();
			prossimaDomanda();
		}
		else {
			Toast.makeText(getApplicationContext(), R.string.errorEmptyString, Toast.LENGTH_SHORT).show();
		}
	}

	public void avviaGioco() {
		progress.setProgress(progress.getMax());
		prossimaDomanda();
		scriviDomanda(domande.get(0));
		timer.avviaTimer();
	}

	public void prossimaDomanda() {
		Domanda d = generaDomanda();
		domande.add(d);
		n_domande++;
		cleanText();
	}
	protected void cleanText(){
		risposta.setText("");
	}

	public void finePartita() {
		if(timer!=null)
			timer.stop();
		Intent i = new Intent(getApplicationContext(), FinePartitaActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.putExtra("difficolta", difficolta);
		i.putExtra("domande", domande);
		i.putExtra("tipo_partita", Giochi.TIPO_SCRIVI_LA_RISPOSTA);
		startActivity(i);
	}

	public void pausaTimer() {
		timer.setPause(500);
	}

	public void scriviDomanda(Domanda d) {
		domanda.setText(d.getDomanda());
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
		showOverlay(getString(R.string.risposta_esatta));
	}

	public void rispostaErrata(Domanda d) {
		showOverlay(getString(R.string.risposta_errata));
	}

	private void showOverlay(String text) {
		overlayText.startAnimation(ani);
		overlayText.setText(text);
	}
	private TimerListener listener;
	
	private TimerListener getTimerListener(){
		if(listener==null){
			listener=new TimerListener() {
				
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
							finePartita();
						}
					});
				}
				
				@Override
				public void onScheduledPauseEnd() {
					runOnUiThread(new Runnable() {
						public void run() {
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
	@Override
	protected void onPause() {
		timer.setPause(true);
		super.onPause();
	}
	@Override
	protected void onResume() {
		timer.setPause(false);
		super.onResume();
	}
}
