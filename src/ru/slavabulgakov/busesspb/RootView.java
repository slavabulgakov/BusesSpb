package ru.slavabulgakov.busesspb;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;
import android.widget.Scroller;

public class RootView extends RelativeLayout {
	
	private Scroller _scroller;
	private void _load(Context context, AttributeSet attrs) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.root_view, this, true);
		_scroller = new Scroller(getContext(), new Interpolator() {
			
			@Override
			public float getInterpolation(float input) {
				return (float)Math.pow(input - 1, 5) + 1;
			}
		});
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
	private float _lastDX = 0;
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)this.getLayoutParams();
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (ev.getRawX() - lp.leftMargin <= _dpToPx(30)) {
				_hold = true;
			} else if (ev.getRawX() - lp.leftMargin > _dpToPx(30) && _opened) {
				_hold = true;
			}
			_prevX = ev.getRawX();
			break;
			
		case MotionEvent.ACTION_MOVE:
			float dX = ev.getRawX() - _prevX;
			if (_hold) {
				if (lp.leftMargin + dX > _dpToPx(200)) {
					lp.leftMargin = _dpToPx(200);
					lp.rightMargin = _dpToPx(-200);
				} else if (lp.leftMargin + dX < 0) {
					lp.leftMargin = 0;
					lp.rightMargin = 0;
				} else {
					lp.leftMargin += dX;
					lp.rightMargin -= dX;
					_lastDX = dX;
					_prevX = ev.getRawX();
				}
				setLayoutParams(lp);
			}
			break;
			
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (lp.leftMargin <= _dpToPx(10)) {
				_opened = false;
				lp.leftMargin = 0;
				setLayoutParams(lp);
			} else if (lp.leftMargin > _dpToPx(190) && _opened) {
				_animateMove(0);
			}
			else if (lp.leftMargin > _dpToPx(190)) {
				_opened = true;
				lp.leftMargin = _dpToPx(200);
				setLayoutParams(lp);
			} else {
				_animateMove(0);
			}
			_hold = false;
			break;

		default:
			break;
		}
		return super.onInterceptTouchEvent(ev); 
	}
	
	public void open() {
		if (!_opened) {
			_animateMove(1);
		}
	}
	
	public void close() {
		if (_opened) {
			_animateMove(-1);
		}
	}
	
	public void toggle() {
		if (_opened) {
			_animateMove(-1);
		} else {
			_animateMove(1);
		}
	}
	
	public Boolean isOpen() {
		return _opened;
	}
	
	private void _animateMove(int direction) {
		final RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)this.getLayoutParams();
		if (direction > 0) {
			_scroller.startScroll(lp.leftMargin, 0, _dpToPx(200) - lp.leftMargin, 0);
		} else if (direction < 0) {
			_scroller.startScroll(lp.leftMargin, 0, -lp.leftMargin, 0);
		} else if (_lastDX > 0) {
			_scroller.startScroll(lp.leftMargin, 0, _dpToPx(200) - lp.leftMargin, 0);
		} else if (_lastDX < 0) {
			_scroller.startScroll(lp.leftMargin, 0, -lp.leftMargin, 0);
		}
		
		post(new Runnable() {
			
			@Override
			public void run() {
				if (_scroller.isFinished()) {
					if (lp.leftMargin < 100) {
						lp.leftMargin = 0;
						_opened = false;
					} else {
						lp.leftMargin = _dpToPx(200);
						_opened = true;
					}
					setLayoutParams(lp);
					return;
				}
				Boolean more = _scroller.computeScrollOffset();
				int dX = 0;
				int currentX = _scroller.getCurrX();
				dX = currentX - (int)_prevX;
				if (lp.leftMargin + dX > _dpToPx(200)) {
					lp.leftMargin = _dpToPx(200);
					lp.rightMargin = _dpToPx(-200);
				} else if (lp.leftMargin + dX < 0) {
					lp.leftMargin = 0;
					lp.rightMargin = 0;
				} else {
					lp.leftMargin += dX;
					lp.rightMargin -= dX;
					_prevX = currentX;
				}
				setLayoutParams(lp);
				
				if (more) {
					postDelayed(this, 16);
				}
			}
		});
	}
}