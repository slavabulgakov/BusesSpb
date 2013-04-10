package ru.slavabulgakov.busesspb;

import android.os.Bundle;
import android.widget.Button;

public class AboutActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		((Button)findViewById(R.id.aboutSendBtn)).setOnClickListener(Contr.getInstance());
	}

}
