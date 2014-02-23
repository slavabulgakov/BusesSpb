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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.ads.AdView;

import ru.slavabulgakov.busesspb.ShareFragment.ShareFragmentListener;
import ru.slavabulgakov.busesspb.controller.Controller;
import ru.slavabulgakov.busesspb.util.IabHelper.OnIabPurchaseFinishedListener;
import ru.slavabulgakov.busesspb.util.IabResult;
import ru.slavabulgakov.busesspb.util.Purchase;

public class AboutActivity extends BaseActivity implements OnClickListener, OnIabPurchaseFinishedListener, ShareFragmentListener {
	
	private ShareFragment _shareFragment;
	private AdView _adView;
	private ScrollView _scrollView;
	private Button _purchaseButton;
	
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
		findViewById(R.id.back_btn).setOnClickListener(Controller.getInstance());
		findViewById(R.id.aboutRateBtn).setOnClickListener(this);
		findViewById(R.id.aboutSendBtn).setOnClickListener(this);
		findViewById(R.id.aboutPurchaseAdsOff).setOnClickListener(this);
		_shareFragment = (ShareFragment)getSupportFragmentManager().findFragmentById(R.id.shareFragment);
		_shareFragment.setListener(this);
		
		_adView = (AdView)findViewById(R.id.aboutAdView);
		_scrollView = (ScrollView)findViewById(R.id.aboutScrollView);
		_purchaseButton = (Button)findViewById(R.id.aboutPurchaseAdsOff);
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
			FlurryAgent.logEvent(FlurryConstants.sendReportBtnPressed);
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
		    FlurryAgent.logEvent(FlurryConstants.rateBtnPressed);
			break;
			
		case R.id.aboutPurchaseAdsOff:
			_helper.launchPurchaseFlow(this, SKU_ADS_OFF, 10001, this);
			FlurryAgent.logEvent(FlurryConstants.purchaseAdsOffBtnPressed);
			break;

		default:
			break;
		}
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		_setAdViewVisible(!isAdsOff());
		_setPurchaseButtonVisible();
	}
	
	@Override
	protected void purchaseDidCheck(boolean hasPurchase) {
		super.purchaseDidCheck(hasPurchase);
		_setAdViewVisible(!isAdsOff());
		_setPurchaseButtonVisible();
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
			_model.setData("hasPurchaseAdsOff", true, true);
			_setAdViewVisible(false);
			_setPurchaseButtonVisible();
			FlurryAgent.logEvent(FlurryConstants.purchaseAdsOffDidSuccess);
		} else if (result.isFailure()) {
			Toast.makeText(this, R.string.purchase_cancel, Toast.LENGTH_LONG).show();
			FlurryAgent.logEvent(FlurryConstants.purchaseAdsOffDidDeny);
		}
	}

	@Override
	public void onSuccessShared() {
		_model.setFreeDays();
		_setAdViewVisible(false);
	}
	
	private void _setAdViewVisible(boolean visible) {
		_adView.setVisibility(visible ? View.VISIBLE : View.GONE);
	}
	
	private void _setPurchaseButtonVisible() {
		boolean visible = !(Boolean)_model.getData("hasPurchaseAdsOff");
		_purchaseButton.setVisibility(visible ? View.VISIBLE : View.GONE);
	}
}
