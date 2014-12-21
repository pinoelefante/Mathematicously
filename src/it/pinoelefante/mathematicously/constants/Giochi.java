package it.pinoelefante.mathematicously.constants;

import it.pinoelefante.mathematicously.R;

public class Giochi {
	public static final int RIFLESSI = 0, SCRIVI_LA_RISPOSTA = 1,
			SFIDA_CONTRO_IL_TEMPO = 2, QUIZ_SHOW = 3, ENDURANCE = 4,
			MEMORY = 5, TRUE_FALSE = 6;

	public static final String TIPO_RIFLESSI = "riflessi",
			TIPO_SCRIVI_LA_RISPOSTA = "scrivi_risposta",
			TIPO_SFIDA_CONTRO_IL_TEMPO = "sfida_tempo",
			TIPO_QUIZ_SHOW = "quiz_show", TIPO_ENDURANCE = "endurance",
			TIPO_MEMORY = "memory", TIPO_TRUE_FALSE = "true_false";

	public static final String TIPO_RIFLESSI_MULTI = "riflessi_multi",
			TIPO_SCRIVI_LA_RISPOSTA_MULTI = "scrivi_risposta_multi",
			TIPO_SFIDA_CONTRO_IL_TEMPO_MULTI = "sfida_tempo_multi",
			TIPO_QUIZ_SHOW_MULTI = "quiz_show_multi",
			TIPO_ENDURANCE_MULTI = "endurance_multi",
			TIPO_MEMORY_MULTI = "memory_multi",
			TIPO_TRUE_FALSE_MULTI = "true_false_multi";
	
	public static int getStringResourceFromTipo(String tipo){
		switch (tipo) {
			case TIPO_ENDURANCE:
			case TIPO_ENDURANCE_MULTI:
				return R.string.game_endurance;
			case TIPO_MEMORY:
			case TIPO_MEMORY_MULTI:
				return R.string.game_memory;
			case TIPO_QUIZ_SHOW:
			case TIPO_QUIZ_SHOW_MULTI:
				return R.string.game_quiz_show;
			case TIPO_RIFLESSI:
			case TIPO_RIFLESSI_MULTI:
				return R.string.game_riflessi;
			case TIPO_SCRIVI_LA_RISPOSTA:
			case TIPO_SCRIVI_LA_RISPOSTA_MULTI:
				return R.string.game_scrivi_la_risposta;
			case TIPO_SFIDA_CONTRO_IL_TEMPO:
			case TIPO_SFIDA_CONTRO_IL_TEMPO_MULTI:
				return R.string.game_sfida_contro_il_tempo;
			case TIPO_TRUE_FALSE:
			case TIPO_TRUE_FALSE_MULTI:
				return R.string.game_truefalse;
		}
		return 0;
	}
}
