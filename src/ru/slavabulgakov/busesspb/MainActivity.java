package ru.slavabulgakov.busesspb;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.osmdroid.ResourceProxy.bitmap;

import ru.slavabulgakov.busesspb.Model.Transport;
import ru.slavabulgakov.busesspb.Model.TransportKind;
import ru.slavabulgakov.busesspb.Model.TransportOverlay;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
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
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuffXfermode;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends BaseActivity {
	
	private GoogleMap _map;
	private Timer _timer;

    @SuppressLint("NewApi")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        _map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(59.946282, 30.356412));
        _map.moveCamera(center);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(10);
        _map.animateCamera(zoom);
        _map.setMyLocationEnabled(true);
        _map.setOnCameraChangeListener(Contr.getInstance());
        
        Button btn = (Button)findViewById(R.id.mainButton);
        btn.setOnClickListener(Contr.getInstance());
    }
    
    @Override
	protected void onResume() {
    	if (_model.getAllTransportOverlay().size() == 0) {
			_map.clear();
		} else {
			updateTransport(true);
		}
    	_timer = new Timer();
    	_timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				MainActivity.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						updateTransport(false);
					}
				});
			}
		}, 0, 15000);
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
	
	private LatLngBounds _getBounds(LatLng latlng) {
		Point point = _map.getProjection().toScreenLocation(latlng);
		Point point1 = new Point(point.x + 4, point.y - 6);
		Point point2 = new Point(point.x - 4, point.y + 7);
		LatLng latlng1 = _map.getProjection().fromScreenLocation(point1);
		LatLng latlng2 = _map.getProjection().fromScreenLocation(point2);
		LatLngBounds bounds = new LatLngBounds(latlng2, latlng1);
		return bounds;
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
					LatLngBounds bounds = _getBounds(position);
					transportOverlay.groundOverlay.remove();
					transportOverlay.groundOverlay = _map.addGroundOverlay(new GroundOverlayOptions().image(_getBusBitMap(transportOverlay.transport.kind)).positionFromBounds(bounds).bearing(transportOverlay.transport.direction));
					transportOverlay.marker.remove();
					transportOverlay.marker = _map.addMarker(new MarkerOptions().position(position).snippet("123").title("qwe"));//.icon(_getRouteNumberBitMap(transportOverlay.transport.routeNumber))
				}
			} else {
				_model.cloneExcessTransportOverlay();
				_model.showFavoriteRoutes(Contr.getInstance());
			}
		}
	}
	
	public void removeExcessTransportOverlay() {
		if (_model.getExcessTransportOverlay() != null) {
			for (TransportOverlay transportOverlay : _model.getExcessTransportOverlay()) {
				transportOverlay.groundOverlay.remove();
				_model.getAllTransportOverlay().remove(transportOverlay);
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
		Bitmap bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Resources resources = getResources();
		float scale = resources.getDisplayMetrics().density;
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.RED);
		paint.setTextSize((int) (14 * scale));
		canvas.drawText(routeNumber, 10, 10, paint);
		return BitmapDescriptorFactory.fromBitmap(bitmap);
	}

	public void showTransportListOnMap(ArrayList<Transport> array) {
		for (Transport transport : array) {
			LatLng position = new LatLng(transport.Lat, transport.Lng);
			LatLngBounds bounds = _getBounds(position);
			TransportOverlay transportOverlay = _getTransportOverlayById(transport.id);
			if (transportOverlay == null) {
				GroundOverlay groundOverlay = _map.addGroundOverlay(new GroundOverlayOptions().image(_getBusBitMap(transport.kind)).positionFromBounds(bounds).bearing(transport.direction));
				Marker marker = _map.addMarker(new MarkerOptions().position(position).snippet("123").title("qwe"));//.icon(_getRouteNumberBitMap(transport.routeNumber))
				transportOverlay = new TransportOverlay();
				transportOverlay.transport = transport;
				transportOverlay.groundOverlay = groundOverlay;
				transportOverlay.marker = marker;
				_model.getAllTransportOverlay().add(transportOverlay);
			} else {
				transportOverlay.groundOverlay.remove();
				transportOverlay.marker.remove();
				transportOverlay.groundOverlay = _map.addGroundOverlay(new GroundOverlayOptions().image(_getBusBitMap(transportOverlay.transport.kind)).positionFromBounds(bounds).bearing(transport.direction));
				transportOverlay.marker = _map.addMarker(new MarkerOptions().position(position).snippet("123").title("qwe"));//.icon(_getRouteNumberBitMap(transport.routeNumber))
				if (_model.getExcessTransportOverlay() != null) {
					_model.getExcessTransportOverlay().remove(transportOverlay);
				}
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