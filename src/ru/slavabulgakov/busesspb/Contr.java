package ru.slavabulgakov.busesspb;

import java.util.ArrayList;
import ru.slavabulgakov.busesspb.Model.OnLoadCompleteListener;
import ru.slavabulgakov.busesspb.RootView.OnActionListener;
import ru.slavabulgakov.busesspb.Ticket.OnRemoveListener;

import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.CameraPosition;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class Contr implements OnClickListener, OnCameraChangeListener, OnLoadCompleteListener, TextWatcher, OnItemClickListener, OnRemoveListener, OnActionListener {
	
	private static volatile Contr _instance;
	private Model _model;
	private Activity _currentActivity;
	
	public static Contr getInstance() {
    	Contr localInstance = _instance;
    	if (localInstance == null) {
    		synchronized (Contr.class) {
    			localInstance = _instance;
    			if (localInstance == null) {
    				_instance = localInstance = new Contr();
    			}
    		}
    	}
    	return localInstance;
    }
	
	public void setActivity(BaseActivity activity) {
		_currentActivity = activity;
		_model = (Model)_currentActivity.getApplicationContext();
	}

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.mainRoutesBtn:
			((MainActivity)_currentActivity).toggleLeftMenu();
			break;
			
		case R.id.busFilter:
			_model.setFilter(Model.BUS_FILTER);
			((MainActivity)_currentActivity).updateTransport();
			break;
			
		case R.id.trolleyFilter:
			_model.setFilter(Model.TROLLEY_FILTER);
			((MainActivity)_currentActivity).updateTransport();
			break;
			
		case R.id.tramFilter:
			_model.setFilter(Model.TRAM_FILTER);
			((MainActivity)_currentActivity).updateTransport();
			break;
		
		default:
			break;
		}
		
		if (_currentActivity.getClass() == MainActivity.class && v.getClass() == Button.class) {
			if (((Button)v).getText().equals("X")) {
				LinearLayout ticketsLayout = (LinearLayout)_currentActivity.findViewById(R.id.selectRouteTickets);
				_model.getFavorite().clear();
				ticketsLayout.removeAllViews();
			}
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onCameraChange(CameraPosition cameraPosition) {
		if (_currentActivity.getClass() == MainActivity.class) {
			((MainActivity)_currentActivity).updateTransport();
			System.out.println(cameraPosition.zoom);
		}
	}

	@Override
	public void onTransportListOfRouteLoadComplete(ArrayList<Transport> array) {
		if (_currentActivity.getClass() == MainActivity.class) {
			((MainActivity)_currentActivity).showTransportListOnMap(array);
		}
	}

	@Override
	public void onRouteKindsLoadComplete(ArrayList<Route> array) {
		if (_currentActivity.getClass() == MainActivity.class) {
			((MainActivity)_currentActivity).showTransportList();
		}
	}

	@Override
	public void onImgLoadComplete(Bitmap img) {
		if (_currentActivity.getClass() == MainActivity.class) {
			((MainActivity)_currentActivity).showTransportImgOnMap(img);
		}
		
	}

	@Override
	public void afterTextChanged(Editable s) {
		String text = s.toString();
		ListView listView = (ListView) _currentActivity.findViewById(R.id.selectRouteListView);
		if (listView.getAdapter() != null) {
			((Adapter)listView.getAdapter()).getFilter().filter(text);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		LinearLayout ticketsLayout = (LinearLayout)_currentActivity.findViewById(R.id.selectRouteTickets);
		ListView listView = (ListView) _currentActivity.findViewById(R.id.selectRouteListView);
		Route route = ((Adapter)listView.getAdapter()).getItem(position);
		Ticket ticket = new Ticket(_currentActivity, null);
		ticket.setRoute(route);
		ticket.setOnRemoveListener(this);
		ticketsLayout.addView(ticket);
		_model.getFavorite().add(route);
		((MainActivity)_currentActivity).putCloseAllButtonToTicketsLayout();
	}

	@Override
	public void onRemove(Ticket ticket) {
		_model.getFavorite().remove(ticket.getRoute());
		((MainActivity)_currentActivity).putCloseAllButtonToTicketsLayout();
	}

	@Override
	public void onAllRoutesLoadComplete() {
	}

	@Override
	public void onOpen() {
		((MainActivity)_currentActivity).animationDidFinish();
	}

	@Override
	public void onHold(Boolean hold) {
		((MainActivity)_currentActivity).enableMapGestures(!hold);
	}
}