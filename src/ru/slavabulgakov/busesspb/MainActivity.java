package ru.slavabulgakov.busesspb;

import java.util.Timer;
import ru.slavabulgakov.busesspb.CloseAllTickets.OnAnimationEndListener;
import ru.slavabulgakov.busesspb.controls.MapController;
import ru.slavabulgakov.busesspb.model.Model.MenuKind;
import ru.slavabulgakov.busesspb.model.Route;
import ru.slavabulgakov.busesspb.model.TransportKind;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
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

import com.flurry.android.FlurryAgent;
import com.google.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.SupportMapFragment;
import android.view.WindowManager;

public class MainActivity extends BaseActivity {
	
	private MapController _mapController;
	private Timer _timer;
	private ListView _listView;
	private EditText _editText;
	private ProgressBar _progressBar;
	private RootView _rootView;
	private CheckButton _busFilter;
	private CheckButton _trolleyFilter;
	private CheckButton _tramFilter;
	private CheckButton _shipFilter;
	private CheckButton _menuBusFilter;
	private CheckButton _menuTrolleyFilter;
	private CheckButton _menuTramFilter;
	private CheckButton _menuShipFilter;
	private RelativeLayout _mainRoutesBtn;
	private ImageButton _clearButton;
	LinearLayout _ticketsLayout;
	private LinearLayout _leftMenu;
	private RightMenu _rightMenu;
	private ImageView _menuIcon;
	private ImageButton _internetDenyImageButton;
	private AdView _adView;
	private CheckButton _pathsButton;
	
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
		_editText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
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
		
		
		
		
		_busFilter = (CheckButton)findViewById(R.id.busFilter);
		_busFilter.setOnClickListener(Contr.getInstance());
		
		_trolleyFilter = (CheckButton)findViewById(R.id.trolleyFilter);
		_trolleyFilter.setOnClickListener(Contr.getInstance());
		
		_tramFilter = (CheckButton)findViewById(R.id.tramFilter);
		_tramFilter.setOnClickListener(Contr.getInstance());
		
		_shipFilter = (CheckButton)findViewById(R.id.shipFilter);
		_shipFilter.setOnClickListener(Contr.getInstance());
		
		_menuBusFilter = (CheckButton)findViewById(R.id.menuBusFilter);
    	_menuBusFilter.setOnClickListener(Contr.getInstance());
    	
    	_menuTrolleyFilter = (CheckButton)findViewById(R.id.menuTrolleyFilter);
		_menuTrolleyFilter.setOnClickListener(Contr.getInstance());
		
		_menuTramFilter = (CheckButton)findViewById(R.id.menuTramFilter);
		_menuTramFilter.setOnClickListener(Contr.getInstance());
		
		_menuShipFilter = (CheckButton)findViewById(R.id.menuShipFilter);
		_menuShipFilter.setOnClickListener(Contr.getInstance());
		
		_pathsButton = (CheckButton)findViewById(R.id.paths);
		_pathsButton.setOnClickListener(Contr.getInstance());
		_pathsButton.setChecked(_model.getModelPaths().pathsIsOn());
		
		updateFilterButtons();
		
		
		
		
		
		((ImageButton)findViewById(R.id.location)).setOnClickListener(Contr.getInstance());
		((ImageButton)findViewById(R.id.plus)).setOnClickListener(Contr.getInstance());
		((ImageButton)findViewById(R.id.minus)).setOnClickListener(Contr.getInstance());
		
		
		((ImageButton)findViewById(R.id.about)).setOnClickListener(Contr.getInstance());
		
		_leftMenu = (LinearLayout)findViewById(R.id.leftMenu);
		_rightMenu = (RightMenu)findViewById(R.id.rightMenu);
		_rightMenu.setModel(_model);
		_menuIcon = ((ImageView)findViewById(R.id.menuIcon));
		
		if (_listView.getAdapter() == null) {
			Adapter adapter = new Adapter(this, _model);
			_listView.setAdapter(adapter);
			adapter.getFilter().filter(_editText.getText());
		}
		
		_internetDenyImageButton = (ImageButton)findViewById(R.id.internetDeny);
		_internetDenyImageButton.setOnClickListener(Contr.getInstance());
		
		_internetDenyImageButton.setVisibility(_internetDenyIconIsShowed() ? View.VISIBLE : View.INVISIBLE);
		
		_adView = (AdView)findViewById(R.id.mainAdView);
    }
    
    public MapController getMapController() {
    	return _mapController;
    }
    
    public CheckButton pathsButton() {
    	return _pathsButton;
    }
    
    private void _updateBottomControls() {
    	Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		int width = display.getWidth();
		width = _model.pxToDp(width);
		if (width < 400) {
			LinearLayout zoom = (LinearLayout)findViewById(R.id.zoomControls);
			RelativeLayout.LayoutParams zoomLayoutParams = (LayoutParams)zoom.getLayoutParams();
			zoomLayoutParams.bottomMargin = _model.dpToPx(isAdsOff() ? 10 : 60);
			zoom.setLayoutParams(zoomLayoutParams);
		}
		
		_adView.setVisibility(isAdsOff() ? View.GONE : View.VISIBLE);
    }
    
    @Override
    protected void purchaseDidCheck(boolean hasPurchase) {
    	super.purchaseDidCheck(hasPurchase);
    	_updateBottomControls();
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
    	if (percent > 0) {
    		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)_leftMenu.getLayoutParams();
        	lp.setMargins((int)(_model.dpToPx(-delta + delta * percent)), 0, 0, 0);
        	_leftMenu.setLayoutParams(lp);
        	
        	RelativeLayout.LayoutParams lpRight = (RelativeLayout.LayoutParams)_rightMenu.getLayoutParams();
        	lpRight.setMargins(0, 0, (int)(_model.dpToPx(-200)), 0);
	    	_rightMenu.setLayoutParams(lpRight);
		} else {
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)_rightMenu.getLayoutParams();
	    	lp.setMargins(0, 0, (int)(_model.dpToPx(-delta + delta * Math.abs(percent))), 0);
	    	_rightMenu.setLayoutParams(lp);
		}
    }
    
    public void updateFilterButtons() {
    	LinearLayout kindBtns = (LinearLayout)findViewById(R.id.kindBtns);
    	if (_model.getFavorite().size() > 0) {
			kindBtns.setVisibility(View.INVISIBLE);
			_pathsButton.setVisibility(View.VISIBLE);
		} else {
			kindBtns.setVisibility(View.VISIBLE);
			_pathsButton.setVisibility(View.INVISIBLE);
		}
    	_busFilter.setChecked(_model.isEnabledFilter(TransportKind.Bus));
    	_trolleyFilter.setChecked(_model.isEnabledFilter(TransportKind.Trolley));
    	_tramFilter.setChecked(_model.isEnabledFilter(TransportKind.Tram));
    	_shipFilter.setChecked(_model.isEnabledFilter(TransportKind.Ship));
		
		_menuBusFilter.setChecked(_model.isEnabledFilterMenu(TransportKind.Bus));
		_menuTrolleyFilter.setChecked(_model.isEnabledFilterMenu(TransportKind.Trolley));
		_menuTramFilter.setChecked(_model.isEnabledFilterMenu(TransportKind.Tram));
		_menuShipFilter.setChecked(_model.isEnabledFilterMenu(TransportKind.Ship));
    }
    
	@Override
	protected void onPause() {
		_model.getModelPaths().save();
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
		super.onResume();
		_updateBottomControls();
		
		GoogleMap map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		_mapController = new MapController(map, _model, Contr.getInstance());
		_mapController.onResume();
		
		_mapController.toggleRotateMap(_model.menuIsOpened(MenuKind.Left));
		
		if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
			_busFilter.setVisibility(View.GONE);
			_trolleyFilter.setVisibility(View.GONE);
			_tramFilter.setVisibility(View.GONE);
			_shipFilter.setVisibility(View.GONE);
			_menuBusFilter.setVisibility(View.GONE);
			_menuTrolleyFilter.setVisibility(View.GONE);
			_menuTramFilter.setVisibility(View.GONE);
			_menuShipFilter.setVisibility(View.GONE);
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
			_shipFilter.setVisibility(View.VISIBLE);
			_menuBusFilter.setVisibility(View.VISIBLE);
			_menuTrolleyFilter.setVisibility(View.VISIBLE);
			_menuTramFilter.setVisibility(View.VISIBLE);
			_menuShipFilter.setVisibility(View.VISIBLE);
			((RelativeLayout)findViewById(R.id.mainRoutesBtn)).setVisibility(View.VISIBLE);
			_editText.setVisibility(View.VISIBLE);
			((ImageButton)findViewById(R.id.about)).setVisibility(View.VISIBLE);
			_progressBar.setVisibility(View.VISIBLE);
			((LinearLayout)findViewById(R.id.zoomControls)).setVisibility(View.VISIBLE);
			((ImageButton)findViewById(R.id.location)).setVisibility(View.VISIBLE);
		}
		
		if (_model.isOnline()) {
			_model.removeAllTransportOverlays();
		}
		
		_updateControls();
	}

	public void putCloseAllButtonToTicketsLayout() {
		if (_model.getFavorite().size() > 1) {
			if (_ticketsLayout.getChildAt(0).getClass() != CloseAllTickets.class) {
				CloseAllTickets closeAllBtn = new CloseAllTickets(this, _model);
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
    
    public void updateTimer() {
    	if (_timer != null) {
			_timer.cancel();
			_timer = null;
		}
    	if (_model.isOnline()) {
    		if (_model.menuIsOpened(MenuKind.Left) && !_model.allRouteIsLoaded()) {
    			_timer = new Timer();
    			_timer.schedule(new UpdateMenuContentTimerTask(this, _model), 0, 3000);
			} else if (!_model.menuIsOpened(MenuKind.Left)) {
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
		_menuShipFilter.setEnabled(true);
		_model.loadDataForAllRoutes();
    }
    
    private void _updateControls() {
    	
    	int resId = _model.menuIsOpened(MenuKind.Left) ? R.drawable.menu_close_icon : R.drawable.menu_open_icon;
		_menuIcon.setImageResource(resId);
    	
    	if (_model.allRouteIsLoaded()) {
    		showMenuContent();
		}
    	
    	
    	updateTimer();
    	
    	
    	
    	
    	{// ���������� ������� ��������� ���������
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
			lp.setMargins(lp.leftMargin, lp.topMargin, (_model.getFavorite().size() > 0 ? _model.dpToPx(60) : 0), lp.bottomMargin);
			_mainRoutesBtn.setLayoutParams(lp);
			_mainRoutesBtn.setPadding(_mainRoutesBtn.getPaddingLeft(), _mainRoutesBtn.getPaddingTop(), (_model.getFavorite().size() > 0 ? _model.dpToPx(5) : 0), _mainRoutesBtn.getPaddingBottom());
			
			RelativeLayout.LayoutParams lpMenuIcon = (RelativeLayout.LayoutParams)_menuIcon.getLayoutParams();
			lpMenuIcon.setMargins(lpMenuIcon.leftMargin, lpMenuIcon.topMargin, (_model.getFavorite().size() > 0 ? 0 : _model.dpToPx(8)), lpMenuIcon.bottomMargin);
			_menuIcon.setLayoutParams(lpMenuIcon);
		}
    	
    	if (!_model.menuIsOpened(MenuKind.Left)) {
    		_model.getModelPaths().loadPaths();
		}
	}
    
    public void keyboardTurnOff() {
    	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(_editText.getWindowToken(), 0);
    }
    
    public void rightMenuChangeState(boolean isOpen) {
    	if (isOpen && _model.getRoutesNames().size() == 0) {
    		_model.loadRoutesNames();
		}
    }
    
    public void menuChangeState(boolean isOpen) {
    	
    	
		_updateControls();
		updateFilterButtons();
		
		
		{// ���������� ����������
			if (!isOpen) {
				keyboardTurnOff();
			}
		}
		
		
		
		
		
		_mapController.toggleRotateMap(isOpen);
		
		
		if (isOpen) {
			FlurryAgent.logEvent(FlurryConstants.menuIsOpen);
		} else {
			if (_model.getFavorite().size() > 0) {
				FlurryAgent.logEvent(FlurryConstants.selectedTransportModeIsOn);
			} else {
				FlurryAgent.logEvent(FlurryConstants.selectedTransportModeIsOff);
			}
		}
		
		
		{// ������� �����
			if (!isOpen) {
				if (_mapController != null) {
					_model.removeSimpleTransportOverlay();
					if (_model.isOnline()) {
						_model.removeAllTransportOverlays();
						_model.removeLastSimpleTransportView();
						updateTransport();
					} else {
						updateTransportOffline();
					}
				}
			}
		}
	}
    
    public void toggleMenu(MenuKind kind) {
    	_rootView.toggleMenu(kind);
	}
    
    public void showMenu(MenuKind kind) {
		_rootView.open(kind);
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
    		if (_mapController != null) {
    			View mainFrame = findViewById(R.id.mainFrame);
        		LatLngBounds bounds = _mapController.getMap().getProjection().getVisibleRegion().latLngBounds;
                _model.loadImg(bounds, _model.pxToDp(mainFrame.getWidth()), _model.pxToDp(mainFrame.getHeight()));
			}
		} else {
			_model.showFavoriteRoutes();
		}
	}
	
	public void updateTransportOffline() {
    	if (_model.getFavorite().size() == 0) {
    		if (_mapController != null) {
    			if (_model.isOnline()) {
    				View mainFrame = findViewById(R.id.mainFrame);
            		LatLngBounds bounds = _mapController.getMap().getProjection().getVisibleRegion().latLngBounds;
                    _model.loadImg(bounds,_model.pxToDp(mainFrame.getWidth()), _model.pxToDp(mainFrame.getHeight()));
				} else {
					_mapController.showTransportImgOnMap();
				}
			}
		} else {
			_mapController.showTransportListOnMap();
		}
	}
	
	public void updateListView() {
		Adapter adapter = (Adapter)_listView.getAdapter();
		if (adapter != null) {
			adapter.getFilter().filterByCurrentPrams();
		}
	}
	
	public void runReopenAnimation() {
		if (!_model.openAnimationIsShowed() && _timer != null && _mapController.getCountShows() > 3 && !_model.menuIsOpenedOnce() && !_rootView.hasTouches()) {
			_model.setOpenAnimationIsShowed();
			_rootView.animateOpen(_model.dpToPx(100));
		}
	}
	
	public RightMenu getRightMenu() {
		return _rightMenu;
	}
}