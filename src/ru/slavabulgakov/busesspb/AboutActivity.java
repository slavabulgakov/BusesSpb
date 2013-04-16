package ru.slavabulgakov.busesspb;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class AboutActivity extends BaseActivity implements OnClickListener {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		((Button)findViewById(R.id.aboutSendBtn)).setOnClickListener(this);
		TextView appVersion = (TextView)findViewById(R.id.app_version);
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			appVersion.setText(appVersion.getText() + pInfo.versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		((ImageButton)findViewById(R.id.back_btn)).setOnClickListener(Contr.getInstance());
	}

	@Override
	public void onClick(View v) {
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{ getString(R.string.author_email) });
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " for Android " + Build.VERSION.RELEASE + " feedback");
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "\n\n" + "Info: " + Build.BRAND + " " + Build.MODEL);
		startActivity(Intent.createChooser(emailIntent, null));
	}
}
