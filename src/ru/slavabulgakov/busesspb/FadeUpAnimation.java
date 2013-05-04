package ru.slavabulgakov.busesspb;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class FadeUpAnimation extends Animation {
	
	int mFromHeight;
	View mView;
	final int _initialHeight;

	public FadeUpAnimation(View view) {
		this.mView = view;
	    this.mFromHeight = view.getHeight();
	    _initialHeight = view.getMeasuredHeight();
	}
	
	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		if (interpolatedTime == 1) {
			mView.setVisibility(View.GONE);
		}
		else {
			mView.getLayoutParams().height = _initialHeight - (int)(_initialHeight * interpolatedTime);
			mView.requestLayout();
		}
	}

	@Override
	public boolean willChangeBounds() {
		return true;
	}

}
