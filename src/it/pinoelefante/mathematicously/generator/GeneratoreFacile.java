package it.pinoelefante.mathematicously.generator;

import it.pinoelefante.mathematicously.constants.Difficolta;

import java.util.ArrayList;
import java.util.Random;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class GeneratoreFacile {
	protected static Random rand	 = new Random(System.currentTimeMillis());
	protected static int[]  nonPrimi = { 4, 6, 8, 9, 10, 12, 14, 15, 16, 18, 20, 21, 22, 24, 25, 26, 27, 28, 30, 32, 33, 34, 35, 36, 38, 39, 40, 42, 44, 45, 46, 48, 49, 50, 51, 52, 54, 55, 56, 57, 58, 60, 62, 63, 64, 65, 66, 68, 69, 70, 72, 74, 75, 76, 77, 78, 80, 81, 82, 84, 85, 86, 87, 88, 90, 91, 92, 93, 94, 95, 96, 98, 99, 100 };

	public static Domanda generaDomanda() {
		String operazione = getOperazione();
		Domanda domanda = null;
		String domandaString = null;
		switch (operazione) {
			case "+":
				domandaString = generaAddizione();
				break;
			case "-":
				domandaString = generaSottrazione();
				break;
			case "*":
				domandaString = generaMoltiplicazione();
				break;
			case "/":
				domandaString = generaDivisione();
				break;
		}
		int rispostaEsatta = generaRispostaEsatta(domandaString);
		domandaString = domandaString.replace("*", "x").replace("/", ":");
		domanda = new Domanda(domandaString, rispostaEsatta, Difficolta.FACILE);
		String[] operandi = null;
		switch (operazione) {
			case "+":
				domanda.setOperazione("+");
				operandi = domandaString.split("\\+");
				domanda.setOp1(Integer.parseInt(operandi[0]));
				domanda.setOp2(Integer.parseInt(operandi[1]));
				generaRispostaErrataAddizione(domanda);
				break;
			case "-":
				domanda.setOperazione("-");
				operandi = domandaString.split("\\-");
				domanda.setOp1(Integer.parseInt(operandi[0]));
				domanda.setOp2(Integer.parseInt(operandi[1]));
				generaRispostaErrataSottrazione(domanda);
				break;
			case "*":
				domanda.setOperazione("*");
				operandi = domandaString.split("x");
				domanda.setOp1(Integer.parseInt(operandi[0]));
				domanda.setOp2(Integer.parseInt(operandi[1]));
				generaRispostaErrataMoltiplicazione(domanda);
				break;
			case "/":
				domanda.setOperazione("/");
				operandi = domandaString.split(":");
				domanda.setOp1(Integer.parseInt(operandi[0]));
				domanda.setOp2(Integer.parseInt(operandi[1]));
				generaRispostaErrataDivisione(domanda);
				break;
		}

		return domanda;
	}

	private static String generaMoltiplicazione() {
		// rand.setSeed(System.currentTimeMillis());
		int op1 = rand.nextInt(9) + 2;
		int op2 = rand.nextInt(9) + 2;
		return op1 + "*" + op2;
	}

	private static String generaSottrazione() {
		// rand.setSeed(System.currentTimeMillis());
		int op1 = rand.nextInt(45) + 5;
		int op2 = rand.nextInt(25) + 5;
		return op1 + "-" + op2;
	}

	private static String generaAddizione() {
		// rand.setSeed(System.currentTimeMillis());
		int op1 = rand.nextInt(45) + 5;
		int op2 = rand.nextInt(25) + 5;
		return op1 + "+" + op2;
	}

	protected static String getOperazione() {
		switch (rand.nextInt(4)) {
			case 0:
				return "+";
			case 1:
				return "-";
			case 2:
				return "*";
			case 3:
				return "/";
		}
		return "+";
	}

	private static String generaDivisione() {
		// rand.setSeed(System.currentTimeMillis());
		int op1 = nonPrimi[rand.nextInt(nonPrimi.length)];
		ArrayList<Integer> divisori = getDivisori(op1);
		int op2 = divisori.get(rand.nextInt(divisori.size()));
		return op1 + "/" + op2;
	}

	protected static ArrayList<Integer> getDivisori(int num) {
		ArrayList<Integer> divisori = new ArrayList<Integer>();
		int op2 = num / 2;
		while (op2 > 1) {
			if (num % op2 == 0)
				divisori.add(op2);
			op2--;
		}
		return divisori;
	}

	protected static int generaRispostaEsatta(String domanda) {
		Expression calc = new ExpressionBuilder(domanda).build();
		return (int) calc.evaluate();
	}

	protected static void generaRispostaErrataAddizione(Domanda d) {
		int r1 = 0, r2 = 0, r3 = 0;
		int op1 = d.getOp2();
		int op2 = d.getOp2();
		for (int i = 0; i < 3; i++) {
			switch (i) {
				case 0:
					r1 = generaRispostaEsatta((op1 + 1) + "+" + (op2));
					break;
				case 1:
					r2 = generaRispostaEsatta((op1 + 1) + "+" + (op2 + 1));
					break;
				case 2:
					r3 = generaRispostaEsatta((op1 - 1) + "+" + (op2));
					;
					break;
			}
		}
		d.setRisposteErrate(r1, r2, r3);
	}

	protected static void generaRispostaErrataSottrazione(Domanda d) {
		int r1 = 0, r2 = 0, r3 = 0;
		int op1 = d.getOp1();
		int op2 = d.getOp2();
		for (int i = 0; i < 3; i++) {
			switch (i) {
				case 0:
					r1 = generaRispostaEsatta((op1 + 1) + "-" + (op2));
					break;
				case 1:
					r2 = generaRispostaEsatta((op1 - 1) + "-" + (op2));
					break;
				case 2:
					r3 = generaRispostaEsatta((op1 + 2) + "-" + (op2));
					;
					break;
			}
		}
		d.setRisposteErrate(r1, r2, r3);
	}

	protected static void generaRispostaErrataMoltiplicazione(Domanda d) {
		int r1 = 0, r2 = 0, r3 = 0;
		int minDiv1 = 1, minDiv2 = 1;
		int op1 = d.getOp1();
		int op2 = d.getOp2();
		minDiv1 = minDiv(op1);
		minDiv2 = minDiv(op2);
		if (minDiv1 == 0 && minDiv2 == 0) {
			minDiv1 = 2;
			minDiv2 = 3;
		}
		for (int i = 0, j = 0; i < 3;) {
			switch (i) {
				case 0:
					r1 = generaRispostaEsatta((op1 + minDiv1 + j) + "*" + (op2 + j));
					if (!rispostaInDomanda(d, r1)) {
						d.setRispostaErrata(i, r1);
						i++;
						j = 0;
					}
					else
						j++;
					break;
				case 1:
					r2 = generaRispostaEsatta((op1 + j) + "*" + (op2 + minDiv2 + j));
					if (!rispostaInDomanda(d, r2)) {
						d.setRispostaErrata(i, r2);
						i++;
						j = 0;
					}
					else
						j++;
					break;
				case 2:
					r3 = generaRispostaEsatta((op1 + minDiv1 + j) + "*" + (op2 + minDiv2));
					;
					if (!rispostaInDomanda(d, r3)) {
						d.setRispostaErrata(i, r3);
						i++;
						j = 0;
					}
					else
						j++;
					break;
			}
		}
	}

	protected static int minDiv(int op) {
		if (op % 2 == 0)
			return 2;
		if (op % 3 == 0)
			return 3;
		if (op % 5 == 0)
			return 5;
		if (op % 7 == 0)
			return 7;
		return 0;
	}

	protected static boolean rispostaInDomanda(Domanda d, int r) {
		for (int i = 0; i < 4; i++) {
			if (r == d.getRisposta(i)) {
				return true;
			}
		}
		return false;
	}

	protected static void generaRispostaErrataDivisione(Domanda d) {
		int op1 = d.getOp1();
		Integer op2 = d.getOp2();
		int r1, r2, r3;
		ArrayList<Integer> divisoriOp = getDivisori(op1);
		divisoriOp.remove(op2);
		for (int i = 0, j = 0; i < 3;) {
			int resp = 0;
			switch (i) {
				case 0:
					if (!divisoriOp.isEmpty()) {
						r1 = generaRispostaEsatta(op1 + "/" + divisoriOp.remove(0));
						d.setRispostaErrata(i, r1);
						i++;
						j = 0;
						break;
					}
					resp = (op1 > 50) ? (op1 - j) : (op1 + j);
					if (!isPrimo(resp)) {
						ArrayList<Integer> divisori = getDivisori(resp);
						resp = generaRispostaEsatta((resp) + "/" + divisori.get(rand.nextInt(divisori.size())));
						if (!rispostaInDomanda(d, resp)) {
							d.setRispostaErrata(i, resp);
							i++;
							j = 0;
						}
						else
							j++;
					}
					else
						j++;
					break;
				case 1:
					if (!divisoriOp.isEmpty()) {
						r2 = generaRispostaEsatta(op1 + "/" + divisoriOp.remove(0));
						d.setRispostaErrata(i, r2);
						i++;
						j = 0;
						break;
					}
					resp = (op1 > 50) ? (op1 - j) : (op1 + j);
					if (!isPrimo(resp)) {
						ArrayList<Integer> divisori = getDivisori(resp);
						resp = generaRispostaEsatta((resp) + "/" + divisori.get(rand.nextInt(divisori.size())));
						if (!rispostaInDomanda(d, resp)) {
							d.setRispostaErrata(i, resp);
							i++;
							j = 0;
						}
						else
							j++;
					}
					else
						j++;
					break;
				case 2:
					if (!divisoriOp.isEmpty()) {
						r3 = generaRispostaEsatta(op1 + "/" + divisoriOp.remove(0));
						d.setRispostaErrata(i, r3);
						i++;
						j = 0;
						break;
					}
					resp = (op1 > 50) ? (op1 - j) : (op1 + j);
					if (!isPrimo(resp)) {
						ArrayList<Integer> divisori = getDivisori(resp);
						resp = generaRispostaEsatta((resp) + "/" + divisori.get(rand.nextInt(divisori.size())));
						if (!rispostaInDomanda(d, resp)) {
							d.setRispostaErrata(i, resp);
							i++;
							j = 0;
						}
						else
							j++;
					}
					else
						j++;
					break;

			}
		}
	}

	protected static boolean isPrimo(int n) {
		for (int i = 0; i < nonPrimi.length; i++)
			if (n == nonPrimi[i])
				return false;
		return true;
	}
}
