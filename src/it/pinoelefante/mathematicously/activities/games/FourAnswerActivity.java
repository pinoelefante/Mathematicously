package it.pinoelefante.mathematicously.activities.games;

import it.pinoelefante.mathematicously.R;
import it.pinoelefante.mathematicously.generator.Domanda;
import it.pinoelefante.mathematicously.utilities.timer.Timer;
import it.pinoelefante.mycustomviews.PButton;
import it.pinoelefante.mycustomviews.PLayoutParams;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public abstract class FourAnswerActivity extends GameActivity {
	protected boolean cliccabile = true;
	protected boolean memoryGame = false;
	protected View rispostaEsattaMemory;
	protected Vibrator vibrator;
	protected Timer timer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_4_risposte);
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		disegna();
	}

	protected PButton		r1, r2, r3, r4, domanda;
	protected RelativeLayout backgroud_layout;
	protected ProgressBar	progress;

	private void disegna() {
		backgroud_layout = (RelativeLayout) findViewById(R.id.layout_4_risposte);
		progress = (ProgressBar) findViewById(R.id.progressBar1);
		progress.setBackgroundResource(R.drawable.style_progress_bar);
		domanda = (PButton) findViewById(R.id.game_domanda);
		domanda.setSizePerc(100, 30, PLayoutParams.RELATIVELAYOUT);
		r1 = (PButton) findViewById(R.id.risposta_1);
		r1.setSizePerc(40, 20, PLayoutParams.RELATIVELAYOUT);
		r2 = (PButton) findViewById(R.id.risposta_2);
		r2.setSizePerc(40, 20, PLayoutParams.RELATIVELAYOUT);
		r3 = (PButton) findViewById(R.id.risposta_3);
		r3.setSizePerc(40, 20, PLayoutParams.RELATIVELAYOUT);
		r4 = (PButton) findViewById(R.id.risposta_4);
		r4.setSizePerc(40, 20, PLayoutParams.RELATIVELAYOUT);
	}

	public void clickResponse(View v) {
		if (!cliccabile)
			return;
		PButton b = (PButton) v;
		String response = b.getText().toString();
		if (!memoryGame) {
			if (response.length() > 0 && !timerIsInPause()) {
				cliccabile = false;
				pausaTimer();
				Integer r = Integer.parseInt(response);
				Domanda domanda = domande.get(n_domande - 1);
				domanda.setRispostaUtente(r, getResponseTime());
				
				if (domanda.isRispostaEsatta()) {
					b.setBackgroundResource(R.drawable.bottone_verde);
					rispostaEsatta(domanda);
				}
				else {
					b.setBackgroundResource(R.drawable.bottone_rosso);
					if(vibrator!=null)
						vibrator.vibrate(100);
					rispostaErrata(domanda);
				}
				prossimaDomanda();
			}
		}
		else {
			cliccabile = false;
			pausaTimer();
			Domanda domanda = domande.get(n_domande - 1);
			if (v == rispostaEsattaMemory) {
				domanda.setRispostaUtente(domanda.getRisposta(0),getResponseTime());
				b.setBackgroundResource(R.drawable.bottone_verde);
				rispostaEsatta(domanda);
			}
			else {
				domanda.setRispostaUtente(domanda.getRisposta(1),getResponseTime());
				b.setBackgroundResource(R.drawable.bottone_rosso);
				if(vibrator!=null)
					vibrator.vibrate(100);
				rispostaErrata(domanda);
			}
			prossimaDomanda();
		}
	}

	protected void resetButtonsColor() {
		r1.setBackgroundResource(R.drawable.style_bottone_normale);
		r2.setBackgroundResource(R.drawable.style_bottone_normale);
		r3.setBackgroundResource(R.drawable.style_bottone_normale);
		r4.setBackgroundResource(R.drawable.style_bottone_normale);
	}
	@Override
	public void finePartita() {
		super.finePartita();
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
