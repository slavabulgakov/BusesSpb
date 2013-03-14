package ru.slavabulgakov.busesspb;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import ru.slavabulgakov.busesspb.Model.OnLoadCompleteListener;
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
import com.google.android.gms.maps.model.LatLngBoundsCreator;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.widget.Button;

public class MainActivity extends BaseActivity implements OnLoadCompleteListener {
	
	private GoogleMap _map;

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
        
        BitmapDescriptor image = BitmapDescriptorFactory.fromBitmap(getBitmapFromURL("http://transport.orgp.spb.ru/cgi-bin/mapserv?TRANSPARENT=TRUE&FORMAT=image%2Fpng&MAP=vehicle_typed.map&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&LAYERS=vehicle_bus%2Cvehicle_ship%2Cvehicle_tram%2Cvehicle_trolley&WHEELCHAIRONLY=false&SRS=EPSG%3A900913&BBOX=3363346.6513533,8373632.3192932,3389293.6592268,8394706.9252358&WIDTH=1326&HEIGHT=1077"));
        LatLngBounds bounds = _map.getProjection().getVisibleRegion().latLngBounds;
        GroundOverlay groundOverlay = _map.addGroundOverlay(new GroundOverlayOptions()
            .image(image)
            .positionFromBounds(bounds));
        
        Button btn = (Button)findViewById(R.id.button1);
        btn.setOnClickListener(Contr.getInstance());
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
	protected void onResume() {
//    	_model.loadDataForAllRoutes(this);
		super.onResume();
	}


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


	@Override
	public void onLoadComplete(ArrayList<Transport> array) {
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


	@Override
	public void onAllRoutesLoadComplete(ArrayList<Transport> array) {
		_map.clear();
	}
}