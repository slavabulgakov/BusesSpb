

package ru.slavabulgakov.busesspb;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class CloseAllTickets extends RelativeLayout implements AnimationListener {
	
	public CloseAllTickets(Context context) {
		super(context);
		setOnClickListener(Contr.getInstance());
		setBackgroundResource(R.drawable.btn_selected_yellow);
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
	
	public void animatedShow() {
		int height = 50;
		Animation animation = new TranslateAnimation(0, 0, height, 0);
		animation.setDuration(400);
		animation.setFillAfter(true);
		startAnimation(animation);
	}
	
	public void animatedRemove() {
		int height = 50;
		Animation animation = new TranslateAnimation(0, 0, 0, height);
		animation.setDuration(400);
		animation.setFillAfter(true);
		animation.setAnimationListener(this);
		startAnimation(animation);
	}

	@Override
	public void onAnimationEnd(Animation arg0) {
		((ViewGroup)getParent()).removeView(this);
	}

	@Override
	public void onAnimationRepeat(Animation arg0) {}

	@Override
	public void onAnimationStart(Animation arg0) {}
}
