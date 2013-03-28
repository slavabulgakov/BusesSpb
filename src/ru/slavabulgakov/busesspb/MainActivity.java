package ru.slavabulgakov.busesspb;

import java.util.ArrayList;
import java.util.Timer;
import ru.slavabulgakov.busesspb.Model.TransportOverlay;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class MainActivity extends BaseActivity {
	
	private GoogleMap _map;
	private Timer _timer;
	private Boolean _opened = false;
	private ListView _listView;
	private EditText _editText;
	private ProgressBar _progressBar;

    @SuppressLint("NewApi")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
//        _map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
//        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(59.946282, 30.356412));
//        _map.moveCamera(center);
//        CameraUpdate zoom = CameraUpdateFactory.zoomTo(10);
//        _map.animateCamera(zoom);
//        _map.setMyLocationEnabled(true);
//        _map.setOnCameraChangeListener(Contr.getInstance());
        
        _progressBar = (ProgressBar)findViewById(R.id.selectRouteProgressBar);
		
		_editText = (EditText)findViewById(R.id.selectRouteText);
		_editText.addTextChangedListener(Contr.getInstance());
		
		_listView = (ListView)findViewById(R.id.selectRouteListView);
		if (_model.getAllRoutes().size() == 0) {
			_progressBar.setVisibility(View.VISIBLE);
			_listView.setVisibility(View.INVISIBLE);
			_editText.setEnabled(false);
			_model.loadDataForAllRoutes(Contr.getInstance());
		} else {
			showTransportList();
		}
		_listView.setOnItemClickListener(Contr.getInstance());
        
        final RelativeLayout mapLayout = (RelativeLayout)findViewById(R.id.mainMapLayout);
	    RelativeLayout btn = (RelativeLayout)findViewById(R.id.mainRoutesBtn);
	    btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TranslateAnimation ta = new TranslateAnimation(0, _opened ? -200 : 200, 0, 0);
				ta.setDuration(400);
				ta.setAnimationListener(new AnimationListener() {
					
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
						RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mapLayout.getLayoutParams());
						lp.leftMargin = _opened ? 0 : 200;
						lp.rightMargin = _opened ? 0 : -200;
						mapLayout.setLayoutParams(lp);
						mapLayout.setAnimation(null);
						_opened = !_opened;
					}
				});
				mapLayout.startAnimation(ta);
			}
		});
    }
    
    public void showTransportList() {
		_progressBar.setVisibility(View.INVISIBLE);
		_listView.setVisibility(View.VISIBLE);
		_editText.setEnabled(true);
		Adapter adapter = new Adapter(this, _model);
		_listView.setAdapter(adapter);
		adapter.getFilter().filter(_editText.getText());
	}
    
    @Override
	protected void onResume() {
//    	TextView routesBtnText = (TextView)findViewById(R.id.mainRoutesBtnText);
//    	routesBtnText.setText(R.string.routes);
//    	HorizontalScrollView routesBtnScrollView = (HorizontalScrollView)findViewById(R.id.mainRoutesBtnScrollView);
//    	if (_model.getFavorite().size() > 0) {
//    		routesBtnText.setText(routesBtnText.getText() + ":");
//    		routesBtnScrollView.setVisibility(View.VISIBLE);
//		} else {
//			routesBtnScrollView.setVisibility(View.GONE);
//		}
//    	
//    	LinearLayout ticketsLayout = (LinearLayout)findViewById(R.id.mainRoutesScrollView);
//    	ticketsLayout.removeAllViews();
//    	int index = 0;
//		for (Route route : _model.getFavorite()) {
//			TicketCloseLess ticket = new TicketCloseLess(this);
//			ticket.setRoute(route);
//			ticketsLayout.addView(ticket);
//			ticket.setLast(index++ == _model.getFavorite().size() - 1);
//		}
//    	
//    	if (_model.getFavorite().size() == 0) {
//			_map.getUiSettings().setRotateGesturesEnabled(false);
//			CameraPosition camPos = _map.getCameraPosition();
//			if (camPos.bearing != 0) {
//				_map.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(camPos.target, camPos.zoom, camPos.tilt, 0)));
//			}
//		} else {
//			_map.getUiSettings().setRotateGesturesEnabled(true);
//		}
//    	
//    	if (_model.getAllTransportOverlay().size() == 0) {
//			_map.clear();
//		} else {
//			updateTransport(true);
//		}
//    	_timer = new Timer();
//    	_timer.schedule(new TimerTask() {
//
//				@Override
//				public void run() {
//					MainActivity.this.runOnUiThread(new Runnable() {
//
//						@Override
//						public void run() {
//							updateTransport(false);
//						}
//					});
//				}
//			}, 0, 5000);
		super.onResume();
	}


	@Override
	protected void onPause() {
		_timer.cancel();
		_timer = null;
		super.onPause();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
	public void updateTransport(Boolean speed) {
    	if (_model.getFavorite().size() == 0) {
    		View mainFrame = findViewById(R.id.mainFrame);
    		GoogleMap map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
    		LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
            _model.loadImg(bounds, mainFrame.getWidth(), mainFrame.getHeight(), Contr.getInstance());
		} else {
			if (speed) {
				for (TransportOverlay transportOverlay : _model.getAllTransportOverlay()) {
					LatLng position = new LatLng(transportOverlay.transport.Lat, transportOverlay.transport.Lng);
					transportOverlay.groundOverlay.remove();
					transportOverlay.groundOverlay = _map.addGroundOverlay(new GroundOverlayOptions().image(_getBusBitMap(transportOverlay.transport.kind)).position(position, _getWidth()).bearing(transportOverlay.transport.direction));//positionFromBounds(bounds).
					transportOverlay.marker.remove();
					transportOverlay.marker = _map.addMarker(new MarkerOptions().position(position).snippet("123").title("qwe").icon(_getRouteNumberBitMap(transportOverlay.transport.routeNumber)));
				}
			} else {
				_model.showFavoriteRoutes(Contr.getInstance());
			}
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
		float zoom = _map.getCameraPosition().zoom;
		double w = .5 * Math.pow(2, Math.max(.0, 21.0 - zoom));
		return (float)w;
	}

	public void showTransportListOnMap(ArrayList<Transport> array) {
		for (Transport transport : array) {
			LatLng position = new LatLng(transport.Lat, transport.Lng);
			TransportOverlay transportOverlay = _getTransportOverlayById(transport.id);
			if (transportOverlay == null) {
				GroundOverlay groundOverlay = _map.addGroundOverlay(new GroundOverlayOptions().image(_getBusBitMap(transport.kind)).position(position, _getWidth()).bearing(transport.direction));
				Marker marker = _map.addMarker(new MarkerOptions().position(position).snippet("123").title("qwe").icon(_getRouteNumberBitMap(transport.routeNumber)));
				transportOverlay = new TransportOverlay();
				transportOverlay.transport = transport;
				transportOverlay.groundOverlay = groundOverlay;
				transportOverlay.marker = marker;
				_model.getAllTransportOverlay().add(transportOverlay);
			} else {
				transportOverlay.groundOverlay.remove();
				transportOverlay.marker.remove();
				transportOverlay.groundOverlay = _map.addGroundOverlay(new GroundOverlayOptions().image(_getBusBitMap(transportOverlay.transport.kind)).position(position, _getWidth()).bearing(transport.direction));
				transportOverlay.marker = _map.addMarker(new MarkerOptions().position(position).snippet("123").title("qwe").icon(_getRouteNumberBitMap(transport.routeNumber)));
			}
		}
	}
	
	public void showTransportImgOnMap(Bitmap img) {
		if (img != null) {
			_map.clear();
			BitmapDescriptor image = BitmapDescriptorFactory.fromBitmap(img);
	        LatLngBounds bounds = _map.getProjection().getVisibleRegion().latLngBounds;
	        _map.addGroundOverlay(new GroundOverlayOptions()
	            .image(image)
	            .positionFromBounds(bounds));
		}
	}
}