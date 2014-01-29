package ru.slavabulgakov.busesspb.controls;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import ru.slavabulgakov.busesspb.R;
import ru.slavabulgakov.busesspb.StationsAdapter;
import ru.slavabulgakov.busesspb.model.Model;
import ru.slavabulgakov.busesspb.paths.Forecast;
import ru.slavabulgakov.busesspb.paths.Station;
import ru.slavabulgakov.busesspb.paths.Stations;

public class RightMenu extends LinearLayout implements View.OnClickListener, AdapterView.OnItemClickListener, TextWatcher {
	private TextView _title;
	private Model _model;
	private ListView _listView;
	private SimpleDateFormat _format;
	private RelativeLayout _progressBar;
	private EditText _stationText;
	private ProgressBar _stationProgressBar;
	private ListView _stationListView;
    private RelativeLayout _stationLayout;
    private Button _stationsBackButton;
	private LinearLayout _rightMenuLayout;
	private Handler _handler;
    private ArrayList<Object> _forecasts;
    private Button _forecastsButton;
    private Context _context;
    private ImageButton _clearButton;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Station station = ((StationsAdapter)_stationListView.getAdapter()).getItem(position);
        _model.setData("stationId", station.id);
        _model.setData("stationTitle", station.name);
        changeToState(State.FORECASTS, true);
        _listener.willShowForecastsMenu();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        String text = s.toString();
        StationsAdapter adapter = (StationsAdapter)_stationListView.getAdapter();
        if (adapter != null) {
            adapter.getFilter().filter(text);
        }
        if (text.length() > 0) {
            _clearButton.setVisibility(View.VISIBLE);
        } else {
            _clearButton.setVisibility(View.GONE);
        }
    }

    public interface Listener {
        void onClickStationsButton();
        void willShowForecastsMenu();
    }

    private Listener _listener;
    public void setListener(Listener listener) {
        _listener = listener;
    }
	
	private Handler _getHandler() {
		if (_handler == null) {
			_handler = new Handler(Looper.getMainLooper());
		}
		return _handler;
	}
	
	public void setModel(Model model) {
		_model = model;
	}

	public void move(double position) {
    	double delta = 100;
    	if (position > 0) {
        	RelativeLayout.LayoutParams lpRight = (RelativeLayout.LayoutParams)getLayoutParams();
        	lpRight.setMargins(0, 0, (_model.dpToPx(-200)), lpRight.bottomMargin);
	    	setLayoutParams(lpRight);
		} else {
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)getLayoutParams();
	    	lp.setMargins(0, 0, (_model.dpToPx(-delta + delta * Math.abs(position))), lp.bottomMargin);
	    	setLayoutParams(lp);
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
        _context = context;
		View.inflate(context, R.layout.right_menu, this);
		_title = (TextView)findViewById(R.id.rightMenuTitle);
		_listView = (ListView)findViewById(R.id.rightMenuListView);
		_format = new SimpleDateFormat("HH:mm", Locale.US);
		_progressBar = (RelativeLayout)findViewById(R.id.rightMenuProgressBar);
		_rightMenuLayout = (LinearLayout)findViewById(R.id.rightMenuLayout);

		_stationText = (EditText)findViewById(R.id.stationText);
        _stationText.addTextChangedListener(this);

		_stationProgressBar = (ProgressBar)findViewById(R.id.stationProgressBar);

		_stationListView = (ListView)findViewById(R.id.stationListView);
        _stationListView.setOnItemClickListener(this);

        _stationLayout = (RelativeLayout)findViewById(R.id.stationLayout);

        _stationsBackButton = (Button)findViewById(R.id.stationsBack);
        _stationsBackButton.setOnClickListener(this);

        _forecastsButton = (Button)findViewById(R.id.forecastsButton);
        _forecastsButton.setOnClickListener(this);

        _clearButton = (ImageButton)findViewById(R.id.clearStationsText);
        _clearButton.setVisibility(GONE);
        _clearButton.setOnClickListener(this);

        _showBackButton(false);
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

	public void loadNearblyStations(final Stations nearblyStations) {
		_getHandler().post(new Runnable() {
			
			@Override
			public void run() {
                _stationProgressBar.setVisibility(GONE);
                _stationListView.setVisibility(VISIBLE);
                StationsAdapter stationsAdapter = (StationsAdapter)_stationListView.getAdapter();
                if (stationsAdapter != null) {
                    stationsAdapter.setNearblyStations(nearblyStations);
                } else {
                    stationsAdapter = new StationsAdapter(_context, _model, nearblyStations);
                    _stationListView.setAdapter(stationsAdapter);
                }
                stationsAdapter.getFilter().filter(_stationText.getText());
			}
		});
	}

	public void loadForecasts(ArrayList<Object> forecasts) {
        _forecasts = forecasts;
        _getHandler().post(new Runnable() {
            @Override
            public void run() {
                _listView.setAdapter(new ListAdapter() {

                    @Override
                    public void unregisterDataSetObserver(DataSetObserver arg0) {}

                    @Override
                    public void registerDataSetObserver(DataSetObserver arg0) {}

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

                        Forecast forecast = (Forecast)_forecasts.get(position);

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

    public enum State {
        STATIONS,
        FORECASTS
    }

    private State _state;
    public State getState() {
        return _state;
    }
    public void changeToState(State state, boolean animated) {
        if (state != _state) {
            _state = state;
            switch (state) {
                case FORECASTS:
                    _changeToForecastsState(animated);
                    break;

                case STATIONS:
                    _changeToStationsState(animated);
                    break;
            }
        }
    }

    private void _showBackButton(boolean show) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)_stationListView.getLayoutParams();
        if (show) {
            _stationsBackButton.setVisibility(VISIBLE);
            lp.setMargins(0, 0, 0, _model.dpToPx(50));
        } else {
            _stationsBackButton.setVisibility(GONE);
            lp.setMargins(0, 0, 0, 0);
        }
        _stationListView.setLayoutParams(lp);
    }

    private void _changeToStationsState(boolean animated) {
        _showBackButton(_model.getData("stationId") != null);
        _stationProgressBar.setVisibility(View.VISIBLE);
        _stationLayout.setVisibility(View.VISIBLE);
        if (animated) {
            LinearLayout.LayoutParams lp_ = (LayoutParams) _rightMenuLayout.getLayoutParams();
            _rightMenuLayout.setLayoutParams(new LinearLayout.LayoutParams(lp_.width, getHeight()));
            TranslateAnimation animation = new TranslateAnimation(0, 0, -(getHeight() - _model.dpToPx(20)), 0);
            animation.setDuration(500);
            _rightMenuLayout.startAnimation(animation);
        }
    }

    private void _changeToForecastsState(boolean animated) {
        if (animated) {
            LinearLayout.LayoutParams lp_ = (LayoutParams) _rightMenuLayout.getLayoutParams();
            _rightMenuLayout.setLayoutParams(new LinearLayout.LayoutParams(lp_.width, getHeight()));
            TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -(getHeight() - _model.dpToPx(20)));
            animation.setDuration(500);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    _stationLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            _stationLayout.setVisibility(GONE);
                        }
                    });
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            _rightMenuLayout.setAnimation(animation);
            _rightMenuLayout.startAnimation(animation);
        } else {
            _stationLayout.setVisibility(GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == _stationsBackButton) {
            changeToState(State.FORECASTS, true);
            _listener.willShowForecastsMenu();
        } else if (v == _forecastsButton) {
            changeToState(State.STATIONS, true);
            _listener.onClickStationsButton();
        } else if (v == _clearButton) {
            _stationText.setText("");
        }
    }
}
