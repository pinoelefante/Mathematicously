package it.pinoelefante.mathematicously.activities.multiplayer;

import it.pinoelefante.mathematicously.R;
import it.pinoelefante.mathematicously.activities.WebActivity;
import it.pinoelefante.mathematicously.constants.Difficolta;
import it.pinoelefante.mathematicously.constants.Giochi;
import it.pinoelefante.mathematicously.database.DBAdapter;
import it.pinoelefante.mathematicously.generator.Statistica;
import it.pinoelefante.mycustomviews.PActivity;
import it.pinoelefante.mycustomviews.PButton;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.GregorianCalendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class FinePartitaMultiActivity extends PActivity {
	private final static String MARKET_URL = "http://goo.gl/b6hrO5";
	private String			  modalita_gioco;
	private int				 totale_domande, risposte_esatte, difficolta;
	private boolean vinto;
	private String nick_avversario, tipo_partita;
	private Statistica stat;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent dati = getIntent();
		if (!dati.hasExtra("difficolta") || 
			!dati.hasExtra("tipo_partita") || 
			!dati.hasExtra("esatte") ||
			!dati.hasExtra("totale") ||
			!dati.hasExtra("stato_partita") || 
			!dati.hasExtra("nome_avversario")) {
			
			Log.d("FinePartitaParams", "Non sono stati passati dei parametri all'intent");
			return;
		}
		difficolta = dati.getIntExtra("difficolta", Difficolta.CASUALE);
		tipo_partita = dati.getStringExtra("tipo_partita");
		modalita_gioco = getString(Giochi.getStringResourceFromTipo(tipo_partita));
		risposte_esatte = dati.getIntExtra("esatte", 0);
		totale_domande = dati.getIntExtra("totale", 0);
		vinto = dati.getBooleanExtra("stato_partita", false);
		nick_avversario = dati.getStringExtra("nome_avversario");
		
		String data = getData();
		Log.d("FinePartita data", data);
		stat = new Statistica();
		stat.setData(data);
		stat.setDifficolta(difficolta);
		stat.setEsatte(risposte_esatte);
		stat.setTipo(tipo_partita);
		stat.setTotali(totale_domande);

		DBAdapter database = new DBAdapter(getApplicationContext()).open();
		database.inserisciStatistica(stat);
		database.close();

		setContentView(R.layout.activity_fine_partita_multi);
		disegna();
		
		if(isOnline()){
			//TODO mostra pubblicità
		}
	}

	private String getData() {
		GregorianCalendar d = new GregorianCalendar();
		int day = d.get(GregorianCalendar.DAY_OF_MONTH);
		int month = d.get(GregorianCalendar.MONTH) + 1;
		int year = d.get(GregorianCalendar.YEAR);
		String data = year + "-" + (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day);
		return data;
	}

	@Override
	public void onBackPressed() {
		Intent i = new Intent(getApplicationContext(), AfterGameMultiplayerLobbyActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
	}

	private TextView  tx_nome_gioco, punteggio, difficolta_tv, vinto_tv;
	private RatingBar stelline;
	private PButton   facebook, twitter, indietro;

	private void disegna() {
		int res = (int) (stat.getRisultato() * 100);
		int dimButtons = calcolaDimensioniH(10);
		int dimIndietro = calcolaDimensioniH(7);
		tx_nome_gioco = (TextView) findViewById(R.id.fine_nomeModalitaGioco);
		tx_nome_gioco.setText(modalita_gioco);
		tx_nome_gioco.setTextSize(TypedValue.COMPLEX_UNIT_PX, calcolaDimensioniH(10));
		punteggio = (TextView) findViewById(R.id.fine_partita_punti);
		punteggio.setText(getString(R.string.fine_partita_punti, res));
		punteggio.setTextSize(TypedValue.COMPLEX_UNIT_PX, calcolaDimensioniH(5));
		difficolta_tv = (TextView) findViewById(R.id.fine_partita_difficolta);
		difficolta_tv.setText(getString(R.string.fine_partita_difficolta, getDifficoltaString()));
		difficolta_tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, calcolaDimensioniH(4));
		stelline = (RatingBar) findViewById(R.id.fine_stelline);
		facebook = (PButton) findViewById(R.id.facebook);
		twitter = (PButton) findViewById(R.id.twitter);
		indietro = (PButton) findViewById(R.id.tornaFirstPage);
		indietro.setSize(dimIndietro, dimIndietro);
		vinto_tv = (TextView) findViewById(R.id.risultato_multi);
		vinto_tv.setText(getString(vinto?R.string.fine_multi_vittoria:R.string.fine_multi_sconfitta));
		vinto_tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, calcolaDimensioniH(6));

		indietro.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				onBackPressed();
			}
		});

		facebook.setSize(dimButtons, dimButtons);
		facebook.setBackgroundResource(R.drawable.style_facebook);
		twitter.setSize(dimButtons, dimButtons);
		twitter.setBackgroundResource(R.drawable.style_twitter);

		int val1Stella = stelline.getMax() / 5;
		if (res < 10) {
			stelline.setProgress(0);
		}
		else if (res < 20) {
			stelline.setProgress(val1Stella * 1);
		}
		else if (res < 40) {
			stelline.setProgress(val1Stella * 2);
		}
		else if (res < 60) {
			stelline.setProgress(val1Stella * 3);
		}
		else if (res < 80) {
			stelline.setProgress(val1Stella * 4);
		}
		else if (res <= 100) {
			stelline.setProgress(val1Stella * 5);
		}
	}

	public void twitta(View v) {
		if(!isOnline()){
			Toast.makeText(getApplicationContext(), R.string.connessione_non_attiva, Toast.LENGTH_SHORT).show();
			return;
		}
		String textTwitter = getString(vinto?R.string.twitter_text_multi_vittoria:R.string.twitter_text_multi_sconfitta, nick_avversario, (int)(stat.getRisultato()*100)) + " " + MARKET_URL + " #mathematicously #" + modalitaGiocoHashtag();
		Intent tweetIntent = new Intent();
		tweetIntent.setType("text/plain");
		tweetIntent.putExtra(Intent.EXTRA_TEXT, textTwitter);
		final PackageManager packageManager = getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);
		boolean found = false;
		for (ResolveInfo resolveInfo : list) {
			String p = resolveInfo.activityInfo.packageName;
			if (p != null && p.startsWith("com.twitter.android")) {
				tweetIntent.setPackage(p);
				found = true;
				startActivity(tweetIntent);
				break;
			}
		}
		if (!found) {
			String text = getString(vinto?R.string.twitter_text_multi_vittoria:R.string.twitter_text_multi_sconfitta, nick_avversario, (int)(stat.getRisultato()*100));
			String url = "https://twitter.com/share?hashtags=mathematicously," + modalitaGiocoHashtag() + "&text=" + text + "&url=" + MARKET_URL;
			Intent i = new Intent(getApplicationContext(), WebActivity.class);
			i.putExtra("url", url);
			startActivity(i);
		}
	}

	@SuppressLint("DefaultLocale")
	private String modalitaGiocoHashtag() {
		String[] s = modalita_gioco.split(" ");
		String res = "";
		for (int i = 0; i < s.length; i++) {
			s[i] = s[i].substring(0, 1).toUpperCase() + (s[i].length() > 1 ? s[i].substring(1).toLowerCase() : "");
			res += s[i];
		}
		return res;
	}

	public void facebookShare(View v) {
		if(!isOnline()){
			Toast.makeText(getApplicationContext(), R.string.connessione_non_attiva, Toast.LENGTH_SHORT).show();
			return;
		}
		String textToShare = getString(vinto?R.string.facebook_text_multi_vittoria:R.string.facebook_text_multi_sconfitta, nick_avversario, getString(Giochi.getStringResourceFromTipo(tipo_partita)), (int)(stat.getRisultato()*100));
		Intent intent = null;
		boolean facebookAppFound = false;

		if (!facebookAppFound) {
			String url_img="";
			try {
				url_img = URLEncoder.encode("http://i.imgur.com/7IYlF5W.png","UTF-8");
			}
			catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			String url = "https://www.facebook.com/dialog/feed?" + "app_id=" + getString(R.string.facebook_app_id) + "&display=popup" + "&caption=Mathematicously" + "&description=" + textToShare +
			(url_img.length()>0?"&picture="+url_img:"")+
			"&link=" + MARKET_URL + "&redirect_uri=https://www.facebook.com";
			intent = new Intent(getApplicationContext(), WebActivity.class);
			intent.putExtra("url", url);
		}
		startActivity(intent);
	}

	private String getDifficoltaString() {
		switch (difficolta) {
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

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();

		if (ni == null)
			return false;

		return ni.isConnected();
	}
}
