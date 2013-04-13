package ru.slavabulgakov.busesspb;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TicketCloseLess extends Ticket {
	
	public TicketCloseLess(Context context) {
		super(context);
	}

	@Override
	protected void load(Context context, AttributeSet attrs) {
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Ticket, 0, 0);
		String routeNumber = a.getString(R.styleable.Ticket_routeNumber);
		a.recycle();
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.ticket_closeless, this, true);
		
		_linearLayout = (LinearLayout)getChildAt(0);
		_icon = (ImageView)_linearLayout.getChildAt(0);
		_routeNumber = (TextView)_linearLayout.getChildAt(1);
		_routeNumber.setText(routeNumber);
	}
}