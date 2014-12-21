package it.pinoelefante.mathematicously.activities.multiplayer;

import it.pinoelefante.mathematicously.R;
import it.pinoelefante.mathematicously.activities.multiplayer.games.EnduranceActivityMulti;
import it.pinoelefante.mathematicously.activities.multiplayer.games.MemoryActivityMulti;
import it.pinoelefante.mathematicously.activities.multiplayer.games.QuizShowActivityMulti;
import it.pinoelefante.mathematicously.activities.multiplayer.games.RiflessiActivityMulti;
import it.pinoelefante.mathematicously.activities.multiplayer.games.ScriviRispostaActivityMulti;
import it.pinoelefante.mathematicously.activities.multiplayer.games.SfidaTempoActivityMulti;
import it.pinoelefante.mathematicously.activities.multiplayer.games.VeroFalsoActivityMulti;
import it.pinoelefante.mathematicously.constants.Difficolta;
import it.pinoelefante.mathematicously.constants.Giochi;
import it.pinoelefante.mathematicously.server.ServerCommunication;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class ScegliDifficoltaMultiActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int tipoGioco = getIntent().getIntExtra("tipoGioco", -1);
		setContentView(R.layout.activity_scegli_difficolta);
		disegna(tipoGioco);
	}
	private void disegna(int tipoPartita) {

		ArrayList<Button> bottoni = new ArrayList<Button>();
		switch (tipoPartita) {
			case Giochi.RIFLESSI:
			case Giochi.SCRIVI_LA_RISPOSTA:
			case Giochi.SFIDA_CONTRO_IL_TEMPO:
			case Giochi.TRUE_FALSE:
			case Giochi.MEMORY:
				bottoni.add(creaBottone(R.string.difficolta_facile, tipoPartita, Difficolta.FACILE));
				bottoni.add(creaBottone(R.string.difficolta_medio, tipoPartita, Difficolta.MEDIO));
				bottoni.add(creaBottone(R.string.difficolta_difficile, tipoPartita, Difficolta.DIFFICILE));
				bottoni.add(creaBottone(R.string.difficolta_casuale, tipoPartita, Difficolta.CASUALE));
				break;
			case Giochi.QUIZ_SHOW: {
				ServerCommunication.getInstance().write("sceltaGioco "+tipoPartita+" "+Difficolta.CASUALE);
				Intent i = new Intent(getApplicationContext(), QuizShowActivityMulti.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.putExtra("server", true);
				startActivity(i);
				break;
			}
			case Giochi.ENDURANCE: {
				ServerCommunication.getInstance().write("sceltaGioco "+tipoPartita+" "+Difficolta.CASUALE);
				Intent i = new Intent(getApplicationContext(), EnduranceActivityMulti.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.putExtra("server", true);
				startActivity(i);
				break;
			}
		}
		LinearLayout lay = (LinearLayout) findViewById(R.id.layout_scegli_difficolta);
		for (Button b : bottoni)
			lay.addView(b);
	}

	private Button creaBottone(int nome, int gioco, int difficolta) {
		Button bottone = new Button(getApplicationContext());
		bottone.setText(nome);
		bottone.setOnClickListener(new DifficoltaPicker(gioco, difficolta));
		return bottone;
	}

	class DifficoltaPicker implements Button.OnClickListener {
		public int difficolta, gioco;

		public DifficoltaPicker(int gioco, int difficolta) {
			this.difficolta = difficolta;
			this.gioco = gioco;
		}

		@Override
		public void onClick(View v) {
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
}
