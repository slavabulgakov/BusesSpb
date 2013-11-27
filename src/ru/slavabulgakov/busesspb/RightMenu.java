package ru.slavabulgakov.busesspb;

import java.text.SimpleDateFormat;
import java.util.Locale;

import ru.slavabulgakov.busesspb.model.Model;
import ru.slavabulgakov.busesspb.paths.Forecast;
import ru.slavabulgakov.busesspb.paths.Forecasts;
import ru.slavabulgakov.busesspb.paths.Station;
import android.annotation.SuppressLint;
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
import android.widget.TextView;

public class RightMenu extends LinearLayout {
	TextView _title;
	Model _model;
	ListView _listView;
	Forecasts _forecasts;
	LayoutInflater _inflater;
	SimpleDateFormat _format;
	
	public void setModel(Model model) {
		_model = model;
	}
	
	private void _load(Context context, AttributeSet attrs) {
		_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		_inflater.inflate(R.layout.right_menu, this, true);
		_title = (TextView)((LinearLayout)getChildAt(0)).getChildAt(0);
		_listView = (ListView)((LinearLayout)getChildAt(0)).getChildAt(1);
		_format = new SimpleDateFormat("HH:mm", Locale.US);
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
		_model.getModelPaths().loadForecastForStationId(station.id);
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
				if (convertView == null) {
					convertView = _inflater.inflate(R.layout.listitem_forecast, parent, false);
					_setViewHolder(convertView);
				} else if (((ViewHolder)convertView.getTag()).needInflate) {
					convertView = _inflater.inflate(R.layout.listitem_forecast, parent, false);
					_setViewHolder(convertView);
				}
				
				Forecast forecast = _forecasts.get(position);
				
				ViewHolder vh = (ViewHolder)convertView.getTag();
				vh.time.setText(_format.format(forecast.time));
				
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
