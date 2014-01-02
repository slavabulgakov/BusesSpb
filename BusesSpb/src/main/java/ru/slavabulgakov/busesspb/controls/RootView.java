package ru.slavabulgakov.busesspb.controls;

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

import ru.slavabulgakov.busesspb.R;
import ru.slavabulgakov.busesspb.model.Model;
import ru.slavabulgakov.busesspb.model.Model.MenuKind;

public class RootView extends RelativeLayout {
	
	public interface OnActionListener {
		void onMenuChangeState(boolean isOpen, MenuKind kind);
		void onHold(Boolean hold);
		void onMove(double percent);
	}
	private OnActionListener _listener;
	private Boolean _leftHold = false;
	private Boolean _rightHold = false;
	private float _prevX = 0;
	private float _lastDX = 0;
	private final int _menuWidth = 250;
	private final int _touchWidth = 30;
	private final int _xClose = 0;
	private final int _xLeftOpen = _menuWidth;
	private final int _xRightOpen = -_menuWidth;
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
		if (_model.menuIsOpened(MenuKind.Left)) {
			_setX(_model.dpToPx(_xLeftOpen));
		} else if (_model.menuIsOpened(MenuKind.Right)) {
			_setX(_model.dpToPx(_xRightOpen));
		} else {
			_setX(_model.dpToPx(_xClose));
		}
	}
	
	@SuppressLint("Override")
	private void _setX(float x) {
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)getLayoutParams();
		lp.leftMargin = (int)x;
		lp.rightMargin = -(int)x;
		setLayoutParams(lp);
        _listener.onMove((double)_getX() / (double)_model.dpToPx(_menuWidth));
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
			if (ev.getRawX() - _getX() <= _model.dpToPx(_touchWidth)) {
				_setLeftHolded(true);
			} else if (ev.getRawX() - _getX() > _model.dpToPx(_touchWidth) && _model.menuIsOpened(MenuKind.Left)) {
				_setLeftHolded(true);
			} else if (ev.getRawX() - _getX() > getWidth() - _model.dpToPx(_touchWidth)) {
				_setRightHolded(true);
			} else if ((ev.getRawX() - _getX() < getWidth() - _model.dpToPx(_touchWidth)) && _model.menuIsOpened(MenuKind.Right)) {
				_setRightHolded(true);
			}
			_prevX = ev.getRawX();
			_hasTouches = true;
			break;
			
		case MotionEvent.ACTION_MOVE:
			float dX = ev.getRawX() - _prevX;
			if (_leftHold) {
				if (_getX() + dX > _model.dpToPx(_xLeftOpen)) {
					_setX(_model.dpToPx(_xLeftOpen));
				} else if (_getX() + dX < 0) {
					_setX(_model.dpToPx(_xClose));
				} else {
					_setX(_getX() + dX);
					_lastDX = dX;
					_prevX = ev.getRawX();
				}
			} else if (_rightHold) {
				if (_getX() + dX < _model.dpToPx(_xRightOpen)) {
					_setX(_model.dpToPx(_xRightOpen));
				} else if (_getX() + dX > 0) {
					_setX(_model.dpToPx(_xClose));
				} else {
					_setX(_getX() + dX);
					_lastDX = dX;
					_prevX = ev.getRawX();
				}
			}
			break;
			
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (_leftHold) {
				if (_getX() <= _model.dpToPx(0)) {
					_setOpened(false, MenuKind.Left);
					_setX(_model.dpToPx(_xClose));
				} else if (_getX() > _model.dpToPx(_xLeftOpen) && _model.menuIsOpened(MenuKind.Left)) {
					_animateMoveLeftMenu(MenuAnimates.CloseMenu);
				} else if (_getX() > _model.dpToPx(_xLeftOpen)) {
					_setOpened(true, MenuKind.Left);
					_setX(_model.dpToPx(_xLeftOpen));
				} else if (_getX() > _model.dpToPx(5) && _lastDX > 0) {
					_animateMoveLeftMenu(MenuAnimates.OpenMenu);
				} else if (_getX() < _model.dpToPx(_xLeftOpen - 5)) {
					_animateMoveLeftMenu(MenuAnimates.CloseMenu);
				}
				_setLeftHolded(false);
			} else if (_rightHold) {
				if (_getX() >= _model.dpToPx(0)) {
					_setOpened(false, MenuKind.Right);
					_setX(_model.dpToPx(_xClose));
				} else if (_getX() < _model.dpToPx(_xRightOpen) && _model.menuIsOpened(MenuKind.Right)) {
					_animateMoveRightMenu(MenuAnimates.CloseMenu);
				} else if (_getX() < _model.dpToPx(_xRightOpen)) {
					_setOpened(true, MenuKind.Right);
					_setX(_model.dpToPx(_xRightOpen));
				} else if (_getX() < _model.dpToPx(-5) && _lastDX < 0) {
					_animateMoveRightMenu(MenuAnimates.OpenMenu);
				} else if (_getX() > _model.dpToPx(_xRightOpen + 5)) {
					_animateMoveRightMenu(MenuAnimates.CloseMenu);
				}
				_setRightHolded(false);
			}
			_hasTouches = false;
			break;

		default:
			break;
		}
		return super.onInterceptTouchEvent(ev); 
	}
	
	
	private void _setLeftHolded(Boolean holded) {
		_leftHold = holded;
		_listener.onHold(holded);
	}
	
	private void _setRightHolded(Boolean holded) {
		_rightHold = holded;
		_listener.onHold(holded);
	}
	
	private boolean _hasTouches = false;
	public boolean hasTouches() {
		return _hasTouches;
	}
	
	public void open(MenuKind kind) {
		if (!_model.menuIsOpened(kind)) {
			_animateMove(MenuAnimates.OpenMenu, kind);
		}
	}
	
	public void close(MenuKind kind) {
		if (_model.menuIsOpened(kind)) {
			_animateMove(MenuAnimates.CloseMenu, kind);
		}
	}
	
	public void toggleMenu(MenuKind kind) {
		if (_model.menuIsOpened(kind)) {
			_animateMove(MenuAnimates.CloseMenu, kind);
		} else {
			_animateMove(MenuAnimates.OpenMenu, kind);
		}
	}
	
	private void _setOpened(Boolean opened, MenuKind kind) {
		if (_listener != null && opened != _model.menuIsOpened(kind)) {
			_model.setMenuOpened(kind, opened);
			_listener.onMenuChangeState(_model.menuIsOpened(kind), kind);
		}
	}
	
	enum MenuAnimates {
		OpenMenu,
		CloseMenu
	}
	
	private void _animateMove(MenuAnimates direction, MenuKind kind) {
		if (kind == MenuKind.Left) {
			_animateMoveLeftMenu(direction);
		} else {
			_animateMoveRightMenu(direction);
		}
	}

	@SuppressLint("NewApi")
	private void _animateMoveLeftMenu(MenuAnimates direction) {
		if (direction == MenuAnimates.OpenMenu) {
			_scroller.startScroll((int)_getX(), 0, _model.dpToPx(_xLeftOpen) - (int)_getX(), 0, 500);
		} else if (direction == MenuAnimates.CloseMenu) {
			_scroller.startScroll((int)_getX(), 0, - (int)_getX(), 0, 500);
		}
		
		post(new Runnable() {
			
			@SuppressLint("NewApi")
			@Override
			public void run() {
				if (_scroller.isFinished()) {
					_scroller.forceFinished(true);
					if ((int)_getX() < 100) {
						_setOpened(false, MenuKind.Left);
					} else {
						_setOpened(true, MenuKind.Left);
					}
					return;
				}
				Boolean more = _scroller.computeScrollOffset();
				int currentX = _scroller.getCurrX();
				if (currentX > _model.dpToPx(_xLeftOpen)) {
					_setX(_model.dpToPx(_xLeftOpen));
				} else if (currentX < 0) {
					_setX(_model.dpToPx(_xClose));
				} else {
					_setX(currentX);
				}
				
				if (more) {
					postDelayed(this, 16);
				}
			}
		});
	}
	
	private void _animateMoveRightMenu(MenuAnimates direction) {
		final int notNegativeValuesOffset = 10000;
		if (direction == MenuAnimates.OpenMenu) {
			_scroller.startScroll((int)_getX() + notNegativeValuesOffset, 0, _model.dpToPx(_xRightOpen) - (int)_getX(), 0, 500);
		} else if (direction == MenuAnimates.CloseMenu) {
			_scroller.startScroll((int)_getX() + notNegativeValuesOffset, 0, -(int)_getX(), 0, 500);
		}
		
		post(new Runnable() {
			
			@SuppressLint("NewApi")
			@Override
			public void run() {
				if (_scroller.isFinished()) {
					_scroller.forceFinished(true);
					if ((int)_getX() > -100) {
						_setOpened(false, MenuKind.Right);
					} else {
						_setOpened(true, MenuKind.Right);
					}
					return;
				}
				Boolean more = _scroller.computeScrollOffset();
				int currentX = _scroller.getCurrX() - notNegativeValuesOffset;
				if (currentX < _model.dpToPx(_xRightOpen)) {
					_setX(_model.dpToPx(_xRightOpen));
				} else if (currentX > 0) {
					_setX(_model.dpToPx(_xClose));
				} else {
					_setX(currentX);
				}
				
				if (more) {
					postDelayed(this, 16);
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