package ru.slavabulgakov.busesspb;

import ru.slavabulgakov.busesspb.model.Model;
import ru.slavabulgakov.busesspb.paths.Station;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RightMenu extends LinearLayout {
	TextView _title;
	Model _model;
	
	public void setModel(Model model) {
		_model = model;
	}
	
	private void _load(Context context, AttributeSet attrs) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.right_menu, this, true);
		_title = (TextView)((LinearLayout)getChildAt(0)).getChildAt(0);
	}

	public RightMenu(Context context) {
		super(context);
		_load(context, null);
	}

	public RightMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		_load(context, attrs);
	}

	@SuppressLint("NewApi")
	public RightMenu(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		_load(context, attrs);
	}
	
	public void loadByStation(Station station) {
		_title.setText(station.name);
		_model.getModelPaths().loadStationForId(station.id);
	}
}
