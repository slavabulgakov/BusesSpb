package ru.slavabulgakov.busesspb;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ru.slavabulgakov.busesspb.Model.TransportOverlay;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

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
		
		_listView = (ListView)findViewById(R.id.selectRouteListView);
		_listView.setOnItemClickListener(Contr.getInstance());
        
	    _mainRoutesBtn = (RelativeLayout)findViewById(R.id.mainRoutesBtn);
	    _mainRoutesBtn.setOnClickListener(Contr.getInstance());
	    
	    LinearLayout ticketsLayout = (LinearLayout)findViewById(R.id.selectRouteTickets);
		for (Route route : _model.getFavorite()) {
			Ticket ticket = new Ticket(this);
			ticket.setRoute(route);
			ticket.setOnRemoveListener(Contr.getInstance());
			ticketsLayout.addView(ticket);
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
		
		
		
		_map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		if (_map != null) {
			CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(59.946282, 30.356412));
	        _map.moveCamera(center);
	        CameraUpdate zoom = CameraUpdateFactory.zoomTo(10);
	        _map.animateCamera(zoom);
	        _map.setMyLocationEnabled(true);
	        _map.setOnCameraChangeListener(Contr.getInstance());
	        _map.getUiSettings().setMyLocationButtonEnabled(false);
	        _map.getUiSettings().setZoomControlsEnabled(false);
	        _updateControls();
		}
		
		((ImageButton)findViewById(R.id.location)).setOnClickListener(Contr.getInstance());
		((ImageButton)findViewById(R.id.plus)).setOnClickListener(Contr.getInstance());
		((ImageButton)findViewById(R.id.minus)).setOnClickListener(Contr.getInstance());
		
		
		((ImageButton)findViewById(R.id.about)).setOnClickListener(Contr.getInstance());
		
		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		int width = display.getWidth();
		if (width < 400) {
			LinearLayout zoom = (LinearLayout)findViewById(R.id.zoomControls);
			RelativeLayout.LayoutParams zoomLayoutParams = (LayoutParams)zoom.getLayoutParams();
			zoomLayoutParams.bottomMargin = _dpToPx(60);
			zoom.setLayoutParams(zoomLayoutParams);
		}
    }
    
    private int _dpToPx(int dp) {
		return (int)(getResources().getDisplayMetrics().density * dp);
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
    	if (_timer != null) {
    		_timer.cancel();
    		_timer = null;
		}
		super.onPause();
	}


	public void putCloseAllButtonToTicketsLayout() {
		LinearLayout ticketsLayout = (LinearLayout)findViewById(R.id.selectRouteTickets);
		if (_model.getFavorite().size() > 1) {
			if (ticketsLayout.getChildAt(0).getClass() != ImageButton.class) {
				ImageButton closeAllBtn = new ImageButton(this);
				closeAllBtn.setOnClickListener(Contr.getInstance());
				closeAllBtn.setImageResource(R.drawable.close);
				closeAllBtn.setBackgroundResource(R.drawable.buttons_bg);
				closeAllBtn.setTag("closeAllBtn");
				ticketsLayout.addView(closeAllBtn, 0);
			}
		} else {
			if (ticketsLayout.getChildCount() > 0) {
				if (ticketsLayout.getChildAt(0).getClass() == ImageButton.class) {
					ticketsLayout.removeViewAt(0);
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
    
    private void _updateControls() {
    	
    	{// ���������� �������� ����
    		if (_model.menuIsOpened()) {
    			if (_model.getAllRoutes().size() == 0) {
    				_progressBar.setVisibility(View.VISIBLE);
    				_listView.setVisibility(View.INVISIBLE);
    				_editText.setEnabled(false);
    				_busFilter.setEnabled(false);
    				_trolleyFilter.setEnabled(false);
    				_tramFilter.setEnabled(false);
    				_model.loadDataForAllRoutes(Contr.getInstance());
    			} else {
    				showTransportList();
    			}
    		}
    	}
    	
    	
    	
    	
    	{// ���������� �������
			if (_timer != null) {
    			_timer.cancel();
    			_timer = null;
			}
			if (!_model.menuIsOpened()) {
				_timer = new Timer();
		    	_timer.schedule(new TimerTask() {

						@Override
						public void run() {
							MainActivity.this.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									updateTransport();
								}
							});
						}
					}, 0, 5000);
			}
		}
    	
    	
    	
    	
    	{// ���������� ������� ��������� ���������
    		LinearLayout ticketsLayout = (LinearLayout)findViewById(R.id.mainRoutesScrollView);
	    	ticketsLayout.removeAllViews();
			for (Route route : _model.getFavorite()) {
				TicketCloseLess ticket = new TicketCloseLess(this);
				ticket.setRoute(route);
				ticketsLayout.addView(ticket);
			}
			
	    	HorizontalScrollView routesBtnScrollView = (HorizontalScrollView)findViewById(R.id.mainRoutesBtnScrollView);
	    	if (_model.getFavorite().size() > 0) {
	    		routesBtnScrollView.setVisibility(View.VISIBLE);
			} else {
				routesBtnScrollView.setVisibility(View.GONE);
			}
		}
	}
    
    private void _keyboardTurnOff() {
    	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(_editText.getWindowToken(), 0);
    }
    
    public void menuChangeState(boolean isOpen) {
    	
    	
		_updateControls();
		updateFilterButtons();
		
		
		{// ���������� ����������
			if (!isOpen) {
				_keyboardTurnOff();
				_model.saveFavorite();
			}
		}
		
		
		
		
		
		{// ����������/��������� �������� �����
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
		
		
		
		
		
		{// ������� �����
			if (!isOpen) {
				if (_map != null) {
					_map.clear();
				}
			}
		}
		
		
	}
    
    public void toggleLeftMenu() {
    	_rootView.toggle();
	}
    
    public void showTransportList() {
		_progressBar.setVisibility(View.INVISIBLE);
		_listView.setVisibility(View.VISIBLE);
		_editText.setEnabled(true);
		_busFilter.setEnabled(true);
		_trolleyFilter.setEnabled(true);
		_tramFilter.setEnabled(true);
		if (_listView.getAdapter() == null) {
			Adapter adapter = new Adapter(this, _model);
			_listView.setAdapter(adapter);
			adapter.getFilter().filter(_editText.getText());
		}
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
    		View mainFrame = findViewById(R.id.mainFrame);
    		LatLngBounds bounds = _map.getProjection().getVisibleRegion().latLngBounds;
            _model.loadImg(bounds, mainFrame.getWidth(), mainFrame.getHeight(), Contr.getInstance());
		} else {
			_model.showFavoriteRoutes(Contr.getInstance());
		}
	}
	
	private TransportOverlay _getTransportOverlayById(int id) {
		TransportOverlay findTransportOverlay = null;
		for (TransportOverlay transportOverlay : _model.getAllTransportOverlay()) {
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
		Bitmap bitmap = Bitmap.createBitmap(40, 30, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Resources resources = getResources();
		float scale = resources.getDisplayMetrics().density;
		
//		Paint paintGreen = new Paint(Paint.ANTI_ALIAS_FLAG);
//		paintGreen.setColor(Color.GREEN);
//		canvas.drawRect(0, 0, 40, 30, paintGreen);
		
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setTextAlign(Align.CENTER);
		paint.setColor(Color.BLACK);
		paint.setTextSize((int) (11 * scale));
		paint.setShadowLayer(4, 2, 2, Color.WHITE);
		canvas.drawText(routeNumber, 20, 15, paint);
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
					String velocity = "��������: " + Integer.toString(transport.velocity) + "��/���";
					if (transportOverlay == null) {
						GroundOverlay groundOverlay = _map.addGroundOverlay(new GroundOverlayOptions().image(_getBusBitMap(transport.kind)).position(position, _getWidth()).bearing(transport.direction));
						Marker marker = _map.addMarker(new MarkerOptions().position(position).title(velocity).icon(_getRouteNumberBitMap(transport.routeNumber)));
						transportOverlay = new TransportOverlay();
						transportOverlay.transport = transport;
						transportOverlay.groundOverlay = groundOverlay;
						transportOverlay.marker = marker;
						_model.getAllTransportOverlay().add(transportOverlay);
					} else {
						transportOverlay.groundOverlay.remove();
						transportOverlay.marker.remove();
						transportOverlay.groundOverlay = _map.addGroundOverlay(new GroundOverlayOptions().image(_getBusBitMap(transportOverlay.transport.kind)).position(position, _getWidth()).bearing(transport.direction));
						transportOverlay.marker = _map.addMarker(new MarkerOptions().position(position).title(velocity).icon(_getRouteNumberBitMap(transport.routeNumber)));
					}
				}
			}
		}
	}
	
	public void showTransportImgOnMap(Bitmap img) {
		if (img != null) {
			if (_map != null) {
				_map.clear();
				BitmapDescriptor image = BitmapDescriptorFactory.fromBitmap(img);
		        LatLngBounds bounds = _map.getProjection().getVisibleRegion().latLngBounds;
		        _map.addGroundOverlay(new GroundOverlayOptions()
		            .image(image)
		            .positionFromBounds(bounds));
			}
		}
	}
}