package ru.slavabulgakov.busesspb;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends BaseActivity implements OnClickListener {
	
	private ShareFragment _shareFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		TextView appVersion = (TextView)findViewById(R.id.app_version);
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			appVersion.setText(appVersion.getText() + pInfo.versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		((ImageButton)findViewById(R.id.back_btn)).setOnClickListener(Contr.getInstance());
		((Button)findViewById(R.id.aboutRateBtn)).setOnClickListener(this);
		((Button)findViewById(R.id.aboutSendBtn)).setOnClickListener(this);
		_shareFragment = (ShareFragment)getSupportFragmentManager().findFragmentById(R.id.shareFragment);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.aboutSendBtn:
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{ getString(R.string.author_email) });
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " for Android " + Build.VERSION.RELEASE + " feedback");
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "\n\n" + "Info: " + Build.BRAND + " " + Build.MODEL);
			startActivity(Intent.createChooser(emailIntent, null));
			break;
			
		case R.id.aboutRateBtn:
			Intent intent = new Intent(Intent.ACTION_VIEW);
			String id = "ru.slavabulgakov.busesspb";
		    intent.setData(Uri.parse("market://details?id=" + id));
		    if (_startActivity(intent) == false) {
		        intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + id));
		        if (_startActivity(intent) == false) {
		            Toast.makeText(this, getString(R.string.market_deny), Toast.LENGTH_LONG).show();
		        }
		    }
			break;

		default:
			break;
		}
		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		_shareFragment.onActivityResult_(requestCode, resultCode, data);
	}
	
	private boolean _startActivity(Intent aIntent) {
	    try
	    {
	        startActivity(aIntent);
	        return true;
	    }
	    catch (ActivityNotFoundException e)
	    {
	        return false;
	    }
	}
}
