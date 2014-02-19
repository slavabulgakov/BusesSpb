package ru.slavabulgakov.busesspb.controls;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import ru.slavabulgakov.busesspb.R;
import ru.slavabulgakov.busesspb.model.Model;
import ru.slavabulgakov.busesspb.paths.Forecast;

public class RightMenu extends LinearLayout {
	private TextView _title;
	private Model _model;
	private ListView _listView;
	private SimpleDateFormat _format;
	private RelativeLayout _progressBar;
	private Handler _handler;
    private ArrayList<Forecast> _forecasts;

	private Handler _getHandler() {
		if (_handler == null) {
			_handler = new Handler(Looper.getMainLooper());
		}
		return _handler;
	}
	
	public void setModel(Model model) {
		_model = model;
	}

    public void updatePosition() {
        if (_model.menuIsClosed(Model.MenuKind.Right)) {
            move(100);
        }
    }

	public void move(double position) {
    	double delta = 100;
    	if (position > 0) {
        	RelativeLayout.LayoutParams lpRight = (RelativeLayout.LayoutParams)getLayoutParams();
            if (lpRight != null) {
                lpRight.setMargins(0, 0, (_model.dpToPx(-200)), lpRight.bottomMargin);
                setLayoutParams(lpRight);
            }
		} else {
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)getLayoutParams();
            if (lp != null) {
                lp.setMargins(0, 0, (_model.dpToPx(-delta + delta * Math.abs(position))), lp.bottomMargin);
                setLayoutParams(lp);
            }
        }
    }
	
	public void setLoading() {
		_progressBar.setVisibility(View.VISIBLE);
		_listView.setVisibility(View.GONE);
	}
	
	public void setLoaded() {
        _getHandler().post(new Runnable() {
            @Override
            public void run() {
                _progressBar.setVisibility(View.GONE);
                _listView.setVisibility(View.VISIBLE);
            }
        });
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

    public void loadForecasts(ArrayList<Forecast> forecasts) {
        _forecasts = forecasts;
        _getHandler().post(new Runnable() {
            @Override
            public void run() {
                _listView.setAdapter(new ListAdapter() {

                    @Override
                    public void unregisterDataSetObserver(DataSetObserver arg0) {
                    }

                    @Override
                    public void registerDataSetObserver(DataSetObserver arg0) {
                    }

                    @Override
                    public boolean isEmpty() {
                        return false;
                    }

                    @Override
                    public boolean hasStableIds() {
                        return false;
                    }

                    @Override
                    public int getViewTypeCount() {
                        return 1;
                    }

                    private void _setViewHolder(View view) {
                        ForecastCellViewHolder vh = new ForecastCellViewHolder();
                        vh.leftIcon = (ImageView) view.findViewById(R.id.listItemForecastKind);
                        vh.leftText = (TextView) view.findViewById(R.id.listItemForecastRouteName);
                        vh.rightText = (TextView) view.findViewById(R.id.listItemForecastTime);
                        vh.centerText = (TextView) view.findViewById(R.id.listItemForecastRouteFullName);
                        vh.needInflate = false;
                        view.setTag(vh);
                    }

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        LayoutInflater inflater = null;
                        if (getContext() != null) {
                            inflater = ((Activity) getContext()).getLayoutInflater();
                        }
                        if (convertView == null) {
                            if (inflater != null) {
                                convertView = inflater.inflate(R.layout.listitem_forecast, parent, false);
                                _setViewHolder(convertView);
                            }
                        } else if (((ForecastCellViewHolder) convertView.getTag()).needInflate) {
                            if (inflater != null) {
                                convertView = inflater.inflate(R.layout.listitem_forecast, parent, false);
                                _setViewHolder(convertView);
                            }
                        }

                        Forecast forecast = _forecasts.get(position);

                        ForecastCellViewHolder vh = null;
                        if (convertView != null) {
                            vh = (ForecastCellViewHolder) convertView.getTag();
                            vh.rightText.setText(_format.format(forecast.time));
                            vh.leftText.setText(forecast.transportNumber);
                            Pair<Integer, Integer> res = vh.backgroundAndIconByKind(forecast.transportKind);
                            convertView.setBackgroundResource(res.first);
                            vh.leftIcon.setImageResource(res.second);
                            vh.centerText.setText(forecast.fullName);
                        }

                        return convertView;
                    }

                    @Override
                    public int getItemViewType(int arg0) {
                        return 0;
                    }

                    @Override
                    public long getItemId(int arg0) {
                        return 0;
                    }

                    @Override
                    public Object getItem(int arg0) {
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
                        return false;
                    }

                    @Override
                    public boolean areAllItemsEnabled() {
                        return false;
                    }
                });
            }
        });
    }
}
