package ru.slavabulgakov.busesspb;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Ticket extends LinearLayout {
	
	interface OnRemoveListener {
		void onRemove(Ticket ticket);
	}
	
	LinearLayout _linearLayout;
	ImageView _icon;
	ImageButton _closeButton;
	TextView _routeNumber;
	Route _route;
	OnRemoveListener _onRemoveListener;
	
	private void _ticket(Context context, AttributeSet attrs) {
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Ticket, 0, 0);
		String routeNumber = a.getString(R.styleable.Ticket_routeNumber);
		a.recycle();
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.ticket, this, true);
		
		_linearLayout = (LinearLayout)getChildAt(0);
		
		LinearLayout vertLinearLayout = (LinearLayout)_linearLayout.getChildAt(0);
		_icon = (ImageView)vertLinearLayout.getChildAt(0);
		
		_routeNumber = (TextView)vertLinearLayout.getChildAt(1);
		_routeNumber.setText(routeNumber);
		
		_closeButton = (ImageButton)_linearLayout.getChildAt(1);
		_closeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				_onRemoveListener.onRemove(Ticket.this);
				((ViewGroup)Ticket.this.getParent()).removeView(Ticket.this);
			}
		});
	}
	
	public Ticket(Context context) {
		super(context);
		_ticket(context, null);
	}



	public Ticket(Context context, AttributeSet attrs) {
		super(context, attrs);
		_ticket(context, attrs);
	}
	
	private void _updateBackground() {
		int resId = -1;
		int closeButton = -1;
		int icon = -1;
		switch (_route.kind) {
		case Bus:
			resId = R.color.bus;
			closeButton = R.color.bus_ticket;
			icon = R.drawable.bus_30_30;
			break;
			
		case Trolley:
			resId = R.color.trolley;
			closeButton = R.color.trolley_ticket;
			icon = R.drawable.trolley_30_30;
			break;
			
		case Tram:
			resId = R.color.tram;
			closeButton = R.color.tram_ticket;
			icon = R.drawable.tram_30_30;
			break;

		default:
			break;
		}
		_linearLayout.setBackgroundResource(resId);
		_closeButton.setBackgroundColor(closeButton);
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
}