package it.pinoelefante.mathematicously.activities;

import it.pinoelefante.mathematicously.R;
import it.pinoelefante.mathematicously.constants.Difficolta;
import it.pinoelefante.mathematicously.constants.Giochi;
import it.pinoelefante.mathematicously.database.DBAdapter;
import it.pinoelefante.mathematicously.database.MyEntry;
import it.pinoelefante.mathematicously.generator.Statistica;
import it.pinoelefante.mycustomviews.PActivity;
import it.pinoelefante.mycustomviews.PButton;
import it.pinoelefante.mycustomviews.PLinearLayout;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;

public class FirstPageActivity extends PActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (getIntent().getBooleanExtra("exit", false)) {
			finish();
			System.exit(0);
		}
	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_first_page);
		disegna();
		
		generaStatistiche();
		getStatistiche();
	}

	private PLinearLayout lay_stats;
	private GraphView	 stat;
	private PButton	   single_player, multiplayer, next_stat, prev_stat;
	private TextView nome_modalita;

	private void disegna() {
		lay_stats = (PLinearLayout) findViewById(R.id.stats_lay);
		single_player = (PButton) findViewById(R.id.btn_single_player);
		multiplayer = (PButton) findViewById(R.id.btn_multiplayer);
		nome_modalita = (TextView) findViewById(R.id.stat_nome_tabella);
		next_stat = (PButton) findViewById(R.id.btn_stat_next);
		prev_stat = (PButton) findViewById(R.id.btn_stat_prev);
		
		int dimButtonPlayer = 0;
		int dimButtonStat = 0;
		int fontSize = 0;
		int h_stats = 0;
		
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			dimButtonPlayer = calcolaDimensioniW(20);
			dimButtonStat = calcolaDimensioniW(10);
			fontSize = calcolaDimensioniH(10);
			h_stats = 80;
		}
		else {
			dimButtonPlayer = calcolaDimensioniH(20);
			dimButtonStat = calcolaDimensioniH(10);
			fontSize = calcolaDimensioniH(3);
			h_stats = 60;
		}
		
		single_player.setSize(dimButtonPlayer, dimButtonPlayer);
		multiplayer.setSize(dimButtonPlayer, dimButtonPlayer);
		next_stat.setSize(dimButtonStat, dimButtonStat);
		prev_stat.setSize(dimButtonStat, dimButtonStat);
		nome_modalita.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
		lay_stats.setSizePerc(100, h_stats);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		disegna();
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			disegna();
		}
		else {
			disegna();
		}
	}

	public void goToNuovaPartita(View v) {
		Intent i = new Intent(getApplicationContext(), ScegliGiocoActivity.class);
		startActivity(i);
	}

	public void goToMultiplayer(View v) {
		/*
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			new AlertDialog.Builder(this).setMessage(R.string.bluetooth_non_disponibile).setCancelable(true).setPositiveButton(R.string.scarica_mathchallenger, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					String appPackageName = getPackageName();
					try {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
					}
					catch (android.content.ActivityNotFoundException anfe) {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
					}
				}
			}).setNegativeButton(R.string.no, null).show();
		}
		else {
			Intent goMulti = new Intent(getApplicationContext(), SfidaAmicoActivity.class);
			startActivity(goMulti);
		}
		*/
		Intent goMulti = new Intent(getApplicationContext(), SfidaAmicoActivity.class);
		startActivity(goMulti);
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this).setMessage(R.string.exit_confirm).setCancelable(false).setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Intent intent = new Intent(getApplicationContext(), FirstPageActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("exit", true);
				startActivity(intent);
			}
		}).setNegativeButton(R.string.no, null).show();
	}
	private ArrayList<MyEntry<String, Integer>> statistiche_disponibili;
	private int current_stat = 0;
	private void getStatistiche(){
		DBAdapter db = new DBAdapter(getApplicationContext()).open();
		statistiche_disponibili = db.getStatisticheGenerateDisponibili();
		for(MyEntry<MyEntry<String,Integer>, String> s: db.getStatisticheSingoleGenerabiliDisponibiliToday(getData())){
			MyEntry<String,Integer> toIns = s.getKey();
			boolean found = false;
			for(int i=0;i<statistiche_disponibili.size();i++){
				MyEntry<String,Integer> ins = statistiche_disponibili.get(i);
				if(ins.getKey().compareTo(toIns.getKey())==0 && ins.getValue().compareTo(toIns.getValue())==0){
					found = true;
					break;
				}
			}
			if(!found){
				statistiche_disponibili.add(toIns);
			}
		}
		db.close();
		if(statistiche_disponibili.isEmpty()){
			nome_modalita.setText(R.string.nessuna_stat);
			if(stat!=null){
				lay_stats.removeAllViews();
				stat = null;
			}
		}
		else {
			if(stat == null)
				initStatView();
			inserisciStatistica(0);
		}
	}
	private void initStatView(){
		if(stat==null){
			stat = new BarGraphView(getApplicationContext(), "");
			stat.setScrollable(true);
			stat.setManualYAxis(true);
			stat.setManualYAxisBounds(100, 0);
			stat.getGraphViewStyle().setHorizontalLabelsColor(Color.BLUE);
			stat.getGraphViewStyle().setVerticalLabelsColor(Color.BLACK);
			stat.setVerticalLabels(new String[]{"100","90","80","70","60","50","40","30","20","10","0"});
			lay_stats.addView(stat);
		}
	}
	private void inserisciStatistica(int index){
		MyEntry<String,Integer> c = statistiche_disponibili.get(index);
		String mod = getNomeGiocoString(c.getKey())+" - "+getDifficoltaString(c.getValue());
		nome_modalita.setText(mod);
		
		DBAdapter db = new DBAdapter(getApplicationContext()).open();
		ArrayList<Statistica> stats = db.selezionaStatisticaAggregata(c.getKey(), c.getValue());
		db.close();
		
		ArrayList<GraphViewData> dati = new ArrayList<GraphViewData>();
		for(int i=0;i<stats.size();i++){
			Statistica s = stats.get(i);
			dati.add(new GraphViewData(i+1, s.getRisultato()*100));
		}
		GraphViewData[] datiArray = new GraphViewData[dati.size()+1];
		int i=0;
		for(;i<dati.size();i++){
			datiArray[i]=dati.get(i);
		}
		float today = getTodayStats(c.getKey(), c.getValue());
		datiArray[i]=new GraphViewData(i, today*100);
		dati.clear();
		
		String[] hStrings = new String[datiArray.length];
		for(i=0;i<datiArray.length;i++){
			hStrings[i]="";
		}
		stat.setHorizontalLabels(hStrings);
		
		stat.removeAllSeries();
		GraphViewSeries dataSeries = new GraphViewSeries(datiArray);
		stat.addSeries(dataSeries);
		
	}
	private float getTodayStats(String key, Integer value) {
		DBAdapter db = new DBAdapter(getApplicationContext()).open();
		ArrayList<Statistica> stats = db.selezionaStatistiche(key, value, getData());
		db.close();
		float val=0;
		for(Statistica s : stats){
			val+=s.getRisultato();
		}
		if(stats.size()>0)
			val = val / stats.size();
		return val;
	}

	public void nextStat(View v){
		if(statistiche_disponibili.isEmpty())
			return;
		if(current_stat+1>=statistiche_disponibili.size())
			current_stat = 0;
		else
			current_stat++;
		inserisciStatistica(current_stat);
	}
	public void prevStat(View v){
		if(statistiche_disponibili.isEmpty())
			return;
		if(current_stat-1<0)
			current_stat = statistiche_disponibili.size()-1;
		else
			current_stat--;
		inserisciStatistica(current_stat);
	}
	private String getDifficoltaString(int difficolta){
		switch(difficolta){
			case Difficolta.FACILE:
				return getString(R.string.difficolta_facile);
			case Difficolta.MEDIO:
				return getString(R.string.difficolta_medio);
			case Difficolta.DIFFICILE:
				return getString(R.string.difficolta_difficile);
			case Difficolta.CASUALE:
				return getString(R.string.difficolta_casuale);
		}
		return getString(R.string.difficolta_facile);
	}
	private String getNomeGiocoString(String tipo){
		switch(tipo){
			case Giochi.TIPO_ENDURANCE:
				return getString(R.string.game_endurance);
			case Giochi.TIPO_ENDURANCE_MULTI:
				return getString(R.string.game_endurance)+" M";
			case Giochi.TIPO_MEMORY:
				return getString(R.string.game_memory);
			case Giochi.TIPO_MEMORY_MULTI:
				return getString(R.string.game_memory)+" M";
			case Giochi.TIPO_QUIZ_SHOW:
				return getString(R.string.game_quiz_show);
			case Giochi.TIPO_QUIZ_SHOW_MULTI:
				return getString(R.string.game_quiz_show)+" M";
			case Giochi.TIPO_RIFLESSI:
				return getString(R.string.game_riflessi);
			case Giochi.TIPO_RIFLESSI_MULTI:
				return getString(R.string.game_riflessi)+" M";
			case Giochi.TIPO_SCRIVI_LA_RISPOSTA:
				return getString(R.string.game_scrivi_la_risposta);
			case Giochi.TIPO_SCRIVI_LA_RISPOSTA_MULTI:
				return getString(R.string.game_scrivi_la_risposta)+" M";
			case Giochi.TIPO_SFIDA_CONTRO_IL_TEMPO:
				return getString(R.string.game_sfida_contro_il_tempo);
			case Giochi.TIPO_SFIDA_CONTRO_IL_TEMPO_MULTI:
				return getString(R.string.game_sfida_contro_il_tempo)+" M";
			case Giochi.TIPO_TRUE_FALSE:
				return getString(R.string.game_truefalse);
			case Giochi.TIPO_TRUE_FALSE_MULTI:
				return getString(R.string.game_truefalse)+" M";
		}
		return "";
	}
	private void generaStatistiche(){
		DBAdapter db = new DBAdapter(getApplicationContext()).open();
		ArrayList<MyEntry<MyEntry<String,Integer>, String>> dati = db.getStatisticheSingoleGenerabiliDisponibili(getData());
		for(MyEntry<MyEntry<String, Integer>, String> d : dati){
			String modalita = d.getKey().getKey();
			int difficolta = d.getKey().getValue();
			String data = d.getValue();
			ArrayList<Statistica> statistiche = db.selezionaStatistiche(modalita, difficolta, data);
			float res = 0f;
			for(Statistica s : statistiche){
				res+=s.getRisultato();
			}
			res = res / statistiche.size();
			Statistica daInserire = new Statistica();
			daInserire.setData(data);
			daInserire.setDifficolta(difficolta);
			daInserire.setRisultato(res);
			daInserire.setTipo(modalita);
			try {
				db.inserisciStatisticaAggregata(daInserire);
				for(Statistica s : statistiche){
					db.rimuoviStatistica(s.getId());
				}
			}
			catch(SQLException e){
				Toast.makeText(getApplicationContext(), "Errore SQL", Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
		}
		db.close();
	}
	private String getData() {
		GregorianCalendar d = new GregorianCalendar();
		int day = d.get(GregorianCalendar.DAY_OF_MONTH);
		int month = d.get(GregorianCalendar.MONTH) + 1;
		int year = d.get(GregorianCalendar.YEAR);
		String data = year + "-" + (month < 10 ? "0" + month : month)+ "-" + (day < 10 ? "0" + day : day);
		return data;
	}
	public void goToInfo(View v){
		Intent i = new Intent(getApplicationContext(), AboutActivity.class);
		startActivity(i);
	}
}
