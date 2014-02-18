package ru.slavabulgakov.busesspb;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLngBounds;

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

public class MainActivity extends BaseActivity {
	
	private MapController _mapController;
	private RootView _rootView;
	private CheckButton _busFilter;
	private CheckButton _trolleyFilter;
	private CheckButton _tramFilter;
	private CheckButton _shipFilter;
	
	private LeftMenu _leftMenu;
	private RightMenu _rightMenu;
	private InternetDenyImageButtonController _internetDenyImageButtonController;
	private AdView _adView;
	private CheckButton _pathsButton;
	private CloselessTicketsTray _closelessTicketsTray;
    private FrameLayout _rightMenuButton;
    private LocationClient _locationClient;
    private ImageView _rightMenuButtonImage;
	
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

        _rightMenuButton = (FrameLayout)findViewById(R.id.rightMenuButton);
        _rightMenuButton.setOnClickListener(Controller.getInstance());

		_pathsButton = (CheckButton)findViewById(R.id.paths);
		_pathsButton.setOnClickListener(Controller.getInstance());
		_pathsButton.setChecked(_model.getModelPaths().pathsIsOn());
        _pathsButton.setVisibility(View.INVISIBLE);

		
		findViewById(R.id.location).setOnClickListener(Controller.getInstance());
		findViewById(R.id.plus).setOnClickListener(Controller.getInstance());
		findViewById(R.id.minus).setOnClickListener(Controller.getInstance());
		
		
		findViewById(R.id.about).setOnClickListener(Controller.getInstance());

		_leftMenu = (LeftMenu)findViewById(R.id.leftMenu);
		_leftMenu.setModel(_model);
		_leftMenu.getTicketsTray().inition(_model, Controller.getInstance());
		_leftMenu.getTicketsTray().update();
		
		updateFilterButtons();
		
		_rightMenu = (RightMenu)findViewById(R.id.rightMenu);
		_rightMenu.setModel(_model);

		_internetDenyImageButtonController = new InternetDenyImageButtonController((ImageButton)findViewById(R.id.internetDeny), _model, this);
		
		_adView = (AdView)findViewById(R.id.mainAdView);
		
		_closelessTicketsTray = (CloselessTicketsTray)findViewById(R.id.closelessTicketsTray);
		_closelessTicketsTray.setOnClickListener(Controller.getInstance());
		_closelessTicketsTray.inition(_model);

        _locationClient = new LocationClient(this, Controller.getInstance(), Controller.getInstance());
        _rightMenuButtonImage = (ImageView)findViewById(R.id.rightMenuButtonImage);
        setRightMenuButtonLoading(false);

        Controller.getInstance().switchToState(new MapState());
    }

    public void setRightMenuButtonLoading(final boolean loading) {
        Controller.getInstance().getHandler().post(new Runnable() {
            @Override
            public void run() {
                ProgressBar progressBar = (ProgressBar)findViewById(R.id.rightMenuButtonProgressBar);
                if (loading) {
                    progressBar.setVisibility(View.VISIBLE);
                    _rightMenuButtonImage.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.GONE);
                    _rightMenuButtonImage.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        _locationClient.connect();
    }

    @Override
    protected void onStop() {
        _locationClient.disconnect();
        super.onStop();
    }

    public Location getLocation() {
        return _locationClient.getLastLocation();
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
		_adView.setVisibility(isAdsOff() ? View.GONE : View.VISIBLE);
    }
    
    @Override
    protected void purchaseDidCheck(boolean hasPurchase) {
    	super.purchaseDidCheck(hasPurchase);
    	_updateBottomControls();
    }
    
    
	@Override
	protected void onPause() {
		_model.saveAllTransportOverlays();
		_model.saveLastSimpleTransportView();
		_model.saveFavorite();
		_model.saveLocation();
		_model.saveZoom();
        if (Controller.getInstance().getState() != null) {
            Controller.getInstance().getState().pause();
        }
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
			_rightMenuButton.setVisibility(View.GONE);
			findViewById(R.id.zoomControls).setVisibility(View.GONE);
			findViewById(R.id.location).setVisibility(View.GONE);
		} else {
			_busFilter.setVisibility(View.VISIBLE);
			_trolleyFilter.setVisibility(View.VISIBLE);
			_tramFilter.setVisibility(View.VISIBLE);
			_shipFilter.setVisibility(View.VISIBLE);
			_leftMenu.setFiltersButtonsVisibility(true);
			_closelessTicketsTray.setVisibility(View.VISIBLE);
			_leftMenu.setInputVisible(true);
			_leftMenu.setProgressBarVisible(true);
			findViewById(R.id.zoomControls).setVisibility(View.VISIBLE);
			findViewById(R.id.location).setVisibility(View.VISIBLE);
		}
		
		if (_model.isOnline()) {
			_model.removeAllTransportOverlays();
		}
		
		updateControls();
        if (Controller.getInstance().getState() != null) {
            Controller.getInstance().getState().resume();
        } else {
            Controller.getInstance().switchToState(new MapState());
        }

        if (_model.allRouteIsLoaded()) {
    		_leftMenu.showMenuContent();
		}
	}

    public void updateControls() {
    	_closelessTicketsTray.update();
    	
    	if (_model.menuIsClosed(MenuKind.Left)) {
    		_model.getModelPaths().updateStationsAndPaths();
		}

        int resId = _model.menuIsOpened(MenuKind.Right) ? R.drawable.menu_open_icon : R.drawable.nearby_stations;
        _rightMenuButtonImage.setImageResource(resId);
    }
    
    public void keyboardTurnOff() {
    	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(_leftMenu.getInput().getWindowToken(), 0);
    }
    
    public void toggleMenu(MenuKind kind) {
    	_rootView.toggleMenu(kind);
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
        _busFilter.setChecked(_model.isEnabledFilter(TransportKind.Bus));
        _trolleyFilter.setChecked(_model.isEnabledFilter(TransportKind.Trolley));
        _tramFilter.setChecked(_model.isEnabledFilter(TransportKind.Tram));
        _shipFilter.setChecked(_model.isEnabledFilter(TransportKind.Ship));

        _leftMenu.updateFilterButtons();

		LinearLayout kindBtns = (LinearLayout)findViewById(R.id.kindBtns);
        TranslateAnimation animation;
        final boolean showButton = _model.getFavorite().size() > 0;
		if (showButton) {
            if (_pathsButton.getVisibility() == View.VISIBLE) {
                return;
            }
			kindBtns.setVisibility(View.INVISIBLE);
            animation = new TranslateAnimation(_model.dpToPx(50), 0, 0, 0);
        } else {
            if (_pathsButton.getVisibility() == View.INVISIBLE) {
                return;
            }
            animation = new TranslateAnimation(0, _model.dpToPx(50), 0, 0);
			kindBtns.setVisibility(View.VISIBLE);
		}
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (showButton) {
                    _pathsButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (!showButton) {
                    _pathsButton.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        animation.setDuration(500);
        _pathsButton.startAnimation(animation);
	}
}