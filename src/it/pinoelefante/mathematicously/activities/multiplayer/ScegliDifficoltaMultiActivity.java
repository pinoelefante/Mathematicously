package it.pinoelefante.mathematicously.activities.multiplayer;

import it.pinoelefante.mathematicously.R;
import it.pinoelefante.mathematicously.activities.games.EnduranceActivity;
import it.pinoelefante.mathematicously.activities.games.QuizShowActivity;
import it.pinoelefante.mathematicously.activities.multiplayer.games.MemoryActivityMulti;
import it.pinoelefante.mathematicously.activities.multiplayer.games.RiflessiActivityMulti;
import it.pinoelefante.mathematicously.activities.multiplayer.games.ScriviRispostaActivityMulti;
import it.pinoelefante.mathematicously.activities.multiplayer.games.SfidaTempoActivityMulti;
import it.pinoelefante.mathematicously.activities.multiplayer.games.VeroFalsoActivityMulti;
import it.pinoelefante.mathematicously.constants.Difficolta;
import it.pinoelefante.mathematicously.constants.Giochi;
import it.pinoelefante.mathematicously.server.ServerCommunication;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;

public class ScegliDifficoltaMultiActivity extends Activity {
private int gioco;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		gioco = getIntent().getIntExtra("tipoGioco", -1);
		
		switch(gioco){
			case Giochi.QUIZ_SHOW: {
				ServerCommunication.getInstance().write("sceltaGioco "+gioco+" "+Difficolta.CASUALE);
				Intent i = new Intent(getApplicationContext(), QuizShowActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.putExtra("server", true);
				startActivity(i);
				break;
			}
			case Giochi.ENDURANCE: {
				ServerCommunication.getInstance().write("sceltaGioco "+gioco+" "+Difficolta.CASUALE);
				Intent i = new Intent(getApplicationContext(), EnduranceActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.putExtra("server", true);
				startActivity(i);
				break;
			}
			default:
				setContentView(R.layout.activity_scegli_difficolta);
				disegna();
				break;
		}
	}
	private RatingBar difficolta;
	private TextView difficoltaText;
	private Button btnScegli;
	private void disegna() {
		btnScegli = (Button) findViewById(R.id.btnScegliDifficolta); 
		difficolta = (RatingBar) findViewById(R.id.difficoltaProgress);
		difficoltaText = (TextView) findViewById(R.id.difficoltaText);
		
		difficolta.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
			public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
				int diff = (int)rating;
				switch(diff){
					case 1:
						btnScegli.setEnabled(true);
						difficoltaText.setText(R.string.difficolta_facile);
						break;
					case 2:
						btnScegli.setEnabled(true);
						difficoltaText.setText(R.string.difficolta_medio);
						break;
					case 3:
						btnScegli.setEnabled(true);
						difficoltaText.setText(R.string.difficolta_difficile);
						break;
					case 4:
						btnScegli.setEnabled(true);
						difficoltaText.setText(R.string.difficolta_casuale);
						break;
					case 0:
						btnScegli.setEnabled(false);
						difficoltaText.setText("");
						break;
				}
			}
		});
	}
	
	public void scegliDifficolta(View v){
		int difficolta = (int)this.difficolta.getRating();
		Intent intent = null;
		switch (gioco) {
			case Giochi.RIFLESSI:
				intent = new Intent(getApplicationContext(), RiflessiActivityMulti.class);
				break;
			case Giochi.SCRIVI_LA_RISPOSTA:
				intent = new Intent(getApplicationContext(), ScriviRispostaActivityMulti.class);
				break;
			case Giochi.SFIDA_CONTRO_IL_TEMPO:
				intent = new Intent(getApplicationContext(), SfidaTempoActivityMulti.class);
				break;
			case Giochi.MEMORY:
				intent = new Intent(getApplicationContext(), MemoryActivityMulti.class);
				break;
			case Giochi.TRUE_FALSE:
				intent = new Intent(getApplicationContext(), VeroFalsoActivityMulti.class);
				break;
		}
		if (intent != null) {
			ServerCommunication.getInstance().write("sceltaGioco "+gioco+" "+difficolta);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("difficolta", difficolta);
			intent.putExtra("server", true);
			startActivity(intent);
		}
	}
}
