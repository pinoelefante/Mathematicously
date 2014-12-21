package it.pinoelefante.mathematicously.activities.multiplayer;

import it.pinoelefante.mathematicously.R;
import it.pinoelefante.mathematicously.constants.Giochi;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ScegliGiocoMultiplayerActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scegli_gioco);
	}
	public void scegliGioco(View v) {
		int tipoGioco = -1;
		switch (v.getId()) {
			case R.id.btn_gioco_riflessi:
				tipoGioco = Giochi.RIFLESSI;
				break;
			case R.id.btn_gioco_endurance:
				tipoGioco = Giochi.ENDURANCE;
				break;
			case R.id.btn_gioco_memory:
				tipoGioco = Giochi.MEMORY;
				break;
			case R.id.btn_gioco_scrivi_la_risposta:
				tipoGioco = Giochi.SCRIVI_LA_RISPOSTA;
				break;
			case R.id.btn_gioco_sfida_contro_il_tempo:
				tipoGioco = Giochi.SFIDA_CONTRO_IL_TEMPO;
				break;
			case R.id.btn_gioco_quiz_show:
				tipoGioco = Giochi.QUIZ_SHOW;
				break;
			case R.id.btn_gioco_truefalse:
				tipoGioco = Giochi.TRUE_FALSE;
				break;
		}
		Intent i = new Intent(getApplicationContext(), ScegliDifficoltaMultiActivity.class);
		i.putExtra("tipoGioco", tipoGioco);
		startActivity(i);
	}
}
