package ru.slavabulgakov.busesspb;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class CloseAllTickets extends RelativeLayout {
	
	public CloseAllTickets(Context context) {
		super(context);
		setOnClickListener(Contr.getInstance());
		setBackgroundResource(R.color.yellow);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
		lp.setMargins(0, 0, 5, 0);
		lp.gravity = Gravity.CENTER_VERTICAL;
		setLayoutParams(lp);
		setMinimumWidth(35);
		
		ImageView closeImage = new ImageView(context);
		closeImage.setImageResource(R.drawable.close);
		LayoutParams rlp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
		rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
		closeImage.setLayoutParams(rlp);
		addView(closeImage);
	}
}
