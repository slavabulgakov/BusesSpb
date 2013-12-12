package ru.slavabulgakov.busesspb.controls;

import ru.slavabulgakov.busesspb.R;
import ru.slavabulgakov.busesspb.model.Model;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class LeftMenu extends LinearLayout {
	private Model _model;
	
	public void setModel(Model model) {
		_model = model;
	}
	
	private void _load(Context context, AttributeSet attrs) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.left_menu, this, true);
	}

	public LeftMenu(Context context) {
		super(context);
		_load(context, null);
	}

	public LeftMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		_load(context, attrs);
	}

	@SuppressLint("NewApi")
	public LeftMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_load(context, attrs);
	}
	
	public void move(double percent) {
    	double delta = 100;
    	if (percent > 0) {
    		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)getLayoutParams();
        	lp.setMargins((int)(_model.dpToPx(-delta + delta * percent)), 0, 0, 0);
        	setLayoutParams(lp);
		}
    }

}
