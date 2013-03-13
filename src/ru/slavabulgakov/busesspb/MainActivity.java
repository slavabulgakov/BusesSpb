package ru.slavabulgakov.busesspb;

import java.util.ArrayList;
import ru.slavabulgakov.busesspb.Model.OnLoadCompleteListener;
import ru.slavabulgakov.busesspb.Model.Transport;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.os.Bundle;
import android.annotation.SuppressLint;
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
        
        Button btn = (Button)findViewById(R.id.button1);
        btn.setOnClickListener(Contr.getInstance());
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