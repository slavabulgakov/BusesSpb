package ru.slavabulgakov.busesspb;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

public class TicketCloseLess extends Ticket {
	
	public TicketCloseLess(Context context, AttributeSet attrs) {
		super(context, attrs);
		_ticket(context, attrs);
	}
	
	public TicketCloseLess(Context context) {
		super(context);
		_ticket(context, null);
	}
	
	private void _ticket(Context context, AttributeSet attrs) {
		_routeNumber.setPadding(0, 0, 0, 0);
		_icon.setPadding(0, 0, 0, 0);
		_linearLayout.setBackgroundColor(Color.TRANSPARENT);
		_closeButton.setVisibility(GONE);
	}

	public void setLast(Boolean last) {
		if (!last) {
			_routeNumber.setText(_routeNumber.getText() + ",");
		}
	}
}