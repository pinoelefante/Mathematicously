package it.pinoelefante.mathematicously.activities;

import it.pinoelefante.mathematicously.R;
import it.pinoelefante.mycustomviews.PActivity;
import it.pinoelefante.mycustomviews.PButton;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends PActivity {
	private final static String 
			URL_FACEBOOK="https://www.facebook.com/Mathematicously",
			URL_TWITTER="https://twitter.com/Mathematicously",
			URL_AMAZON_STORE="http://www.amazon.com/gp/product/id_prodotto";
			//URL_PLAY_STORE=""; //Hard coded
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		disegna();
	}
	private TextView about_logo_text;
	private PButton facebook, twitter, playstore, amazonapps;
	private void disegna(){
		about_logo_text = (TextView) findViewById(R.id.about_logo_text);
		
		try {
			String nome_app = getString(R.string.app_name);
			String vCode =""+ getPackageManager().getPackageInfo(getPackageName(),0).versionCode;
			String vName =""+ getPackageManager().getPackageInfo(getPackageName(),0).versionName;
			about_logo_text.setText(getString(R.string.about_logo_text, nome_app, vName,vCode));
		}
		catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		facebook = (PButton) findViewById(R.id.facebook_page);
		twitter = (PButton) findViewById(R.id.twitter_page);
		playstore = (PButton) findViewById(R.id.play_store);
		amazonapps = (PButton) findViewById(R.id.amazon_apps);
		
		int dimButtons = 0;
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			dimButtons = calcolaDimensioniW(10);
		}
		else {
			dimButtons = calcolaDimensioniH(10);
		}
		facebook.setSize(dimButtons, dimButtons);
		facebook.setBackgroundResource(R.drawable.style_facebook);
		twitter.setSize(dimButtons, dimButtons);
		twitter.setBackgroundResource(R.drawable.style_twitter);
		playstore.setSize(dimButtons, dimButtons);
		playstore.setBackgroundResource(R.drawable.style_playstore);
		amazonapps.setSize(dimButtons, dimButtons);
		amazonapps.setBackgroundResource(R.drawable.style_amazon);
	}
	public void facebook(View v){
		Intent intent=new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(URL_FACEBOOK));
		startActivity(intent);
	}
	public void twitter(View v){
		Intent intent=new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(URL_TWITTER));
		startActivity(intent);
	}
	public void amazon(View v){
		Intent intent=new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(URL_AMAZON_STORE));
		startActivity(intent);
	}
	public void playstore(View v){
		String appPackageName = getPackageName();
		try {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
		}
		catch (android.content.ActivityNotFoundException anfe) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
		}
	}
}
