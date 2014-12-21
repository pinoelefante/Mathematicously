package it.pinoelefante.mathematicously.database;

/**
 * Utiizzo di questa classe poiché non è disponibile AbstractMap.Entry nella API level 8
 */

public class MyEntry<K, V> {
	private K k;
	private V v;
	public MyEntry(K k, V v){
		setKey(k);
		setValue(v);
	}
	public K getKey() {
		return k;
	}
	public void setKey(K k) {
		this.k = k;
	}
	public V getValue() {
		return v;
	}
	public void setValue(V v) {
		this.v = v;
	}
}
