package it.pinoelefante.mathematicously.generator;

public class Statistica {
	private int id, esatte, totali, difficolta;
	private String tipo, data;
	private float risultato;
	public Statistica() {
		
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getEsatte() {
		return esatte;
	}
	public void setEsatte(int esatte) {
		this.esatte = esatte;
	}
	public int getTotali() {
		return totali;
	}
	public void setTotali(int totali) {
		this.totali = totali;
	}
	public int getDifficolta() {
		return difficolta;
	}
	public void setDifficolta(int difficolta) {
		this.difficolta = difficolta;
	}
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public float getRisultato(){
		if(risultato == 0f){
			if(totali>0)
				return ((float)esatte)/totali;
		}
		return risultato;
	}
	public void setRisultato(float risultato) {
		this.risultato = risultato;
	}
}
