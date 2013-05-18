package ru.slavabulgakov.busesspb;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;

public class Browser extends BaseActivity implements OnClickListener {
	
	private WebView _browser;
	private ProgressBar _progressBar;
	private ImageButton _backButton;
	
	class JavaScriptInterface {
		@JavascriptInterface
		public void setHtml(String html) {
			String findString = "<kbd aria-labelledby=\"code-desc\"><code>";
			if (html.contains(findString)) {
				int indexStart = html.indexOf(findString);
				indexStart += findString.length();
				int endIndex = html.indexOf("</code></kbd>");
				String pin = html.substring(indexStart, endIndex);
				_model.setData("twitterPin", pin);
				finish();
			}
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browser);
		_browser = (WebView)findViewById(R.id.browser);
		_browser.getSettings().setJavaScriptEnabled(true);
		_browser.addJavascriptInterface(new JavaScriptInterface(), "HTMLOUT");
		_browser.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				_browser.loadUrl("javascript:window.HTMLOUT.setHtml('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
				_progressBar.setVisibility(View.GONE);
				_model.removeData("twitterBrowserLoading");
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				_progressBar.setVisibility(View.VISIBLE);
				_model.setData("twitterBrowserLoading", true);
			}
			
		});
		_browser.loadUrl((String)_model.getData("twitterUrl"));
		_progressBar = (ProgressBar)findViewById(R.id.browserProgressBar);
		if (_model.getData("twitterBrowserLoading") != null) {
			_progressBar.setVisibility(View.VISIBLE);
		} else {
			_progressBar.setVisibility(View.GONE);
		}
		
		_backButton = (ImageButton)findViewById(R.id.browserBackBtn);
		_backButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.browserBackBtn) {
			finish();
		}
	}

}
