package ru.slavabulgakov.busesspb;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

public class RootView extends RelativeLayout {
	
	private void _load(Context context, AttributeSet attrs) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.root_view, this, true);
	}

	public RootView(Context context) {
		super(context);
		_load(context, null);
	}

	public RootView(Context context, AttributeSet attrs) {
		super(context, attrs);
		_load(context, attrs);
	}

	public RootView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_load(context, attrs);
	}
	
	private int _dpToPx(int dp) {
		return (int)(getResources().getDisplayMetrics().density * dp);
	}
	
	private Boolean _hold = false;
	private Boolean _opened = false;
	private float _prevX = 0;
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)this.getLayoutParams();
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			System.out.printf("ACTION_DOWN on x:%f, leftMargin:%d, x - m:%f", ev.getRawX(), lp.leftMargin, ev.getRawX() - lp.leftMargin);
			if (ev.getRawX() - lp.leftMargin <= _dpToPx(30)) {
				_hold = true;
			} else if (ev.getRawX() - lp.leftMargin > _dpToPx(30) && _opened) {
				_hold = true;
			}
			_prevX = ev.getRawX();
			break;
			
		case MotionEvent.ACTION_MOVE:
			System.out.println(ev.getRawX());
			float dX = ev.getRawX() - _prevX;
			if (_hold) {
				if (lp.leftMargin + dX > _dpToPx(200) && !_opened) {
					_opened = true;
					lp.leftMargin = _dpToPx(200);
					lp.rightMargin = _dpToPx(-200);
				} else if (lp.leftMargin + dX < 0 && _opened) {
					_opened = false;
					lp.leftMargin = 0;
					lp.rightMargin = 0;
				} else {
					lp.leftMargin += dX;
					lp.rightMargin -= dX;
					_prevX = ev.getRawX();
				}
				setLayoutParams(lp);
			}
			break;
			
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (lp.leftMargin <= _dpToPx(30)) {
				_opened = false;
				lp.leftMargin = 0;
				setLayoutParams(lp);
			} else if (lp.leftMargin > _dpToPx(190)) {
				_opened = false;
				lp.leftMargin = _dpToPx(200);
				setLayoutParams(lp);
			} else {
				_animateMove();
			}
			_hold = false;
			break;

		default:
			break;
		}
		return super.onInterceptTouchEvent(ev); 
	}
	
	private void _animateMove() {
		final RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)this.getLayoutParams();
		int dX = 0;
		if (lp.leftMargin < _dpToPx(100) && _opened) {
			dX = -lp.leftMargin;
		} else if (lp.leftMargin < _dpToPx(100) && !_opened) {
			dX = 200 - lp.leftMargin;
		} else {
			if (_opened) {
				dX = -lp.leftMargin;
			} else {
				dX = 200 - lp.leftMargin;
			}
		}
		lp.leftMargin += dX;
		lp.rightMargin -= dX;
		TranslateAnimation ta = new TranslateAnimation(0, dX, 0, 0);
		ta.setDuration(400);
		ta.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				setLayoutParams(lp);
				if (lp.leftMargin < 100) {
					_opened = false;
				} else {
					_opened = true;
				}
			}
		});
		this.startAnimation(ta);
	}

}
