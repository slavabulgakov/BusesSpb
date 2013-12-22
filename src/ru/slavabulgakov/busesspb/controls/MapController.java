package ru.slavabulgakov.busesspb.controls;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ru.slavabulgakov.busesspb.R;
import ru.slavabulgakov.busesspb.model.Model;
import ru.slavabulgakov.busesspb.model.SimpleTransportView;
import ru.slavabulgakov.busesspb.model.Transport;
import ru.slavabulgakov.busesspb.model.TransportKind;
import ru.slavabulgakov.busesspb.model.Model.MenuKind;
import ru.slavabulgakov.busesspb.model.TransportOverlay;
import ru.slavabulgakov.busesspb.paths.Path;
import ru.slavabulgakov.busesspb.paths.Point;
import ru.slavabulgakov.busesspb.paths.Station;
import ru.slavabulgakov.busesspb.paths.Stations;
import ru.slavabulgakov.busesspb.paths.SubPath;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Align;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapController implements OnCameraChangeListener, OnInfoWindowClickListener {
	
	public interface Listener {
		void onMapImgUpdated();
		void onInfoWindowClick(Marker marker);
		void onCameraChange(CameraPosition cameraPosition);
	}
	
	private GoogleMap _map;
	private Model _model;
	private Handler _handler;
	private Listener _listener;
	
	public MapController(GoogleMap map, Model model, Listener listener) {
		_map = map;
		_model = model;
		_handler = new Handler(Looper.getMainLooper());
		_listener = listener;
	}
	
	public void onResume() {
		if (_map != null) {
			CameraUpdate center = CameraUpdateFactory.newLatLng(_model.getLocation());
	        _map.moveCamera(center);
	        CameraUpdate zoom = CameraUpdateFactory.zoomTo((float)_model.getZoom());
	        _map.animateCamera(zoom);
	        _map.setMyLocationEnabled(true);
	        _map.setOnCameraChangeListener(this);
	        _map.getUiSettings().setMyLocationButtonEnabled(false);
	        _map.getUiSettings().setZoomControlsEnabled(false);
	        _map.setOnInfoWindowClickListener(this);
		}
	}
	
	public GoogleMap getMap() {
		return _map;
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

    public void enableMapGestures(Boolean enable) {
    	if (enable) {
    		Timer timer = new Timer();
        	timer.schedule(new TimerTask() {
    			
    			@Override
    			public void run() {
    				_handler.post(new Runnable() {
						
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
    
    public void toggleRotateMap(boolean isOpen) {
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
    
    public void clearMap() {
    	_handler.post(new Runnable() {
			
			@Override
			public void run() {
				if (_map != null) {
					_map.clear();
					if (!_model.menuIsOpened(MenuKind.Left)) {
			    		_model.getModelPaths().loadPaths();
					}
				}
			}
		});
    }
    
    public float getWidth() {
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
					TransportOverlay transportOverlay = _getTransportOverlayById(transport.id);
					if (transportOverlay == null) {
						transportOverlay = new TransportOverlay();
						_model.getAllTransportOverlays().add(transportOverlay);
					}
					transportOverlay.transport = transport;
					_addTransportOverlay(transportOverlay);
				}
			}
		}
	}
	public void showTransportListOnMap() {
		if (_map != null) {
			for (TransportOverlay transportOverlay : _model.getAllTransportOverlays()) {
				_addTransportOverlay(transportOverlay);
			}
		}
	}
    
    private void _addTransportOverlay(TransportOverlay transportOverlay) {
    	if (transportOverlay.groundOverlay != null) {
			transportOverlay.groundOverlay.remove();
		}
		if (transportOverlay.marker != null) {
			transportOverlay.marker.remove();
		}
		LatLng position = new LatLng(transportOverlay.transport.Lat, transportOverlay.transport.Lng);
		String velocity = _model.getString(R.string.velocity) + Integer.toString(transportOverlay.transport.velocity) + _model.getString(R.string.km);
		GroundOverlay groundOverlay = _map.addGroundOverlay(new GroundOverlayOptions().image(_getBusBitMap(transportOverlay.transport.kind)).position(position, getWidth()).bearing(transportOverlay.transport.direction));
		Marker marker = _map.addMarker(new MarkerOptions().position(position).title(velocity).icon(_getRouteNumberBitMap(transportOverlay.transport.routeNumber)));
		transportOverlay.groundOverlay = groundOverlay;
		transportOverlay.marker = marker;
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
			
		case Ship:
			resId = R.drawable.ship;
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
		int color = 0;
		if (routeNumber.equalsIgnoreCase("1М")) {
			color = Color.argb(0xff, 0xD7, 0x17, 0x36);
		} else if (routeNumber.equalsIgnoreCase("2М")) {
			color = Color.argb(0xff, 0x1, 0x96, 0xFF);
		} else if (routeNumber.equalsIgnoreCase("3М")) {
			color = Color.argb(0xff, 0x4, 0x9F, 0x5C);
		} else if (routeNumber.equalsIgnoreCase("4М")) {
			color = Color.argb(0xff, 0xE0, 0x72, 0x5);
		} else if (routeNumber.equalsIgnoreCase("5М")) {
			color = Color.argb(0xff, 0x72, 0x5, 0x7C);
		} else {
			color = Color.argb(0xff, 0x45, 0x45, 0x45);
		}
		
		paintHighlight.setColor(color);
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
	
	private int _countShows = 0;
	public int getCountShows() {
		return _countShows;
	}
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
		        
		        _listener.onMapImgUpdated();
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
	        
	        _listener.onMapImgUpdated();
	        _countShows++;
		}
	}
	
	public void showPath(Path path) {
		if (path != null) {
			for (SubPath subPath : path) {
				PolylineOptions polylineOptions = new PolylineOptions();
				for (Point point : subPath) {
					polylineOptions.add(point.getLatlng());
				}
				Polyline polyline = _map.addPolyline(polylineOptions.width(2).color(Color.rgb(220, 60, 0)).zIndex(-2));
				_model.getModelPaths().addMapItem(polyline);
			}
		}
	}
	
	private float _getStationWidth() {
		float zoom = 0;
		if (_map != null) {
			zoom = _map.getCameraPosition().zoom;
		}
		double w = .3 * Math.pow(2, Math.max(.0, 21.0 - zoom));
		return (float)w;
	}
	
	public void showStations(Stations stations) {
		for (Station station : stations) {
			MarkerOptions markerOptions = new MarkerOptions().position(station.point.getLatlng()).title(station.name).icon(_getEmptyBitMap()).anchor((float).5, (float).5);
			Marker marker = _map.addMarker(markerOptions);
			Pair<Marker, Station> pair = new Pair<Marker, Station>(marker, station);
			_model.getModelPaths().addMapItem(pair);
			
			BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.station);
			GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions().image(icon).position(station.point.getLatlng(), _getStationWidth()).zIndex(-1);
			GroundOverlay groundOverlay = _map.addGroundOverlay(groundOverlayOptions);
			_model.getModelPaths().addMapShortTimeItem(groundOverlay);
		}
	}
	
	private BitmapDescriptor _getEmptyBitMap() {
		Bitmap bitmap = Bitmap.createBitmap(5, 5, Config.ARGB_8888);
		return BitmapDescriptorFactory.fromBitmap(bitmap);
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		_listener.onInfoWindowClick(marker);
	}

	@Override
	public void onCameraChange(CameraPosition cameraPosition) {
		_listener.onCameraChange(cameraPosition);
	}
}
