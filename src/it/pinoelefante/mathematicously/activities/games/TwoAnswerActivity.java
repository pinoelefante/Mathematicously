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
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;

public abstract class TwoAnswerActivity extends GameActivity {
	protected boolean true_false_game = false;
	protected boolean cliccabile = true;
	protected Vibrator vibrator;
	protected Timer	timer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_true_false);
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	}
	
	protected PButton domanda, risposta1, risposta2;
	protected ProgressBar progress;
	protected TextView overlay;
	protected void disegna(){
		overlay = (TextView) findViewById(R.id.overlayTF);
		domanda = (PButton) findViewById(R.id.true_false_domanda);
		domanda.setSizePerc(100, 30, PLayoutParams.RELATIVELAYOUT);
		risposta1 = (PButton) findViewById(R.id.risposta_true);
		risposta2 = (PButton) findViewById(R.id.risposta_false);
		if(true_false_game){
			risposta1.setBackgroundResource(R.drawable.style_bottone_true);
			risposta2.setBackgroundResource(R.drawable.style_bottone_false);
		}
		risposta1.setSizePerc(40, 20, PLayoutParams.RELATIVELAYOUT);
		risposta2.setSizePerc(40, 20, PLayoutParams.RELATIVELAYOUT);
		progress = (ProgressBar) findViewById(R.id.progressBar_true_false);
		progress.setBackgroundResource(R.drawable.style_progress_bar);
	}
	public void clickResponse(View v){
		if(!cliccabile || timerIsInPause())
			return;
		
		cliccabile = false;
		pausaTimer();
		PButton b = (PButton)v;
		
		if(true_false_game){
			String textDomanda = domanda.getText().toString();
			Domanda domanda = domande.get(n_domande - 1);
			if(b==risposta1){
				String d = domanda.getDomanda()+" = "+domanda.getRisposta(0);
				if(d.compareTo(textDomanda)==0){
					domanda.setRispostaUtente(domanda.getRisposta(0),getResponseTime());
					rispostaEsatta(domanda);
				}
				else {
					domanda.setRispostaUtente(domanda.getRisposta(1),getResponseTime());
					if(vibrator!=null)
						vibrator.vibrate(100);
					rispostaErrata(domanda);
				}
			}
			else if(b == risposta2){
				String d = domanda.getDomanda()+" = "+domanda.getRisposta(1);
				if(d.compareTo(textDomanda)==0){
					domanda.setRispostaUtente(domanda.getRisposta(0),getResponseTime());
					rispostaEsatta(domanda);
				}
				else {
					domanda.setRispostaUtente(domanda.getRisposta(1),getResponseTime());
					if(vibrator!=null)
						vibrator.vibrate(100);
					rispostaErrata(domanda);
				}
			}
			prossimaDomanda();
		}
		else {
			String response = b.getText().toString();
			if (response.length() > 0 && !timerIsInPause()) {
				
				Integer r = Integer.parseInt(response);
				Domanda domanda = domande.get(n_domande - 1);
				domanda.setRispostaUtente(r,getResponseTime());
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
	}
	protected void resetButtonsColor(){
		if(true_false_game){
			risposta1.setBackgroundResource(R.drawable.style_bottone_true);
			risposta2.setBackgroundResource(R.drawable.style_bottone_false);
		}
		else {
			risposta1.setBackgroundResource(R.drawable.style_bottone_normale);
			risposta2.setBackgroundResource(R.drawable.style_bottone_normale);
		}
	}
	protected void showOverlay(Animation ani, String text) {
		overlay.startAnimation(ani);
		overlay.setText(text);
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
