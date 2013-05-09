package ru.slavabulgakov.busesspb;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.RelativeLayout;
import android.widget.Scroller;

public class RootView extends RelativeLayout {
	
	interface OnActionListener {
		void onMenuChangeState(boolean isOpen);
		void onHold(Boolean hold);
	}
	private OnActionListener _listener;
	private Boolean _hold = false;
	private float _prevX = 0;
	private float _lastDX = 0;
	private final int _shadowWidth = 0;
	private final int _menuWidth = 250;
	private final int _touchWidth = 30;
	private final int _xClose = -_shadowWidth;
	private final int _xOpen = _menuWidth - _shadowWidth;
	private Model _model;
	public void setOnOpenListener(OnActionListener listener) {
		_listener = listener;
	}
	
	private Scroller _scroller;
	@SuppressLint("NewApi")
	private void _load(Context context, AttributeSet attrs) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.root_view, this, true);
		_scroller = new Scroller(getContext(), new DecelerateInterpolator(3));
	}
	
	@SuppressLint("NewApi")
	public void setModel(Model model) {
		_model = model;
		if (_model.menuIsOpened()) {
			_setX(_dpToPx(_xOpen));
		} else {
			_setX(_dpToPx(_xClose));
		}
	}
	
	@SuppressLint("Override")
	private void _setX(float x) {
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)getLayoutParams();
		lp.leftMargin = (int)x;
		lp.rightMargin = -(int)x - _shadowWidth;
		setLayoutParams(lp);
	}
	
	@SuppressLint("Override")
	private int _getX() {
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)getLayoutParams();
		return lp.leftMargin;
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
	
	@SuppressLint("NewApi")
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (ev.getRawX() - _getX() <= _dpToPx(_touchWidth)) {
				_setHolded(true);
			} else if (ev.getRawX() - _getX() > _dpToPx(_touchWidth) && _model.menuIsOpened()) {
				_setHolded(true);
			}
			_prevX = ev.getRawX();
			break;
			
		case MotionEvent.ACTION_MOVE:
			float dX = ev.getRawX() - _prevX;
			if (_hold) {
				if (_getX() + dX > _dpToPx(_xOpen)) {
					_setX(_dpToPx(_xOpen));
				} else if (_getX() + dX < 0) {
					_setX(_dpToPx(_xClose));
				} else {
					_setX(_getX() + dX);
					_lastDX = dX;
					_prevX = ev.getRawX();
				}
			}
			break;
			
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (_getX() <= _dpToPx(_shadowWidth)) {
				_setOpened(false);
				_setX(_dpToPx(_xClose));
			} else if (_getX() > _dpToPx(_xOpen) && _model.menuIsOpened()) {
				_animateMove(-1);
			} else if (_getX() > _dpToPx(_xOpen)) {
				_setOpened(true);
				_setX(_dpToPx(_xOpen));
			} else {
				_animateMove(0);
			}
			if (_hold) {
				_setHolded(false);
			}
			break;

		default:
			break;
		}
		return super.onInterceptTouchEvent(ev); 
	}
	
	
	public void _setHolded(Boolean holded) {
		_hold = holded;
		_listener.onHold(holded);
	}
	
	public void open() {
		if (!_model.menuIsOpened()) {
			_animateMove(1);
		}
	}
	
	public void close() {
		if (_model.menuIsOpened()) {
			_animateMove(-1);
		}
	}
	
	public void toggle() {
		if (_model.menuIsOpened()) {
			_animateMove(-1);
		} else {
			_animateMove(1);
		}
	}
	
	private int _dpToPx(int dp) {
		return (int)(getResources().getDisplayMetrics().density * dp);
	}

	private void _setOpened(Boolean opened) {
		if (_listener != null && opened != _model.menuIsOpened()) {
			_model.setMenuOpened(opened);
			_listener.onMenuChangeState(_model.menuIsOpened());
		}
	}

	@SuppressLint("NewApi")
	private void _animateMove(int direction) {
		if (direction > 0) {
			_scroller.startScroll((int)_getX(), 0, _dpToPx(_xOpen) - (int)_getX(), 0);
		} else if (direction < 0) {
			_scroller.startScroll((int)_getX(), 0, -_shadowWidth - (int)_getX(), 0);
		} else if (_lastDX > 0) {
			_scroller.startScroll((int)_getX(), 0, _dpToPx(_xOpen) - (int)_getX(), 0);
		} else if (_lastDX < 0) {
			_scroller.startScroll((int)_getX(), 0, -_shadowWidth - (int)_getX(), 0);
		}
		
		post(new Runnable() {
			
			@SuppressLint("NewApi")
			@Override
			public void run() {
				if (_scroller.isFinished()) {
					_scroller.forceFinished(true);
					if ((int)_getX() < 100) {
						_setOpened(false);
					} else {
						_setOpened(true);
					}
					return;
				}
				Boolean more = _scroller.computeScrollOffset();
				int currentX = _scroller.getCurrX();
				if (currentX > _dpToPx(_xOpen)) {
					_setX(_dpToPx(_xOpen));
				} else if (currentX < 0) {
					_setX(_dpToPx(_xClose));
				} else {
					_setX(currentX);
				}
				
				if (more) {
					post(this);
				}
			}
		});
	}
	
	public void animateOpen(final int delta) {
		final Scroller scroller = new Scroller(getContext(), new OvershootInterpolator());
		scroller.startScroll(0, 0, delta, 0, 500);
		post(new Runnable() {
			
			@Override
			public void run() {
				if (scroller.isFinished()) {
					scroller.forceFinished(true);
					_animateClose(delta);
					return;
				}
				boolean more = scroller.computeScrollOffset();
				int currentX = scroller.getCurrX();
				_setX(currentX);
				if (more) {
					postDelayed(this, 16);
				}
			}
		});
	}
	
	private void _animateClose(int delta) {
		final Scroller scroller = new Scroller(getContext(), new BounceInterpolator());
		scroller.startScroll(delta, 0, -delta, 0, 1000);
		post(new Runnable() {
			
			@Override
			public void run() {
				if (scroller.isFinished()) {
					scroller.forceFinished(true);
					return;
				}
				boolean more = scroller.computeScrollOffset();
				int currentX = scroller.getCurrX();
				_setX(currentX);
				if (more) {
					postDelayed(this, 16);
				}
			}
		});
	}
}