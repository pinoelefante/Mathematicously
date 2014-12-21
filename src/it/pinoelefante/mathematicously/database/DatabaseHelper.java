package it.pinoelefante.mathematicously.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	private final static String DB_NAME	= "mathematicously.sqlite";
	private final static int	db_version = 1;

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, db_version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String query_statistiche_singole = "CREATE TABLE IF NOT EXISTS statistiche (" +
				"id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"tipo_partita TEXT NOT NULL," + 
				"difficolta INT," +
				"esatte INT,"+
				"domande INT,"+
				"data TEXT NOT NULL"+
				")";
		db.execSQL(query_statistiche_singole);

		String query_statistiche_generate = "CREATE TABLE IF NOT EXISTS statistiche_generate (" +
				"id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"tipo_partita TEXT NOT NULL," + 
				"difficolta INT," +
				"risultato FLOAT,"+
				"data TEXT NOT NULL"+
				")";
		db.execSQL(query_statistiche_generate);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
