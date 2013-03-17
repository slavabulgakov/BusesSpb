package ru.slavabulgakov.busesspb;

import ru.slavabulgakov.busesspb.Model.Transport;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Ticket extends LinearLayout {
	
	interface OnRemoveListener {
		void onRemove(Ticket ticket);
	}
	
	Button _closeButton;
	TextView _roueNumber;
	Transport _transport;
	OnRemoveListener _onRemoveListener;
	
	private void _ticket(Context context, AttributeSet attrs) {
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Ticket, 0, 0);
		String routeNumber = a.getString(R.styleable.Ticket_routeNumber);
		a.recycle();
		
		setOrientation(LinearLayout.HORIZONTAL);
		setGravity(Gravity.CENTER_VERTICAL);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.ticket, this, true);
		_roueNumber = (TextView)getChildAt(0);
		_roueNumber.setText(routeNumber);
		
		_closeButton = (Button)getChildAt(1);
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
	
	public void setTransport(Transport transport) {
		_transport = transport;
		_roueNumber.setText(_transport.routeNumber);
	}
	
	public Transport getTransport() {
		return _transport;
	}
	
	public void setOnRemoveListener(OnRemoveListener listener) {
		_onRemoveListener = listener;
	}
}