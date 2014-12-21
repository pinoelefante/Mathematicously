package it.pinoelefante.mathematicously.generator;

import java.io.Serializable;

public class Domanda implements Serializable{
	private static final long serialVersionUID = -8652518341434353515L;
	
	private String  domanda;
	private int	 risposta_esatta;
	private int	 risposta_utente;
	private int	 risposta1, risposta2, risposta3;
	private boolean risposta_data;
	private float   tempo_risposta;
	private int	 difficolta;

	private int	 op1, op2;
	private String  operazione;

	public Domanda(String domanda, int risposta, int difficolta) {
		this.domanda = domanda;
		this.risposta_esatta = risposta;
		this.difficolta = difficolta;
		setRisposteErrate(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
	}

	public String getDomanda() {
		return domanda;
	}

	public boolean isRispostaData() {
		return risposta_data;
	}

	public boolean isRispostaEsatta() {
		return (isRispostaData() && risposta_esatta == risposta_utente);
	}

	public int getRisposta(int r) {
		switch (r) {
			case 0:
				return risposta_esatta;
			case 1:
				return risposta1;
			case 2:
				return risposta2;
			case 3:
				return risposta3;
		}
		return 0;
	}

	public void setRispostaUtente(int r) {
		if (!isRispostaData()) {
			risposta_utente = r;
			risposta_data = true;
		}
	}

	public void setRispostaUtente(int r, float tempo) {
		if (!isRispostaData()) {
			risposta_utente = r;
			tempo_risposta = tempo;
			risposta_data = true;
		}
	}

	public float getTempoRisposta() {
		return tempo_risposta;
	}

	public int getDifficolta() {
		return difficolta;
	}

	public void setRisposteErrate(int r1, int r2, int r3) {
		risposta1 = r1;
		risposta2 = r2;
		risposta3 = r3;
	}

	public void setRispostaErrata(int r, int v) {
		switch (r) {
			case 0:
				risposta1 = v;
				break;
			case 1:
				risposta2 = v;
				break;
			case 2:
				risposta3 = v;
				break;
		}
	}

	public int getOp1() {
		return op1;
	}

	public void setOp1(int o) {
		op1 = o;
	}

	public void setOp2(int o) {
		op2 = o;
	}

	public void setOperazione(String o) {
		operazione = o;
	}

	public int getOp2() {
		return op2;
	}

	public String getOperatore() {
		return operazione;
	}

	public void setDifficolta(int d) {
		difficolta = d;
	}

	public void setDomanda(String d) {
		domanda = d;
	}

	public void setRispostaEsatta(int t) {
		risposta_esatta = t;
	}
}
