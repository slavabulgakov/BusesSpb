package ru.slavabulgakov.busesspb;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.View.OnClickListener;

public class CheckButton extends LinearLayout implements OnClickListener {
	boolean _checked;
	LinearLayout _linearLayout;
	ImageView _imageView;
	OnClickListener _listener;
	int _id;

	public CheckButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CheckButton, 0, 0);
		Drawable icon = a.getDrawable(R.styleable.CheckButton_android_src);
		int iconBackground = a.getResourceId(R.styleable.CheckButton_iconBackground, R.drawable.ticket_bg_bus);
		int background = a.getResourceId(R.styleable.CheckButton_background, R.drawable.btn_selected_black);
		_checked = a.getBoolean(R.styleable.CheckButton_checked, false);
		_id = a.getInt(R.styleable.CheckButton_android_id, -1);
		a.recycle();
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.check_button, this, true);
		
		_linearLayout = (LinearLayout)getChildAt(0);
		_linearLayout.setBackgroundResource(background);
		_linearLayout.setOnClickListener(this);
		
		_imageView = (ImageView)findViewById(R.id.checkButtonIcon);
		_imageView.setImageDrawable(icon);
		_imageView.setBackgroundResource(iconBackground);
	}

	@Override
	public void onClick(View v) {
		setChecked(!_checked);
		if (_listener != null) {
			_listener.onClick(this);
		}
	}
	
	public void setOnClickListener(OnClickListener listener) {
		_listener = listener;
	}
	
	public boolean checked() {
		return _checked;
	}
	
	public void setChecked(boolean checked) {
		_checked = checked;
		_linearLayout.setSelected(_checked);
	}
}