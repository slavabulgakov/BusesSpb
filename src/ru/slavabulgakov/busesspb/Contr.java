package ru.slavabulgakov.busesspb;

import java.util.ArrayList;

import ru.slavabulgakov.busesspb.Model.OnLoadCompleteListener;
import ru.slavabulgakov.busesspb.RootView.OnActionListener;
import ru.slavabulgakov.busesspb.Ticket.OnAnimationEndListener;
import ru.slavabulgakov.busesspb.Ticket.OnRemoveListener;

import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.CameraPosition;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
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

	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		ListView listView = (ListView)_currentActivity.findViewById(R.id.selectRouteListView);
		switch (v.getId()) {
		case R.id.mainRoutesBtn:
			((MainActivity)_currentActivity).toggleLeftMenu();
			break;
			
		case R.id.busFilter:
			_model.setFilter(TransportKind.Bus);
			((MainActivity)_currentActivity).updateTransport();
			((MainActivity)_currentActivity).updateFilterButtons();
			break;
			
		case R.id.trolleyFilter:
			_model.setFilter(TransportKind.Trolley);
			((MainActivity)_currentActivity).updateTransport();
			((MainActivity)_currentActivity).updateFilterButtons();
			break;
			
		case R.id.tramFilter:
			_model.setFilter(TransportKind.Tram);
			((MainActivity)_currentActivity).updateTransport();
			((MainActivity)_currentActivity).updateFilterButtons();
			break;
			
		case R.id.location:
			((MainActivity)_currentActivity).moveCameraToMyLocation();
			break;
			
		case R.id.plus:
			((MainActivity)_currentActivity).zoomCameraTo(1);
			break;
			
		case R.id.minus:
			((MainActivity)_currentActivity).zoomCameraTo(-1);
			break;
			
		case R.id.about:
			_currentActivity.startActivity(new Intent(_currentActivity, AboutActivity.class));
			break;
			
		case R.id.clearRouteText:
			EditText editText = (EditText)_currentActivity.findViewById(R.id.selectRouteText);
			editText.setText("");
			break;
			
		case R.id.back_btn:
			_currentActivity.finish();
			break;
			
		default:
			break;
		}
		
		if (listView != null) {
			if (listView.getAdapter() != null) {
				TransportKind kind = TransportKind.None;
				switch (v.getId()) {
				case R.id.menuBusFilter:
					kind = TransportKind.Bus;
					break;
					
				case R.id.menuTrolleyFilter:
					kind = TransportKind.Trolley;
					break;
					
				case R.id.menuTramFilter:
					kind = TransportKind.Tram;
					break;

				default:
					break;
				}
				if (kind != TransportKind.None) {
					_model.setFilterMenu(kind);
					((MainActivity)_currentActivity).updateListView();
					((MainActivity)_currentActivity).updateFilterButtons();
				}
			}
		}
		
		if (_currentActivity.getClass() == MainActivity.class && v.getClass() == CloseAllTickets.class) {
			LinearLayout ticketsLayout = (LinearLayout)_currentActivity.findViewById(R.id.selectRouteTickets);
			for (Route route : _model.getFavorite()) {
				_model.getAllRoutes().add(route);
			}
			_model.getFavorite().clear();
			ticketsLayout.removeAllViews();
			((MainActivity)_currentActivity).updateListView();
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onCameraChange(CameraPosition cameraPosition) {
		if (_currentActivity.getClass() == MainActivity.class) {
			((MainActivity)_currentActivity).updateTransport();
			_model.setZoom(cameraPosition.zoom);
			_model.setLocation(cameraPosition.target);
		}
	}

	@Override
	public void onTransportListOfRouteLoadComplete(ArrayList<Transport> array) {
		if (_currentActivity.getClass() == MainActivity.class) {
			if(array == null) {
				Toast.makeText(_currentActivity, R.string.server_access_deny, Toast.LENGTH_LONG).show();
			}
			((MainActivity)_currentActivity).showTransportListOnMap(array);
		}
	}

	@Override
	public void onRouteKindsLoadComplete(ArrayList<Route> array) {
		if (_currentActivity.getClass() == MainActivity.class) {
			if(array == null) {
				Toast.makeText(_currentActivity, R.string.server_access_deny, Toast.LENGTH_LONG).show();
			}
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
		
		ImageButton clearButton = (ImageButton)_currentActivity.findViewById(R.id.clearRouteText);
		if (text.length() > 0) {
			clearButton.setVisibility(View.VISIBLE);
		} else {
			clearButton.setVisibility(View.GONE);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final LinearLayout ticketsLayout = (LinearLayout)_currentActivity.findViewById(R.id.selectRouteTickets);
		ListView listView = (ListView) _currentActivity.findViewById(R.id.selectRouteListView);
		Adapter adapter = (Adapter)listView.getAdapter();
		Route route = adapter.getItem(position);
		final Ticket ticket = new Ticket(_currentActivity, null);
		ticket.setRoute(route);
		ticket.setOnRemoveListener(this);
		_model.setRouteToFavorite(route);
		((MainActivity)_currentActivity).putCloseAllButtonToTicketsLayout();
		if (_model.getFavorite().size() > 1) {
			for (int i = 0; i < ticketsLayout.getChildCount(); i++) {
				View ticket_ = (View)ticketsLayout.getChildAt(i);
				if (ticket_.getClass() == Ticket.class) {
					((Ticket)ticket_).animatedOffsetRight(null);
				}
			}
			
			ticketsLayout.addView(ticket, 1);
			ticket.animatedShow();
		} else {
			ticketsLayout.addView(ticket);
			ticket.animatedShow();
		}
		((MainActivity)_currentActivity).updateListView();
		
		HorizontalScrollView routeTicketsScrollView = (HorizontalScrollView)_currentActivity.findViewById(R.id.routeTicketsScrollView);
		if (_model.getFavorite().size() > 0) {
			routeTicketsScrollView.setVisibility(View.VISIBLE);
		} else {
			routeTicketsScrollView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onRemove(Ticket ticket) {
		_model.setRouteToAll(ticket.getRoute());
		((MainActivity)_currentActivity).updateListView();
		((MainActivity)_currentActivity).putCloseAllButtonToTicketsLayout();
		LinearLayout ticketsLayout = (LinearLayout)_currentActivity.findViewById(R.id.selectRouteTickets);
		for(int i = 0; i < ticketsLayout.getChildCount() - 1; i++) {
			if (ticketsLayout.getChildAt(i).getClass() == Ticket.class) {
				Ticket t = (Ticket)ticketsLayout.getChildAt(i);
				if (t.getRoute().id.equals(ticket.getRoute().id)) {
					for(int j = i + 1; j < ticketsLayout.getChildCount() - 1; j++) {
						if(ticketsLayout.getChildAt(j).getClass() == Ticket.class) {
							Ticket ti = (Ticket)ticketsLayout.getChildAt(j);
							ti.animatedOffsetLeft(null);
						}
					}
					break;
				}
			}
		}
	}

	@Override
	public void onMenuChangeState(boolean isOpen) {
		((MainActivity)_currentActivity).menuChangeState(isOpen);
	}

	@Override
	public void onHold(Boolean hold) {
		((MainActivity)_currentActivity).enableMapGestures(!hold);
	}

	@Override
	public void onInternetAccessDeny() {
		Toast.makeText(_currentActivity, R.string.internet_access_deny, Toast.LENGTH_LONG).show();
	}
}