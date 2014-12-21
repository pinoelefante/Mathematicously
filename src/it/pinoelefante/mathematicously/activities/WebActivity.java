package it.pinoelefante.mathematicously.activities;

import it.pinoelefante.mathematicously.R;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebActivity extends Activity {
	private String url;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		url = getIntent().getStringExtra("url");
		if(url == null)
			onBackPressed();
		setContentView(R.layout.activity_web);
		disegna();
	}
	private WebView web; 
	private void disegna(){
		web = (WebView) findViewById(R.id.webView1);
		web.setWebViewClient(new WebViewClient(){
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				view.clearHistory();
			}
		});
		web.loadUrl(url);
	}
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		web.destroy();
	}
}
