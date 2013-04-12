package ru.slavabulgakov.busesspb;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Configuration;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;

public class BaseActivity extends FragmentActivity {
	protected ProgressDialog _progressDialog;
	protected Model _model;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_model = (Model)getApplicationContext();
	}
	
	
	
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
		Contr.getInstance().setActivity(this);
	}



	@Override
	protected void onStop() {
		super.onStop();
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



	public void update() {
		// TODO Auto-generated method stub
		
	}
	
}
