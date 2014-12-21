package it.pinoelefante.mathematicously.database;

import it.pinoelefante.mathematicously.generator.Statistica;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBAdapter {

	private Context		context;
	private SQLiteDatabase database;
	private DatabaseHelper dbHelper;
	private boolean		open = false;

	public DBAdapter(Context c) {
		context = c;
	}

	public DBAdapter open() throws SQLException {
		if (!open) {
			dbHelper = new DatabaseHelper(context);
			database = dbHelper.getWritableDatabase();
		}
		open = true;
		return this;
	}

	public void close() {
		dbHelper.close();
		open = false;
	}

	private ContentValues cv_StatisticaSingola(String tipo_partita, int diff, int esatte, int domande, String data) {
		ContentValues values = new ContentValues();
		values.put("tipo_partita", tipo_partita);
		values.put("difficolta", diff);
		values.put("esatte", esatte);
		values.put("domande", domande);
		values.put("data", data);
		return values;
	}
	private ContentValues cv_StatisticaAggregata(Statistica s) {
		ContentValues values = new ContentValues();
		values.put("tipo_partita", s.getTipo());
		values.put("difficolta", s.getDifficolta());
		values.put("risultato", s.getRisultato());
		values.put("data", s.getData());
		return values;
	}

	public void inserisciStatistica(Statistica s) throws SQLException {
		database.insertOrThrow("statistiche", null, cv_StatisticaSingola(s.getTipo(), s.getDifficolta(), s.getEsatte(), s.getTotali(), s.getData()));
	}
	public ArrayList<Statistica> selezionaStatistiche(String tipo, int difficolta, String data) {
		Cursor res = database.query("statistiche", null, "tipo_partita=\""+tipo+"\" AND data=\""+data+"\" AND difficolta="+difficolta, null, null, null, null);
		ArrayList<Statistica> stats = new ArrayList<Statistica>();
		while (res.moveToNext()) {
			int id = res.getInt(res.getColumnIndex("id"));
			String tipo_partita = res.getString(res.getColumnIndex("tipo_partita"));
			String data_partita = res.getString(res.getColumnIndex("data"));
			int diff = res.getInt(res.getColumnIndex("difficolta"));
			int esatte = res.getInt(res.getColumnIndex("esatte"));
			int domande = res.getInt(res.getColumnIndex("domande"));
			Statistica s = new Statistica();
			s.setId(id);
			s.setTipo(tipo_partita);
			s.setData(data_partita);
			s.setDifficolta(diff);
			s.setEsatte(esatte);
			s.setTotali(domande);
			stats.add(s);
		}
		res.close();
		return stats;
	}
	public void rimuoviStatistica(int id){
		database.delete("statistiche", "id="+id, null);
	}
	public void inserisciStatisticaAggregata(Statistica s) throws SQLException{
		database.insertOrThrow("statistiche_generate", null, cv_StatisticaAggregata(s));
	}
	public ArrayList<Statistica> selezionaStatisticaAggregata(String tipo, int difficolta) {
		Cursor res = database.query("statistiche_generate", null, "tipo_partita=\""+tipo+"\" AND difficolta="+difficolta, null, null, null, "data ASC");
		ArrayList<Statistica> stats = new ArrayList<Statistica>();
		while (res.moveToNext()) {
			int id = res.getInt(res.getColumnIndex("id"));
			String tipo_partita = res.getString(res.getColumnIndex("tipo_partita"));
			String data_partita = res.getString(res.getColumnIndex("data"));
			int diff = res.getInt(res.getColumnIndex("difficolta"));
			float risultato = res.getFloat(res.getColumnIndex("risultato"));
			Statistica s = new Statistica();
			s.setId(id);
			s.setTipo(tipo_partita);
			s.setData(data_partita);
			s.setDifficolta(diff);
			s.setRisultato(risultato);
			stats.add(s);
		}
		res.close();
		return stats;
	}
	public ArrayList<MyEntry<MyEntry<String, Integer>, String>> getStatisticheSingoleGenerabiliDisponibili(String data){
		String[] col = {"tipo_partita","difficolta","data"};
		Cursor res = database.query(true, "statistiche", col, "data!=\""+data+"\"", null, null, null, null, null);
		ArrayList<MyEntry<MyEntry<String, Integer>,String>> list = new ArrayList<MyEntry<MyEntry<String,Integer>,String>>();
		while(res.moveToNext()){
			String tipo_p = res.getString(res.getColumnIndex("tipo_partita"));
			String data_p = res.getString(res.getColumnIndex("data"));
			int difficolta = res.getInt(res.getColumnIndex("difficolta"));
			MyEntry<String,Integer> tipo = new MyEntry<String, Integer>(tipo_p, difficolta);
			list.add(new MyEntry<MyEntry<String,Integer>, String>(tipo, data_p));
		}
		return list;
	}
	public ArrayList<MyEntry<MyEntry<String, Integer>, String>> getStatisticheSingoleGenerabiliDisponibiliToday(String data){
		String[] col = {"tipo_partita","difficolta","data"};
		Cursor res = database.query(true, "statistiche", col, "data=\""+data+"\"", null, null, null, null, null);
		ArrayList<MyEntry<MyEntry<String, Integer>,String>> list = new ArrayList<MyEntry<MyEntry<String,Integer>,String>>();
		while(res.moveToNext()){
			String tipo_p = res.getString(res.getColumnIndex("tipo_partita"));
			String data_p = res.getString(res.getColumnIndex("data"));
			int difficolta = res.getInt(res.getColumnIndex("difficolta"));
			MyEntry<String,Integer> tipo = new MyEntry<String, Integer>(tipo_p, difficolta);
			list.add(new MyEntry<MyEntry<String,Integer>, String>(tipo, data_p));
		}
		return list;
	}
	public ArrayList<MyEntry<String, Integer>> getStatisticheGenerateDisponibili(){
		String[] col = {"tipo_partita","difficolta"};
		Cursor res = database.query(true, "statistiche_generate", col, null, null, null, null, "tipo_partita,difficolta ASC", null);
		ArrayList<MyEntry<String, Integer>> list = new ArrayList<MyEntry<String,Integer>>();
		while(res.moveToNext()){
			String tipo_p = res.getString(res.getColumnIndex("tipo_partita"));
			Integer diff = res.getInt(res.getColumnIndex("difficolta"));
			MyEntry<String, Integer> e = new MyEntry<String, Integer>(tipo_p, diff);
			list.add(e);
		}
		return list;
	}
	
	public void executeUpdate(String query) {
		database.execSQL(query);
	}
}
