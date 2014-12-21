package it.pinoelefante.mathematicously.generator;

import it.pinoelefante.mathematicously.constants.Difficolta;

public class GeneratoreDifficile extends GeneratoreFacile {
	public static Domanda generaDomanda() {
		Domanda d1 = GeneratoreFacile.generaDomanda();
		Domanda d2 = GeneratoreFacile.generaDomanda();
		d1.setDifficolta(Difficolta.DIFFICILE);
		d1.setOp1(d1.getRisposta(0));
		d1.setOp2(d2.getRisposta(0));

		String operazione = getOperazione();
		d1.setDomanda("(" + d1.getDomanda() + ")" + operazione + "(" + d2.getDomanda() + ")");
		String dom = d1.getDomanda().replace("x", "*").replace(":", "/");
		int risposta = generaRispostaEsatta(dom);
		d1.setRispostaEsatta(risposta);

		switch (operazione) {
			case "+":
				generaRispostaErrataAddizione(d1);
				break;
			case "-":
				generaRispostaErrataSottrazione(d1);
				break;
		}

		return d1;
	}

	protected static String getOperazione() {
		switch (rand.nextInt(2)) {
			case 0:
				return "+";
			case 1:
				return "-";
		}
		return "+";
	}
}
