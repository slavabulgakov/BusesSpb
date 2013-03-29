package ru.slavabulgakov.busesspb;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return super.onInterceptTouchEvent(ev);
	}

}
