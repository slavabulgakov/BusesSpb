package ru.slavabulgakov.busesspb;

import ru.slavabulgakov.busesspb.controller.Controller;
import ru.slavabulgakov.busesspb.controller.MapState;
import ru.slavabulgakov.busesspb.controls.CheckButton;
import ru.slavabulgakov.busesspb.controls.CloselessTicketsTray;
import ru.slavabulgakov.busesspb.controls.InternetDenyImageButtonController;
import ru.slavabulgakov.busesspb.controls.LeftMenu;
import ru.slavabulgakov.busesspb.controls.MapController;
import ru.slavabulgakov.busesspb.controls.RightMenu;
import ru.slavabulgakov.busesspb.controls.RootView;
import ru.slavabulgakov.busesspb.model.Model.MenuKind;
import ru.slavabulgakov.busesspb.model.TransportKind;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

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
	private RootView _rootView;
	private CheckButton _busFilter;
	private CheckButton _trolleyFilter;
	private CheckButton _tramFilter;
	private CheckButton _shipFilter;
	
	LinearLayout _ticketsLayout;
	private LeftMenu _leftMenu;
	private RightMenu _rightMenu;
	private InternetDenyImageButtonController _internetDenyImageButtonController;
	private AdView _adView;
	private CheckButton _pathsButton;
	private CloselessTicketsTray _closelessTicketsTray;
	
    @SuppressLint("NewApi")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        _rootView = (RootView)findViewById(R.id.mainMapLayout);
		_rootView.setOnOpenListener(Controller.getInstance());
		_rootView.setModel(_model);
        
		_busFilter = (CheckButton)findViewById(R.id.busFilter);
		_busFilter.setOnClickListener(Controller.getInstance());
		
		_trolleyFilter = (CheckButton)findViewById(R.id.trolleyFilter);
		_trolleyFilter.setOnClickListener(Controller.getInstance());
		
		_tramFilter = (CheckButton)findViewById(R.id.tramFilter);
		_tramFilter.setOnClickListener(Controller.getInstance());
		
		_shipFilter = (CheckButton)findViewById(R.id.shipFilter);
		_shipFilter.setOnClickListener(Controller.getInstance());
		
		_pathsButton = (CheckButton)findViewById(R.id.paths);
		_pathsButton.setOnClickListener(Controller.getInstance());
		_pathsButton.setChecked(_model.getModelPaths().pathsIsOn());

		
		((ImageButton)findViewById(R.id.location)).setOnClickListener(Controller.getInstance());
		((ImageButton)findViewById(R.id.plus)).setOnClickListener(Controller.getInstance());
		((ImageButton)findViewById(R.id.minus)).setOnClickListener(Controller.getInstance());
		
		
		((ImageButton)findViewById(R.id.about)).setOnClickListener(Controller.getInstance());
		
		_leftMenu = (LeftMenu)findViewById(R.id.leftMenu);
		_leftMenu.setModel(_model);
		_leftMenu.getTicketsTray().inition(_model, Controller.getInstance());
		_leftMenu.getTicketsTray().update();
		
		updateFilterButtons();
		
		_rightMenu = (RightMenu)findViewById(R.id.rightMenu);
		_rightMenu.setModel(_model);
        _rightMenu.setListener(Controller.getInstance());
		
		_internetDenyImageButtonController = new InternetDenyImageButtonController((ImageButton)findViewById(R.id.internetDeny), _model, this);
		
		_adView = (AdView)findViewById(R.id.mainAdView);
		
		_closelessTicketsTray = (CloselessTicketsTray)findViewById(R.id.closelessTicketsTray);
		_closelessTicketsTray.setOnClickListener(Controller.getInstance());
		_closelessTicketsTray.inition(_model);
    }
    
    public CloselessTicketsTray getCloselessTicketsTray() {
    	return _closelessTicketsTray;
    }
    
    public InternetDenyImageButtonController getInternetDenyButtonController() {
		return _internetDenyImageButtonController;
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
    
    
	@Override
	protected void onPause() {
		_model.getModelPaths().save();
		_model.saveAllTransportOverlays();
		_model.saveLastSimpleTransportView();
		_model.saveFavorite();
		_model.saveLocation();
		_model.saveZoom();
		Controller.getInstance().getState().pause();
		super.onPause();
	}


	@Override
	protected void onResume() {
		super.onResume();
		_updateBottomControls();
		
		GoogleMap map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		_mapController = new MapController(map, _model, Controller.getInstance());
		_mapController.onResume();
		
		_mapController.toggleRotateMap(_model.menuIsOpened(MenuKind.Left));
		
		if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
			_busFilter.setVisibility(View.GONE);
			_trolleyFilter.setVisibility(View.GONE);
			_tramFilter.setVisibility(View.GONE);
			_shipFilter.setVisibility(View.GONE);
			_leftMenu.setFiltersButtonsVisibility(false);
			_closelessTicketsTray.setVisibility(View.GONE);
			_leftMenu.setInputVisible(false);
			_leftMenu.setProgressBarVisible(false);
			((ImageButton)findViewById(R.id.about)).setVisibility(View.GONE);
			((LinearLayout)findViewById(R.id.zoomControls)).setVisibility(View.GONE);
			((ImageButton)findViewById(R.id.location)).setVisibility(View.GONE);
		} else {
			_busFilter.setVisibility(View.VISIBLE);
			_trolleyFilter.setVisibility(View.VISIBLE);
			_tramFilter.setVisibility(View.VISIBLE);
			_shipFilter.setVisibility(View.VISIBLE);
			_leftMenu.setFiltersButtonsVisibility(true);
			_closelessTicketsTray.setVisibility(View.VISIBLE);
			_leftMenu.setInputVisible(true);
			((ImageButton)findViewById(R.id.about)).setVisibility(View.VISIBLE);
			_leftMenu.setProgressBarVisible(true);
			((LinearLayout)findViewById(R.id.zoomControls)).setVisibility(View.VISIBLE);
			((ImageButton)findViewById(R.id.location)).setVisibility(View.VISIBLE);
		}
		
		if (_model.isOnline()) {
			_model.removeAllTransportOverlays();
		}
		
		updateControls();
		Controller.getInstance().switchToState(new MapState());
		
		if (_model.allRouteIsLoaded()) {
    		_leftMenu.showMenuContent();
		}
	}

    public void updateControls() {
    	_closelessTicketsTray.update();
    	
    	if (!_model.menuIsOpened(MenuKind.Left)) {
    		_model.getModelPaths().loadPaths();
		}
	}
    
    public void keyboardTurnOff() {
    	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(_leftMenu.getInput().getWindowToken(), 0);
    }
    
    public void toggleMenu(MenuKind kind) {
    	_rootView.toggleMenu(kind);
	}
    
    public void showMenu(MenuKind kind) {
		_rootView.open(kind);
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
	
	public void runReopenAnimation() {
		if (!_model.openAnimationIsShowed() && Controller.getInstance().getState().getClass() == MapState.class && _mapController.getCountShows() > 3 && !_model.menuIsOpenedOnce() && !_rootView.hasTouches()) {
			_model.setOpenAnimationIsShowed();
			_rootView.animateOpen(_model.dpToPx(100));
		}
	}
	
	public RightMenu getRightMenu() {
		return _rightMenu;
	}
	
	public LeftMenu getLeftMenu() {
    	return _leftMenu;
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
		
		_leftMenu.updateFilterButtons();
	}
}