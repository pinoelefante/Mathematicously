package it.pinoelefante.mathematicously.server;

import it.pinoelefante.mathematicously.generator.Domanda;

public class Parser {
	public static Domanda parseDomanda(String l){
		String[] v = l.split(" ");
		Domanda d = new Domanda(v[1], Integer.parseInt(v[3]),Integer.parseInt(v[2]));
		d.setRispostaErrata(0, Integer.parseInt(v[4]));
		d.setRispostaErrata(1, Integer.parseInt(v[5]));
		d.setRispostaErrata(2, Integer.parseInt(v[6]));
		return d;
	}
}
