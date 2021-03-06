package ru.slavabulgakov.busesspb.controls;

import ru.slavabulgakov.busesspb.Animations;
import ru.slavabulgakov.busesspb.R;
import ru.slavabulgakov.busesspb.model.Route;
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
	
	public interface OnRemoveListener {
		void willRemove(Ticket ticket);
		void didRemove(Ticket ticket);
	}
	
	public interface OnAnimationEndListener {
		void onAnimated(Ticket ticket);
	}
	
	LinearLayout _linearLayout;
	ImageView _icon;
	ImageView _closeButton;
	TextView _routeNumber;
	Route _route;
	OnRemoveListener _onRemoveListener;
	LinearLayout _vertLinearLayout;
	
	protected void load(final Context context, AttributeSet attrs) {
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
				animatedRemove(new OnAnimationEndListener() {
					
					@Override
					public void onAnimated(Ticket ticket) {
						setVisibility(View.GONE);
						((View) getParent()).post(new Runnable() {
				            public void run() {
				            	((ViewGroup)Ticket.this.getParent()).removeView(Ticket.this);
				            }
				        });
						_onRemoveListener.didRemove(ticket);
					}
				});
				_onRemoveListener.willRemove(Ticket.this);
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
			
		case Ship:
			resId = R.color.ship;
			closeButton = R.drawable.ticket_close_btn_bg_ship;
			icon = R.drawable.ship_30_30;
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
	
	public void animatedRemove(OnAnimationEndListener listener) {
		_animationEndListener = listener;
		Animation animation = new TranslateAnimation(0, 0, 0, getHeight());
		animation.setDuration(Animations.ANIMATION_DURATION);
		animation.setAnimationListener(this);
		startAnimation(animation);
	}
	private boolean _isShowed = false;
	public boolean isShowed() {
		return _isShowed;
	}
	
	public void animatedShow(int offset) {
		Animation animation = new TranslateAnimation(0, 0, offset, 0);
		animation.setDuration(Animations.ANIMATION_DURATION);
		startAnimation(animation);
		_isShowed = true;
	}
	private OnAnimationEndListener _animationEndListener;
	public void animatedOffsetRight(int offset, OnAnimationEndListener listener) {
		_animationEndListener = listener;
		Animation animation = new TranslateAnimation(-offset, 0, 0, 0);
		animation.setDuration(Animations.ANIMATION_DURATION);
		animation.setAnimationListener(this);
		startAnimation(animation);
	}
	public void animatedOffsetLeft(int offset, OnAnimationEndListener listener) {
		_animationEndListener = listener;
		Animation animation = new TranslateAnimation(0, -offset, 0, 0);
		animation.setDuration(Animations.ANIMATION_DURATION);
		animation.setAnimationListener(this);
		startAnimation(animation);
	}

	@Override
	public void onAnimationEnd(Animation arg0) {
		if (_animationEndListener != null) {
				_animationEndListener.onAnimated(this);
		}
		clearAnimation();
	}

	@Override
	public void onAnimationRepeat(Animation arg0) {}

	@Override
	public void onAnimationStart(Animation arg0) {}
}