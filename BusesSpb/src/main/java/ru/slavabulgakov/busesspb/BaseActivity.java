package ru.slavabulgakov.busesspb;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;

import com.flurry.android.FlurryAgent;

import ru.slavabulgakov.busesspb.controller.Controller;
import ru.slavabulgakov.busesspb.model.Model;
import ru.slavabulgakov.busesspb.util.IabHelper;
import ru.slavabulgakov.busesspb.util.IabHelper.OnIabSetupFinishedListener;
import ru.slavabulgakov.busesspb.util.IabResult;
import ru.slavabulgakov.busesspb.util.Inventory;

public class BaseActivity extends FragmentActivity {
	protected ProgressDialog _progressDialog;
	protected Model _model;
	protected IabHelper _helper;
	protected static String SKU_ADS_OFF = "ads_off";

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_model = (Model)getApplicationContext();
		_model.setListener(Controller.getInstance());
		_model.getModelPaths().setListener(Controller.getInstance());

		String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA26ADkNNKb3IcTfy7HT4ZOoumAZqj2xQ+OHUQuiliVukaq6gwhaLms6vMsDslCgsWhdc3II0c+lViuLcLRUQevPPZ0aVa0zdNbBQrMfbPSqlga1vE66iikcssdonrrL2S+uSqEu7Q8hlLP3Rbu+AV9vLdSUKfmG83EHecgRpyKeu0urhW2IXbVt8W61p5EykVCrcNpN8P2/8RhpOFv7iEMPGQEvX2VX+gLM6+bwH5jlFXcZusfv5sQTq5Kd5gcfk885LH21B3pZxXrhDIlVdryt+Bawk2zbeoANAGR9Gnk8B39uNU5EsMIFVFftElPayo4Gi4yVqVWRCPjYlTzgqMWQIDAQAB";
		_helper = new IabHelper(this, base64EncodedPublicKey);
		
		_helper.startSetup(new OnIabSetupFinishedListener() {
			
			@Override
			public void onIabSetupFinished(IabResult result) {
				if (result.isSuccess()) {
					_helper.queryInventoryAsync(_gotInventoryListener);
				}
			}
		});
	}
	
	
	
	@SuppressLint("NewApi")
	@Override
	public void startActivity(Intent intent, Bundle options) {
		super.startActivity(intent, options);
		overridePendingTransition(R.anim.animation_enter, R.anim.animation_leave);
	}



	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.animation_enter_back, R.anim.animation_leave_back);
	}



	@Override
	protected void onResume() {
		super.onResume();
	}
	
	protected boolean isAdsOff() {
		boolean hasPurchase = (Boolean)_model.getData("hasPurchaseAdsOff", false);
		boolean isFreeDays = _model.isFreeDays();
		boolean off = hasPurchase || isFreeDays;
		return off;
	}

	IabHelper.QueryInventoryFinishedListener _gotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
		
		@Override
		public void onQueryInventoryFinished(IabResult result, Inventory inv) {
			if (result.isSuccess()) {
				boolean hasPurchaseAdsOff = inv.hasPurchase(SKU_ADS_OFF);
				_model.setData("hasPurchaseAdsOff", hasPurchaseAdsOff, true);
				purchaseDidCheck(hasPurchaseAdsOff);
			}
		}
	};
	
	protected void purchaseDidCheck(boolean hasPurchase) {}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		if (_progressDialog != null) {
			if (_progressDialog.isShowing()) {
				hideProgressDialog();
			}
		}
		
	}
    
    

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (_progressDialog != null) {
			if (_progressDialog.isShowing()) {
				hideProgressDialog();
			}
		}
		
	}



	@Override
	protected void onStart() {
		super.onStart();
		Controller.getInstance().setActivity(this);
		FlurryAgent.onStartSession(this, FlurryConstants.API_KEY);
	}



	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}
	
	
	
	public void showAlertDialog(int titleId, int messageId, int iconId) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(messageId)
			.setTitle(titleId)
			.setCancelable(false)
			.setIcon(iconId)
			.setPositiveButton(R.string.ok, null);
		AlertDialog alert = builder.create();
		alert.show();
    }
	
	public void showAlertHtmlDialog(int titleId, int messageId, int iconId) {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle(titleId);
		b.setIcon(iconId);

		WebView wv = new WebView(getBaseContext());
		wv.loadData(getString(messageId), "text/html", "utf-8");
		wv.setBackgroundColor(Color.TRANSPARENT);
		wv.getSettings().setDefaultTextEncodingName("utf-8");
		b.setView(wv);

		b.setNegativeButton(R.string.close, null);	
		b.show();
	}
	
	protected void showProgressDialog() {
		_progressDialog = new ProgressDialog(this);
		_progressDialog.setMessage(getString(R.string.loading));
		_progressDialog.show();
		_progressDialog.setOnCancelListener(new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				Model model = (Model)getApplicationContext();
				model.cancel();
			}
		});
    }
	
	protected void hideProgressDialog() {
		_progressDialog.dismiss();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	      case R.id.item_about:
	    	  startActivity(new Intent(BaseActivity.this, AboutActivity.class));
	    	  return true;
	            
//	      case R.id.item_comment:
//	    	  final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
//              emailIntent.setType("plain/text");
//              emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{ "slavabulgakov@gmail.com" });
//              emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "test_subject");
//              emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "test_message");
//              startActivity(Intent.createChooser(emailIntent, "Send mail..."));
//	    	  return true;
	            
	      default:
	    	  return super.onOptionsItemSelected(item);
	      }
	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (!_helper.handleActivityResult(requestCode, resultCode, data)) {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}


	public void update() {
		// TODO Auto-generated method stub
		
	}

    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    // navigation bar (at the bottom of the screen on a Nexus device)
    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }
	
}
