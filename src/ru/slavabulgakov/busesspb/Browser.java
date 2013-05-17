package ru.slavabulgakov.busesspb;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Browser extends BaseActivity {
	
	private WebView _browser;
	
	class JavaScriptInterface {
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
			}
			
		});
		_browser.loadUrl((String)_model.getData("twitterUrl"));
	}

}
