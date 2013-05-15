package ru.slavabulgakov.busesspb;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ru.slavabulgakov.busesspb.CloseAllTickets.OnAnimationEndListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.location.Location;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.SupportMapFragment;
import android.view.WindowManager;

public class MainActivity extends BaseActivity {
	
	private GoogleMap _map;
	private Timer _timer;
	private ListView _listView;
	private EditText _editText;
	private ProgressBar _progressBar;
	private RootView _rootView;
	private LinearLayout _busFilter;
	private LinearLayout _trolleyFilter;
	private LinearLayout _tramFilter;
	private LinearLayout _menuBusFilter;
	private LinearLayout _menuTrolleyFilter;
	private LinearLayout _menuTramFilter;
	private RelativeLayout _mainRoutesBtn;
	private ImageButton _clearButton;
	LinearLayout _ticketsLayout;
	private LinearLayout _leftMenu;
	private ImageView _menuIcon;
	private ImageButton _internetDenyImageButton;
	
    @SuppressLint("NewApi")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        _rootView = (RootView)findViewById(R.id.mainMapLayout);
		_rootView.setOnOpenListener(Contr.getInstance());
		_rootView.setModel(_model);
        
        _progressBar = (ProgressBar)findViewById(R.id.selectRouteProgressBar);
		
		_editText = (EditText)findViewById(R.id.selectRouteText);
		_editText.addTextChangedListener(Contr.getInstance());
		_editText.setOnKeyListener(Contr.getInstance());
		
		_clearButton = (ImageButton)findViewById(R.id.clearRouteText);
		_clearButton.setOnClickListener(Contr.getInstance());
		if (_editText.getText().length() > 0) {
			_clearButton.setVisibility(View.VISIBLE);
		} else {
			_clearButton.setVisibility(View.GONE);
		}
		
		_listView = (ListView)findViewById(R.id.selectRouteListView);
		_listView.setOnItemClickListener(Contr.getInstance());
        
	    _mainRoutesBtn = (RelativeLayout)findViewById(R.id.mainRoutesBtn);
	    _mainRoutesBtn.setOnClickListener(Contr.getInstance());
	    
	    _ticketsLayout = (LinearLayout)findViewById(R.id.selectRouteTickets);
		for (Route route : _model.getFavorite()) {
			Ticket ticket = new Ticket(this);
			ticket.setRoute(route);
			ticket.setOnRemoveListener(Contr.getInstance());
			_ticketsLayout.addView(ticket);
		}
		putCloseAllButtonToTicketsLayout();
		HorizontalScrollView routeTicketsScrollView = (HorizontalScrollView)findViewById(R.id.routeTicketsScrollView);
		if (_model.getFavorite().size() > 0) {
			routeTicketsScrollView.setVisibility(View.VISIBLE);
		} else {
			routeTicketsScrollView.setVisibility(View.GONE);
		}
		
		
		
		
		_busFilter = (LinearLayout)findViewById(R.id.busFilter);
		_busFilter.setOnClickListener(Contr.getInstance());
		
		_trolleyFilter = (LinearLayout)findViewById(R.id.trolleyFilter);
		_trolleyFilter.setOnClickListener(Contr.getInstance());
		
		_tramFilter = (LinearLayout)findViewById(R.id.tramFilter);
		_tramFilter.setOnClickListener(Contr.getInstance());
		
		_menuBusFilter = (LinearLayout)findViewById(R.id.menuBusFilter);
    	_menuBusFilter.setOnClickListener(Contr.getInstance());
    	
    	_menuTrolleyFilter = (LinearLayout)findViewById(R.id.menuTrolleyFilter);
		_menuTrolleyFilter.setOnClickListener(Contr.getInstance());
		
		_menuTramFilter = (LinearLayout)findViewById(R.id.menuTramFilter);
		_menuTramFilter.setOnClickListener(Contr.getInstance());
		updateFilterButtons();
		
		
		
		
		
		((ImageButton)findViewById(R.id.location)).setOnClickListener(Contr.getInstance());
		((ImageButton)findViewById(R.id.plus)).setOnClickListener(Contr.getInstance());
		((ImageButton)findViewById(R.id.minus)).setOnClickListener(Contr.getInstance());
		
		
		((ImageButton)findViewById(R.id.about)).setOnClickListener(Contr.getInstance());
		
		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		int width = display.getWidth();
		width = _model.pxToDp(width);
		if (width < 400) {
			LinearLayout zoom = (LinearLayout)findViewById(R.id.zoomControls);
			RelativeLayout.LayoutParams zoomLayoutParams = (LayoutParams)zoom.getLayoutParams();
			zoomLayoutParams.bottomMargin = _model.dpToPx(60);
			zoom.setLayoutParams(zoomLayoutParams);
		}
		
		_leftMenu = (LinearLayout)findViewById(R.id.leftMenu);
		_menuIcon = ((ImageView)findViewById(R.id.menuIcon));
		
		if (_listView.getAdapter() == null) {
			Adapter adapter = new Adapter(this, _model);
			_listView.setAdapter(adapter);
			adapter.getFilter().filter(_editText.getText());
		}
		
		_internetDenyImageButton = (ImageButton)findViewById(R.id.internetDeny);
		_internetDenyImageButton.setOnClickListener(Contr.getInstance());
		
		_internetDenyImageButton.setVisibility(_internetDenyIconIsShowed() ? View.VISIBLE : View.INVISIBLE);
    }
    
    private Boolean _internetDenyIconIsShowed() {
    	Boolean showed = (Boolean)_model.getData("internetDenyIsShowed");
    	if (showed == null) {
			return false;
		}
    	return showed; 
    }
    private void _setInternetDenyIconShowed(Boolean showed) {
    	_model.setData("internetDenyIsShowed", showed);
    }
    
    public void showInternetDenyIcon() {
    	if (!_internetDenyIconIsShowed()) {
    		_setInternetDenyIconShowed(true);
    		runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					TranslateAnimation animation = new TranslateAnimation(_model.dpToPx(-100), 0, 0, 0);
		        	animation.setInterpolator(new BounceInterpolator());
		        	_internetDenyImageButton.setVisibility(View.VISIBLE);
		        	animation.setDuration(2000);
		        	animation.setAnimationListener(new AnimationListener() {
		    			
		    			@Override
		    			public void onAnimationStart(Animation animation) {}
		    			
		    			@Override
		    			public void onAnimationRepeat(Animation animation) {}
		    			
		    			@Override
		    			public void onAnimationEnd(Animation animation) {}
		    		});
		        	_internetDenyImageButton.startAnimation(animation);
				}
			});
    		
		}
    }
    public void hideInternetDenyIcon() {
    	if (_internetDenyIconIsShowed()) {
    		_setInternetDenyIconShowed(false);
    		runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					TranslateAnimation animation = new TranslateAnimation(0, _model.dpToPx(-100), 0, 0);
		        	animation.setInterpolator(new AnticipateInterpolator());
		        	animation.setDuration(2000);
		        	animation.setAnimationListener(new AnimationListener() {
		    			
		    			@Override
		    			public void onAnimationStart(Animation animation) {}
		    			
		    			@Override
		    			public void onAnimationRepeat(Animation animation) {}
		    			
		    			@Override
		    			public void onAnimationEnd(Animation animation) {
		    				_internetDenyImageButton.setVisibility(View.INVISIBLE);
		    			}
		    		});
		        	_internetDenyImageButton.startAnimation(animation);
				}
			});
		}
    }
    
    public void moveLeftMenu(double percent) {
    	double delta = 100;
    	RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)_leftMenu.getLayoutParams();
    	lp.setMargins((int)(_model.dpToPx(-delta + delta * percent)), 0, 0, 0);
    	_leftMenu.setLayoutParams(lp);
    }
    
    public void moveCameraToMyLocation() {
		Location location = _map.getMyLocation();
		if (location != null) {
			_map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
		}
	}
    
    public void zoomCameraTo(float zoom) {
    	CameraPosition camPos = _map.getCameraPosition();
    	CameraUpdate camUp = CameraUpdateFactory.zoomTo(camPos.zoom + zoom);
        _map.animateCamera(camUp);
	}
    
    public void updateFilterButtons() {
    	LinearLayout kindBtns = (LinearLayout)findViewById(R.id.kindBtns);
    	if (_model.getFavorite().size() > 0) {
			kindBtns.setVisibility(View.INVISIBLE);
		} else {
			kindBtns.setVisibility(View.VISIBLE);
		}
    	_busFilter.setSelected(_model.isEnabledFilter(TransportKind.Bus));
    	_trolleyFilter.setSelected(_model.isEnabledFilter(TransportKind.Trolley));
    	_tramFilter.setSelected(_model.isEnabledFilter(TransportKind.Tram));
		
		_menuBusFilter.setSelected(_model.isEnabledFilterMenu(TransportKind.Bus));
		_menuTrolleyFilter.setSelected(_model.isEnabledFilterMenu(TransportKind.Trolley));
		_menuTramFilter.setSelected(_model.isEnabledFilterMenu(TransportKind.Tram));
    }
    
	@Override
	protected void onPause() {
		_model.saveAllTransportOverlays();
		_model.saveLastSimpleTransportView();
		_model.saveFavorite();
		_model.saveLocation();
		_model.saveZoom();
    	if (_timer != null) {
    		_timer.cancel();
    		_timer = null;
		}
		super.onPause();
	}


	@Override
	protected void onResume() {
		_map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		if (_map != null) {
			CameraUpdate center = CameraUpdateFactory.newLatLng(_model.getLocation());
	        _map.moveCamera(center);
	        CameraUpdate zoom = CameraUpdateFactory.zoomTo(_model.getZoom());
	        _map.animateCamera(zoom);
	        _map.setMyLocationEnabled(true);
	        _map.setOnCameraChangeListener(Contr.getInstance());
	        _map.getUiSettings().setMyLocationButtonEnabled(false);
	        _map.getUiSettings().setZoomControlsEnabled(false);
		}
		
		_toggleRotateMap(_model.menuIsOpened());
		
		if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
			_busFilter.setVisibility(View.GONE);
			_trolleyFilter.setVisibility(View.GONE);
			_tramFilter.setVisibility(View.GONE);
			_menuBusFilter.setVisibility(View.GONE);
			_menuTrolleyFilter.setVisibility(View.GONE);
			_menuTramFilter.setVisibility(View.GONE);
			((RelativeLayout)findViewById(R.id.mainRoutesBtn)).setVisibility(View.GONE);
			_editText.setVisibility(View.GONE);
			((ImageButton)findViewById(R.id.about)).setVisibility(View.GONE);
			_progressBar.setVisibility(View.GONE);
			((LinearLayout)findViewById(R.id.zoomControls)).setVisibility(View.GONE);
			((ImageButton)findViewById(R.id.location)).setVisibility(View.GONE);
		} else {
			_busFilter.setVisibility(View.VISIBLE);
			_trolleyFilter.setVisibility(View.VISIBLE);
			_tramFilter.setVisibility(View.VISIBLE);
			_menuBusFilter.setVisibility(View.VISIBLE);
			_menuTrolleyFilter.setVisibility(View.VISIBLE);
			_menuTramFilter.setVisibility(View.VISIBLE);
			((RelativeLayout)findViewById(R.id.mainRoutesBtn)).setVisibility(View.VISIBLE);
			_editText.setVisibility(View.VISIBLE);
			((ImageButton)findViewById(R.id.about)).setVisibility(View.VISIBLE);
			_progressBar.setVisibility(View.VISIBLE);
			((LinearLayout)findViewById(R.id.zoomControls)).setVisibility(View.VISIBLE);
			((ImageButton)findViewById(R.id.location)).setVisibility(View.VISIBLE);
		}
		
		_updateControls();
		super.onResume();
	}

	public void putCloseAllButtonToTicketsLayout() {
		if (_model.getFavorite().size() > 1) {
			if (_ticketsLayout.getChildAt(0).getClass() != CloseAllTickets.class) {
				CloseAllTickets closeAllBtn = new CloseAllTickets(this);
				_ticketsLayout.addView(closeAllBtn, 0);
				closeAllBtn.animatedShow(_model.dpToPx(60));
			}
		} else {
			if (_ticketsLayout.getChildCount() > 0) {
				View closeAllBtn = _ticketsLayout.getChildAt(0);
				if (closeAllBtn.getClass() == CloseAllTickets.class) {
					((CloseAllTickets)closeAllBtn).animatedRemove(new OnAnimationEndListener() {
						
						@Override
						public void onAnimated(final CloseAllTickets button) {
							button.setVisibility(View.GONE);
							((View) button.getParent()).post(new Runnable() {
					            public void run() {
					            	((ViewGroup)button.getParent()).removeView(button);
					            }
					        });
						}
					});
				}
			}
		}
	}
    
    public void enableMapGestures(Boolean enable) {
    	if (enable) {
    		Timer timer = new Timer();
        	timer.schedule(new TimerTask() {
    			
    			@Override
    			public void run() {
    				runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							if (_map != null) {
								_map.getUiSettings().setAllGesturesEnabled(true);
							}
						}
					});
    				
    			}
    		}, 200);
		} else {
			if (_map != null) {
				_map.getUiSettings().setAllGesturesEnabled(enable);
			}
		}
	}
    
    public void updateTimer() {
    	if (_timer != null) {
			_timer.cancel();
			_timer = null;
		}
    	if (_model.isOnline()) {
    		if (_model.menuIsOpened() && !_model.allRouteIsLoaded()) {
    			_timer = new Timer();
    			_timer.schedule(new UpdateMenuContentTimerTask(this, _model), 0, 3000);
			} else if (!_model.menuIsOpened()) {
    			_timer = new Timer();
    	    	_timer.schedule(new UpdateTransportTimerTask(this, _model), 0, 3000);
			}
		} else {
			_timer = new Timer();
	    	_timer.schedule(new CheckInternetConnectionTimerTask(this, _model), 5000, 5000);
		}
    }
    
    public void loadMenuContent() {
    	_progressBar.setVisibility(View.VISIBLE);
//		_listView.setVisibility(View.INVISIBLE);
		_editText.setEnabled(false);
		_menuBusFilter.setEnabled(false);
		_menuTrolleyFilter.setEnabled(false);
		_menuTramFilter.setEnabled(false);
		_model.loadDataForAllRoutes();
    }
    
    private void _updateControls() {
    	
    	int resId = _model.menuIsOpened() ? R.drawable.menu_close_icon : R.drawable.menu_open_icon;
		_menuIcon.setImageResource(resId);
    	
    	if (_model.allRouteIsLoaded()) {
    		showMenuContent();
		}
    	
    	
    	updateTimer();
    	
    	
    	
    	
    	{// обновление тикетов выбранных маршрутов
    		LinearLayout ticketsLayout = (LinearLayout)findViewById(R.id.mainRoutesScrollView);
	    	ticketsLayout.removeAllViews();
			for (Route route : _model.getFavorite()) {
				TicketCloseLess ticket = new TicketCloseLess(this);
				ticket.setRoute(route);
				ticketsLayout.addView(ticket, 0);
			}
			
	    	HorizontalScrollView routesBtnScrollView = (HorizontalScrollView)findViewById(R.id.mainRoutesBtnScrollView);
	    	if (_model.getFavorite().size() > 0) {
	    		routesBtnScrollView.setVisibility(View.VISIBLE);
			} else {
				routesBtnScrollView.setVisibility(View.GONE);
			}
	    	
	    	RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)_mainRoutesBtn.getLayoutParams();
			lp.setMargins(lp.leftMargin, lp.topMargin, _model.getFavorite().size() > 0 ? _model.dpToPx(50) : 0, lp.bottomMargin);
			_mainRoutesBtn.setLayoutParams(lp);
		}
	}
    
    public void keyboardTurnOff() {
    	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(_editText.getWindowToken(), 0);
    }
    
    private void _toggleRotateMap(boolean isOpen) {
    	if (!isOpen) {
			if (_map != null) {
				if (_model.getFavorite().size() == 0) {
					_map.getUiSettings().setRotateGesturesEnabled(false);
					CameraPosition camPos = _map.getCameraPosition();
					if (camPos.bearing != 0) {
						_map.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(camPos.target, camPos.zoom, camPos.tilt, 0)));
					}
				} else {
					_map.getUiSettings().setRotateGesturesEnabled(true);
				}
			}
		}
    }
    
    public void menuChangeState(boolean isOpen) {
    	
    	
		_updateControls();
		updateFilterButtons();
		
		
		{// отключение клавиатуры
			if (!isOpen) {
				keyboardTurnOff();
			}
		}
		
		
		
		
		
		{// отключение/включение вертелки карты
			_toggleRotateMap(isOpen);
		}
		
		
		
		
		
		{// очистка карты
			if (!isOpen) {
				if (_map != null) {
					_map.clear();
					if (_model.isOnline()) {
						_model.removeAllTransportOverlays();
						updateTransport();
					} else {
						updateTransportOffline();
					}
				}
			}
		}
	}
    
    public void clearMap() {
    	if (_map != null) {
			_map.clear();
		}
    }
    
    public void toggleLeftMenu() {
    	_rootView.toggle();
	}
    
    public void showMenuContent() {
		_progressBar.setVisibility(View.INVISIBLE);
		_listView.setVisibility(View.VISIBLE);
		_editText.setEnabled(true);
		_menuBusFilter.setEnabled(true);
		_menuTrolleyFilter.setEnabled(true);
		_menuTramFilter.setEnabled(true);
		updateListView();
	}
    
	@SuppressLint("UseValueOf")
	public static float _distFrom(LatLng point1, LatLng point2) {
	    double earthRadius = 3958.75;
	    double dLat = Math.toRadians(point2.latitude - point1.latitude);
	    double dLng = Math.toRadians(point2.longitude - point1.longitude);
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	               Math.cos(Math.toRadians(point1.latitude)) * Math.cos(Math.toRadians(point2.latitude)) *
	               Math.sin(dLng/2) * Math.sin(dLng/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist = earthRadius * c;

	    int meterConversion = 1609;

	    return new Float(dist * meterConversion).floatValue();
	}
	
	@SuppressLint("NewApi")
	public void updateTransport() {
		if (_model.getFavorite().size() == 0) {
    		if (_map != null) {
    			View mainFrame = findViewById(R.id.mainFrame);
        		LatLngBounds bounds = _map.getProjection().getVisibleRegion().latLngBounds;
                _model.loadImg(bounds, mainFrame.getWidth(), mainFrame.getHeight());
			}
		} else {
			_model.showFavoriteRoutes();
		}
	}
	
	public void updateTransportOffline() {
    	if (_model.getFavorite().size() == 0) {
    		if (_map != null) {
    			if (_model.isOnline()) {
    				View mainFrame = findViewById(R.id.mainFrame);
            		LatLngBounds bounds = _map.getProjection().getVisibleRegion().latLngBounds;
                    _model.loadImg(bounds, mainFrame.getWidth(), mainFrame.getHeight());
				} else {
					showTransportImgOnMap();
				}
			}
		} else {
			showTransportListOnMap();
		}
	}
	
	public void updateListView() {
		Adapter adapter = (Adapter)_listView.getAdapter();
		if (adapter != null) {
			adapter.getFilter().filterByCurrentPrams();
		}
	}
	
	private TransportOverlay _getTransportOverlayById(int id) {
		TransportOverlay findTransportOverlay = null;
		for (TransportOverlay transportOverlay : _model.getAllTransportOverlays()) {
			if (transportOverlay.transport.id == id) {
				findTransportOverlay = transportOverlay;
				break;
			}
		}
		return findTransportOverlay;
	}
	
	private BitmapDescriptor _getBusBitMap(TransportKind kind) {
		int resId  = -1;
		switch (kind) {
		case Bus:
			resId = R.drawable.bus;
			break;
			
		case Trolley:
			resId = R.drawable.trolley;
			break;
			
		case Tram:
			resId = R.drawable.tram;
			break;

		default:
			break;
		}
		return BitmapDescriptorFactory.fromResource(resId);
	}
	
	private BitmapDescriptor _getRouteNumberBitMap(String routeNumber) {
		int height = _model.dpToPx(20);
		int fontSize = _model.dpToPx(11);
		
		
		Paint paintHighlight = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintHighlight.setColor(Color.argb(0xff, 0x45, 0x45, 0x45));
		int textWidth = Math.round(_model.dpToPx((int)paintHighlight.measureText(routeNumber)));
		int leftRightMargin = 0;
		Bitmap bitmap = Bitmap.createBitmap(textWidth + leftRightMargin * 2, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		
		int topBottomMargin = 0;
		canvas.drawRect(leftRightMargin, topBottomMargin, textWidth - leftRightMargin, topBottomMargin + fontSize, paintHighlight);
		
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setTextAlign(Align.CENTER);
		paint.setColor(Color.WHITE);
		paint.setTextSize(fontSize);
		canvas.drawText(routeNumber, textWidth / 2, height / 2 - _model.dpToPx(1), paint);
		
		return BitmapDescriptorFactory.fromBitmap(bitmap);
	}
	
	private float _getWidth() {
		float zoom = 0;
		if (_map != null) {
			zoom = _map.getCameraPosition().zoom;
		}
		double w = .5 * Math.pow(2, Math.max(.0, 21.0 - zoom));
		return (float)w;
	}

	public void showTransportListOnMap(ArrayList<Transport> array) {
		if (_map != null) {
			if (array != null) {
				for (Transport transport : array) {
					LatLng position = new LatLng(transport.Lat, transport.Lng);
					TransportOverlay transportOverlay = _getTransportOverlayById(transport.id);
					String velocity = getString(R.string.velocity) + Integer.toString(transport.velocity) + getString(R.string.km);
					if (transportOverlay == null) {
						transportOverlay = new TransportOverlay();
						_model.getAllTransportOverlays().add(transportOverlay);
					} else {
						if (transportOverlay.groundOverlay != null) {
							transportOverlay.groundOverlay.remove();
						}
						if (transportOverlay.marker != null) {
							transportOverlay.marker.remove();
						}
					}
					transportOverlay.groundOverlay = _map.addGroundOverlay(new GroundOverlayOptions().image(_getBusBitMap(transport.kind)).position(position, _getWidth()).bearing(transport.direction));
					transportOverlay.marker = _map.addMarker(new MarkerOptions().position(position).title(velocity).icon(_getRouteNumberBitMap(transport.routeNumber)));
					transportOverlay.transport = transport;
				}
			}
		}
	}
	public void showTransportListOnMap() {
		if (_map != null) {
			for (TransportOverlay transportOverlay : _model.getAllTransportOverlays()) {
				Transport transport = transportOverlay.transport;
				LatLng position = new LatLng(transport.Lat, transport.Lng);
				if (transportOverlay.groundOverlay != null) {
					transportOverlay.groundOverlay.remove();
				}
				if (transportOverlay.marker != null) {
					transportOverlay.marker.remove();
				}
				
				GroundOverlay groundOverlay = _map.addGroundOverlay(new GroundOverlayOptions().image(_getBusBitMap(transport.kind)).position(position, _getWidth()).bearing(transport.direction));
				Marker marker = _map.addMarker(new MarkerOptions().position(position).title("").icon(_getRouteNumberBitMap(transport.routeNumber)));
				transportOverlay.groundOverlay = groundOverlay;
				transportOverlay.marker = marker;
			}
		}
	}
	
	private int _countShows = 0;
	public void showTransportImgOnMap(Bitmap img) {
		if (img != null) {
			if (_map != null) {
				_model.removeSimpleTransportOverlay();
				BitmapDescriptor image = BitmapDescriptorFactory.fromBitmap(img);
		        LatLngBounds bounds = _map.getProjection().getVisibleRegion().latLngBounds;
		        GroundOverlay overlay = _map.addGroundOverlay(new GroundOverlayOptions()
		            .image(image)
		            .positionFromBounds(bounds));
		        _model.setSimpleTransportOverlay(overlay);
		        _model.setLastSimpleTransportView(new SimpleTransportView(img, bounds));
		        
		        if (!_model.openAnimationIsShowed() && _timer != null && _countShows > 3 && !_model.menuIsOpenedOnce() && !_rootView.hasTouches()) {
					_model.setOpenAnimationIsShowed();
					_rootView.animateOpen(_model.dpToPx(100));
				}
		        _countShows++;
			}
		}
	}
	public void showTransportImgOnMap() {
		if (_map != null) {
			_model.removeSimpleTransportOverlay();
			SimpleTransportView lastSimpleTransportView = _model.getLastSimpleTransportView();
			if (lastSimpleTransportView != null) {
				GroundOverlay overlay = _map.addGroundOverlay(new GroundOverlayOptions()
	            .image(BitmapDescriptorFactory.fromBitmap(lastSimpleTransportView.image))
	            .positionFromBounds(lastSimpleTransportView.getBounds()));
				_model.setSimpleTransportOverlay(overlay);
			}
	        
	        if (!_model.openAnimationIsShowed() && _timer != null && _countShows > 3 && !_model.menuIsOpenedOnce() && !_rootView.hasTouches()) {
				_model.setOpenAnimationIsShowed();
				_rootView.animateOpen(_model.dpToPx(100));
			}
	        _countShows++;
		}
	}
}