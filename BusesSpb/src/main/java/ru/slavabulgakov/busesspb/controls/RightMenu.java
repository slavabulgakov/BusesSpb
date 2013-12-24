package ru.slavabulgakov.busesspb.controls;

import java.text.SimpleDateFormat;
import java.util.Locale;

import ru.slavabulgakov.busesspb.Animations;
import ru.slavabulgakov.busesspb.R;
import ru.slavabulgakov.busesspb.controller.Controller;
import ru.slavabulgakov.busesspb.model.Model;
import ru.slavabulgakov.busesspb.paths.Forecast;
import ru.slavabulgakov.busesspb.paths.Forecasts;
import ru.slavabulgakov.busesspb.paths.Station;
import ru.slavabulgakov.busesspb.paths.Stations;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RightMenu extends LinearLayout {
	private TextView _title;
	private Model _model;
	private ListView _listView;
	private Forecasts _forecasts;
	private Stations _nearblyStations;
	private SimpleDateFormat _format;
	private RelativeLayout _progressBar;
	private EditText _stationText;
	private Button _stationButton;
	private ProgressBar _stationProgressBar;
	private ListView _stationListView;
	private LinearLayout _rightMenuLayout;
	private Handler _handler;
	
	private Handler _getHandler() {
		if (_handler == null) {
			_handler = new Handler(Looper.getMainLooper());
		}
		return _handler;
	}
	
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
		_rightMenuLayout = (LinearLayout)findViewById(R.id.rightMenuLayout);
		_stationText = (EditText)findViewById(R.id.stationText);
		_stationButton = (Button)findViewById(R.id.stationButton);
		_stationProgressBar = (ProgressBar)findViewById(R.id.stationProgressBar);
		_stationProgressBar.setVisibility(View.GONE);
		_stationListView = (ListView)findViewById(R.id.stationListView);
		_stationListView.setVisibility(View.GONE);
		_stationButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				_stationButton.setVisibility(View.GONE);
				_stationProgressBar.setVisibility(View.VISIBLE);
				TranslateAnimation animation = new TranslateAnimation(0, 0, -_model.dpToPx(100), 0);
				_stationListView.setVisibility(View.VISIBLE);
				animation.setDuration(5000);
				animation.setAnimationListener(new AnimationListener() {
					
					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						
					}
				});
				_rightMenuLayout.startAnimation(animation);
			}
		});
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
	
	public void loadNearblyStations(Stations stations) {
		_nearblyStations = stations;
		_getHandler().post(new Runnable() {
			
			@Override
			public void run() {
				_stationListView.setAdapter(new ListAdapter() {
					
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
						TransportCellViewHolder vh = new TransportCellViewHolder();
						vh.leftIcon = (ImageView)view.findViewById(R.id.listItemForecastKind);
						vh.leftText = (TextView)view.findViewById(R.id.listItemForecastRouteName);
						vh.rightText = (TextView)view.findViewById(R.id.listItemForecastTime);
						vh.needInflate = false;
						view.setTag(vh);
					}
					
					@Override
					public View getView(int position, View convertView, ViewGroup parent) {
						LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
						if (convertView == null) {
							convertView = inflater.inflate(R.layout.listitem_forecast, parent, false);
							
							_setViewHolder(convertView);
						} else if (((TransportCellViewHolder)convertView.getTag()).needInflate) {
							convertView = inflater.inflate(R.layout.listitem_forecast, parent, false);
							_setViewHolder(convertView);
						}
						
						Station station= _nearblyStations.get(position);
						
						TransportCellViewHolder vh = (TransportCellViewHolder)convertView.getTag();
//						vh.rightText.setText(_format.format(forecast.time));
						vh.leftText.setText(station.name);
						Pair<Integer, Integer> res = vh.backgroundAndIconByKind(station.kind);
						convertView.setBackgroundResource(res.first);
						vh.leftIcon.setImageResource(res.second);
						
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
						return _nearblyStations.size();
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
		});
	}
	
	public void loadForecasts(Forecasts forecasts) {
		_forecasts = forecasts;
		_listView.setAdapter(new ListAdapter() {
			
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
				TransportCellViewHolder vh = new TransportCellViewHolder();
				vh.leftIcon = (ImageView)view.findViewById(R.id.listItemForecastKind);
				vh.leftText = (TextView)view.findViewById(R.id.listItemForecastRouteName);
				vh.rightText = (TextView)view.findViewById(R.id.listItemForecastTime);
				vh.needInflate = false;
				view.setTag(vh);
			}
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
				if (convertView == null) {
					convertView = inflater.inflate(R.layout.listitem_forecast, parent, false);
					
					_setViewHolder(convertView);
				} else if (((TransportCellViewHolder)convertView.getTag()).needInflate) {
					convertView = inflater.inflate(R.layout.listitem_forecast, parent, false);
					_setViewHolder(convertView);
				}
				
				Forecast forecast = _forecasts.get(position);
				
				TransportCellViewHolder vh = (TransportCellViewHolder)convertView.getTag();
				vh.rightText.setText(_format.format(forecast.time));
				vh.leftText.setText(forecast.transportNumber);
				Pair<Integer, Integer> res = vh.backgroundAndIconByKind(forecast.transportKind);
				convertView.setBackgroundResource(res.first);
				vh.leftIcon.setImageResource(res.second);
				
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
