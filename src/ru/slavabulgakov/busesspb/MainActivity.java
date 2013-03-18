package ru.slavabulgakov.busesspb;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ru.slavabulgakov.busesspb.Model.Marker;
import ru.slavabulgakov.busesspb.Model.Transport;

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
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Point;
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
    	_map.clear();
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
		}, 15000, 15000);
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

	private ArrayList<Marker> _excessMarkes;
	@SuppressLint("NewApi")
	public void updateTransport() {
    	if (_model.getFavorite().size() == 0) {
    		View mainFrame = findViewById(R.id.mainFrame);
    		GoogleMap map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
    		LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
            _model.loadImg(bounds, mainFrame.getWidth(), mainFrame.getHeight(), Contr.getInstance());
		} else {
			_excessMarkes = (ArrayList<Model.Marker>)_model.getMarkers().clone();
			_model.showFavoriteRoutes(Contr.getInstance());
		}
	}
	
	public void removeExcessMarkers() {
		for (Marker marker : _excessMarkes) {
			marker.groundOverlay.remove();
			_model.getMarkers().remove(marker);
		}
	}
	
	private void _setMarker(int id, GroundOverlay groundOverlay) {
		GroundOverlay findGroundOverlay = _getMarker(id);
		if (findGroundOverlay == null) {
			Marker newMarker = new Marker();
			newMarker.id = id;
			newMarker.groundOverlay = groundOverlay;
			_model.getMarkers().add(newMarker);
			if (_excessMarkes != null) {
				_excessMarkes.remove(newMarker);
			}
		}
	}
	
	private GroundOverlay _getMarker(int id) {
		Marker findMarker = null;
		for (Marker marker : _model.getMarkers()) {
			if (marker.id == id) {
				findMarker = marker;
				break;
			}
		}
		if (findMarker != null) {
			return findMarker.groundOverlay;
		}
		return null;
	}

	public void showTransportListOnMap(ArrayList<Transport> array) {
		BitmapDescriptor bitmapDescr = BitmapDescriptorFactory.fromResource(R.drawable.bus);
		for (Transport transport : array) {
			LatLng latlng = new LatLng(transport.Lat, transport.Lng);
			Point point = _map.getProjection().toScreenLocation(latlng);
			Point point1 = new Point(point.x + 4, point.y - 6);
			Point point2 = new Point(point.x - 4, point.y + 7);
			LatLng latlng1 = _map.getProjection().fromScreenLocation(point1);
			LatLng latlng2 = _map.getProjection().fromScreenLocation(point2);
			LatLngBounds bounds = new LatLngBounds(latlng2, latlng1);
			GroundOverlay groundOverlay = _getMarker(transport.id);
			if (groundOverlay == null) {
				groundOverlay = _map.addGroundOverlay(new GroundOverlayOptions().image(bitmapDescr).positionFromBounds(bounds).bearing(transport.direction));
				_setMarker(transport.id, groundOverlay);
			} else {
				groundOverlay.setPositionFromBounds(bounds);
				groundOverlay.setBearing(transport.direction);
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