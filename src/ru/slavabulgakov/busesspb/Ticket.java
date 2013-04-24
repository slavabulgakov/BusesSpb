package ru.slavabulgakov.busesspb;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Ticket extends LinearLayout implements AnimationListener {
	
	interface OnRemoveListener {
		void onRemove(Ticket ticket);
	}
	
	interface OnAnimationEndListener {
		void onAnimated(Ticket ticket);
	}
	
	LinearLayout _linearLayout;
	ImageView _icon;
	ImageView _closeButton;
	TextView _routeNumber;
	Route _route;
	OnRemoveListener _onRemoveListener;
	LinearLayout _vertLinearLayout;
	
	protected void load(Context context, AttributeSet attrs) {
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Ticket, 0, 0);
		String routeNumber = a.getString(R.styleable.Ticket_routeNumber);
		a.recycle();
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.ticket, this, true);
		
		_linearLayout = (LinearLayout)getChildAt(0);
		
		_vertLinearLayout = (LinearLayout)_linearLayout.getChildAt(0);
		_icon = (ImageView)_vertLinearLayout.getChildAt(0);
		
		_routeNumber = (TextView)_vertLinearLayout.getChildAt(1);
		_routeNumber.setText(routeNumber);
		
		_closeButton = (ImageView)_linearLayout.getChildAt(1);
		_linearLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				_onRemoveListener.onRemove(Ticket.this);
				((ViewGroup)Ticket.this.getParent()).removeView(Ticket.this);
			}
		});
	}
	
	public Ticket(Context context) {
		super(context);
		load(context, null);
	}



	public Ticket(Context context, AttributeSet attrs) {
		super(context, attrs);
		load(context, attrs);
	}
	
	private void _updateBackground() {
		int resId = -1;
		int closeButton = -1;
		int icon = -1;
		switch (_route.kind) {
		case Bus:
			resId = R.color.bus;
			closeButton = R.drawable.ticket_close_btn_bg_bus;
			icon = R.drawable.bus_30_30;
			break;
			
		case Trolley:
			resId = R.color.trolley;
			closeButton = R.drawable.ticket_close_btn_bg_trolley;
			icon = R.drawable.trolley_30_30;
			break;
			
		case Tram:
			resId = R.color.tram;
			closeButton = R.drawable.ticket_close_btn_bg_tram;
			icon = R.drawable.tram_30_30;
			break;

		default:
			break;
		}
		_linearLayout.setBackgroundResource(resId);
		if (_closeButton != null) {
			_closeButton.setBackgroundResource(closeButton);
		}
		_icon.setImageResource(icon);
	}
	
	public void setRoute(Route route) {
		_route = route;
		_routeNumber.setText(_route.routeNumber);
		_updateBackground();
	}
	
	public Route getRoute() {
		return _route;
	}
	
	public void setOnRemoveListener(OnRemoveListener listener) {
		_onRemoveListener = listener;
	}
	
	private boolean _isShowed = false;
	public boolean isShowed() {
		return _isShowed;
	}
	public void animatedShow() {
		_isShowed = true;
		int height = 50;
		Animation animation = new TranslateAnimation(0, 0, height, 0);
		animation.setDuration(400);
		animation.setFillAfter(true);
		startAnimation(animation);
	}
	private OnAnimationEndListener _animationEndListener;
	public void animatedOffset(OnAnimationEndListener listener) {
		_animationEndListener = listener;
		Animation animation = new TranslateAnimation(-getWidth(), 0, 0, 0);
		animation.setDuration(400);
		animation.setFillAfter(true);
		animation.setAnimationListener(this);
		startAnimation(animation);
	}

	@Override
	public void onAnimationEnd(Animation arg0) {
		_animationEndListener.onAnimated(this);
	}

	@Override
	public void onAnimationRepeat(Animation arg0) {}

	@Override
	public void onAnimationStart(Animation arg0) {}
}