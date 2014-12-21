package it.pinoelefante.mathematicously.generator;

import it.pinoelefante.mathematicously.constants.Difficolta;

public class GeneratoreMedio extends GeneratoreFacile {
	public static Domanda generaDomanda() {
		Domanda d = GeneratoreFacile.generaDomanda();
		d.setDifficolta(Difficolta.MEDIO);
		String op = getOperazione();
		switch (op) {
			case "+":
				generaAddizione(d);
				generaRispostaErrataAddizione(d);
				break;
			case "-":
				generaSottrazione(d);
				generaRispostaErrataSottrazione(d);
				break;
			case "*":
				generaMoltiplicazione(d);
				generaRispostaErrataMoltiplicazione(d);
				break;
		}
		String dom = d.getDomanda().replace("x", "*").replace(":", "/");
		int risp = generaRispostaEsatta(dom);
		d.setRispostaEsatta(risp);
		return d;
	}

	protected static String getOperazione() {
		switch (rand.nextInt(3)) {
			case 0:
				return "+";
			case 1:
				return "-";
			case 2:
				return "*";
		}
		return "+";
	}

	private static void generaAddizione(Domanda d) {
		d.setOperazione("+");
		d.setOp1(d.getRisposta(0));
		int operando = rand.nextInt(10) + 1;
		d.setOp2(operando);
		if (rand.nextBoolean()) {
			String domanda = "(" + d.getDomanda() + ")+" + operando;
			d.setDomanda(domanda);
		}
		else {
			String domanda = operando + "+(" + d.getDomanda() + ")";
			d.setDomanda(domanda);
		}
	}

	private static void generaSottrazione(Domanda d) {
		d.setOperazione("-");
		d.setOp1(d.getRisposta(0));
		int operando = rand.nextInt(10) + 1;
		d.setOp2(operando);
		if (rand.nextBoolean()) {
			String domanda = "(" + d.getDomanda() + ")-" + operando;
			d.setDomanda(domanda);
		}
		else {
			String domanda = operando + "-(" + d.getDomanda() + ")";
			d.setDomanda(domanda);
		}
	}

	private static void generaMoltiplicazione(Domanda d) {
		d.setOperazione("*");
		d.setOp1(d.getRisposta(0));
		int operando = rand.nextInt(5) + 1;
		d.setOp2(operando);
		if (rand.nextBoolean()) {
			String domanda = "(" + d.getDomanda() + ")x" + operando;
			d.setDomanda(domanda);
		}
		else {
			String domanda = operando + "x(" + d.getDomanda() + ")";
			d.setDomanda(domanda);
		}
	}
}
