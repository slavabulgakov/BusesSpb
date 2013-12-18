package ru.slavabulgakov.busesspb.controls;

import java.text.SimpleDateFormat;
import java.util.Locale;

import ru.slavabulgakov.busesspb.R;
import ru.slavabulgakov.busesspb.model.Model;
import ru.slavabulgakov.busesspb.paths.Forecast;
import ru.slavabulgakov.busesspb.paths.Forecasts;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RightMenu extends LinearLayout {
	TextView _title;
	Model _model;
	ListView _listView;
	Forecasts _forecasts;
	SimpleDateFormat _format;
	RelativeLayout _progressBar;
	
	public void setModel(Model model) {
		_model = model;
	}
	
	public void move(double percent) {
    	double delta = 100;
    	if (percent > 0) {
        	RelativeLayout.LayoutParams lpRight = (RelativeLayout.LayoutParams)getLayoutParams();
        	lpRight.setMargins(0, 0, (int)(_model.dpToPx(-200)), 0);
	    	setLayoutParams(lpRight);
		} else {
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)getLayoutParams();
	    	lp.setMargins(0, 0, (int)(_model.dpToPx(-delta + delta * Math.abs(percent))), 0);
	    	setLayoutParams(lp);
		}
    }
	
	public void setLoading() {
		_progressBar.setVisibility(View.VISIBLE);
		_listView.setVisibility(View.GONE);
	}
	
	public void setLoaded() {
		_progressBar.setVisibility(View.GONE);
		_listView.setVisibility(View.VISIBLE);
	}
	
	public void setTitle(String title) {
		_title.setText(title);
	}
	
	private void _load(Context context, AttributeSet attrs) {
		View.inflate(context, R.layout.right_menu, this);
		_title = (TextView)findViewById(R.id.rightMenuTitle);
		_listView = (ListView)findViewById(R.id.rightMenuListView);
		_format = new SimpleDateFormat("HH:mm", Locale.US);
		_progressBar = (RelativeLayout)findViewById(R.id.rightMenuProgressBar);
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
	
	public void loadForecasts(Forecasts forecasts) {
		_forecasts = forecasts;
		_listView.setAdapter(new ListAdapter() {
			
			class ViewHolder {
				public boolean needInflate;
				public TextView routeNumber;
				public ImageView routeKindImg;
				public TextView time;
			}
			
			@Override
			public void unregisterDataSetObserver(DataSetObserver arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void registerDataSetObserver(DataSetObserver arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean isEmpty() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean hasStableIds() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public int getViewTypeCount() {
				return 1;
			}
			private void _setViewHolder(View view) {
				ViewHolder vh = new ViewHolder();
				vh.routeKindImg = (ImageView)view.findViewById(R.id.listItemForecastKind);
				vh.routeNumber = (TextView)view.findViewById(R.id.listItemForecastRouteName);
				vh.time = (TextView)view.findViewById(R.id.listItemForecastTime);
				vh.needInflate = false;
				view.setTag(vh);
			}
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
				if (convertView == null) {
					convertView = inflater.inflate(R.layout.listitem_forecast, parent, false);
					
					_setViewHolder(convertView);
				} else if (((ViewHolder)convertView.getTag()).needInflate) {
					convertView = inflater.inflate(R.layout.listitem_forecast, parent, false);
					_setViewHolder(convertView);
				}
				
				Forecast forecast = _forecasts.get(position);
				
				ViewHolder vh = (ViewHolder)convertView.getTag();
				vh.time.setText(_format.format(forecast.time));
				vh.routeNumber.setText(forecast.transportNumber);
				
				return convertView;
			}
			
			@Override
			public int getItemViewType(int arg0) {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public long getItemId(int arg0) {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public Object getItem(int arg0) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int getCount() {
				int count = 0;
				if (_forecasts != null) {
					count = _forecasts.size();
				}
				return count;
			}
			
			@Override
			public boolean isEnabled(int position) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean areAllItemsEnabled() {
				// TODO Auto-generated method stub
				return false;
			}
		});
	}
}
