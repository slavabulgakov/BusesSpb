package ru.slavabulgakov.busesspb;

import com.google.ads.AdView;

import ru.slavabulgakov.busesspb.util.IabHelper.OnIabPurchaseFinishedListener;
import ru.slavabulgakov.busesspb.util.IabResult;
import ru.slavabulgakov.busesspb.util.Purchase;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends BaseActivity implements OnClickListener, OnIabPurchaseFinishedListener {
	
	private ShareFragment _shareFragment;
	private AdView _adView;
	
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
		((Button)findViewById(R.id.aboutPurchaseAdsOff)).setOnClickListener(this);
		_shareFragment = (ShareFragment)getSupportFragmentManager().findFragmentById(R.id.shareFragment);
		
		_adView = (AdView)findViewById(R.id.mainAdView);
		if (_hasPurchaseAdsOff) {
			_adView.setVisibility(View.GONE);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (_helper != null) {
			_helper.dispose();
			_helper = null;
		}
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
			
		case R.id.aboutPurchaseAdsOff:
			_helper.launchPurchaseFlow(this, SKU_ADS_OFF, 10001, this);
			break;

		default:
			break;
		}
		
	}
	
	@Override
    protected void onPurñhaseChecked(boolean hasPurchase) {
    	super.onPurñhaseChecked(hasPurchase);
    	if (_hasPurchaseAdsOff) {
			((ViewGroup)_adView.getParent()).removeView(_adView);
		} else {
			_adView.setVisibility(View.VISIBLE);
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

	@Override
	public void onIabPurchaseFinished(IabResult result, Purchase info) {
		if (result.isSuccess()) {
			_hasPurchaseAdsOff = true;
			_model.setData("hasPurchaseAdsOff", true, true);
		}
	}
}
