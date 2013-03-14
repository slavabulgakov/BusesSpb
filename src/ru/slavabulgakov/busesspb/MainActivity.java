package ru.slavabulgakov.busesspb;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ru.slavabulgakov.busesspb.Model.Transport;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
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
        
        Button btn = (Button)findViewById(R.id.button1);
        btn.setOnClickListener(Contr.getInstance());
    }
    
    @SuppressLint("NewApi")
	public void updateTransportImg() {
    	View mainFrame = findViewById(R.id.mainFrame);
		GoogleMap map = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
		LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        _model.loadImg(bounds, mainFrame.getWidth(), mainFrame.getHeight(), Contr.getInstance());
	}

    @Override
	protected void onResume() {
    	_timer = new Timer();
    	_timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				MainActivity.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						updateTransportImg();
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


	public void showTransportListOnMap(ArrayList<Transport> array) {
		BitmapDescriptor bitmapDescr = BitmapDescriptorFactory.fromResource(R.drawable.bus);
		System.out.println("length:" + array.size());
		for (Transport transport : array) {
			LatLng latlng = new LatLng(transport.Lat, transport.Lng);
			_map.addMarker(new MarkerOptions()
            .position(latlng)
            .title(transport.routeNumber)
            .icon(bitmapDescr));
			System.out.println("lat:" + Double.toString(latlng.latitude) + ", lng:" + Double.toString(latlng.longitude));
		}
	}

	public void clearMap() {
		_map.clear();
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