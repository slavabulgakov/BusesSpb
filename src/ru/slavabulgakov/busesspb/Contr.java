package ru.slavabulgakov.busesspb;

import java.util.ArrayList;

import ru.slavabulgakov.busesspb.RootView.OnActionListener;
import ru.slavabulgakov.busesspb.Ticket.OnAnimationEndListener;
import ru.slavabulgakov.busesspb.Ticket.OnRemoveListener;
import ru.slavabulgakov.busesspb.model.Model;
import ru.slavabulgakov.busesspb.model.Route;
import ru.slavabulgakov.busesspb.model.RouteName;
import ru.slavabulgakov.busesspb.model.Transport;
import ru.slavabulgakov.busesspb.model.TransportKind;
import ru.slavabulgakov.busesspb.model.Model.MenuKind;
import ru.slavabulgakov.busesspb.model.Model.OnLoadCompleteListener;
import ru.slavabulgakov.busesspb.paths.Forecasts;
import ru.slavabulgakov.busesspb.paths.ModelPaths.OnPathLoaded;
import ru.slavabulgakov.busesspb.paths.Path;
import ru.slavabulgakov.busesspb.paths.Station;
import ru.slavabulgakov.busesspb.paths.Stations;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class Contr implements OnClickListener, OnCameraChangeListener, OnLoadCompleteListener, TextWatcher, OnItemClickListener, OnRemoveListener, OnActionListener, OnKeyListener, OnPathLoaded, OnInfoWindowClickListener {
	
	private static volatile Contr _instance;
	private Model _model;
	private BaseActivity _currentActivity;
	private Handler _handler;
	
	public Handler getHandler() {
		if (_handler == null) {
			_handler = new Handler(Looper.getMainLooper());
		}
		return _handler;
	}
	
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
	
	public BaseActivity getActivity() {
		return _currentActivity;
	}

	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		ListView listView = (ListView)_currentActivity.findViewById(R.id.selectRouteListView);
		switch (v.getId()) {
		case R.id.mainRoutesBtn:
			_mainActivity().toggleMenu(MenuKind.Left);
			break;
			
		case R.id.busFilter:
			_model.setFilter(TransportKind.Bus);
			_mainActivity().updateTransportOffline();
			_mainActivity().updateFilterButtons();
			FlurryAgent.logEvent(FlurryConstants.busFilterBtnPressed);
			break;
			
		case R.id.trolleyFilter:
			_model.setFilter(TransportKind.Trolley);
			_mainActivity().updateTransportOffline();
			_mainActivity().updateFilterButtons();
			FlurryAgent.logEvent(FlurryConstants.trolleyFilterBtnPressed);
			break;
			
		case R.id.tramFilter:
			_model.setFilter(TransportKind.Tram);
			_mainActivity().updateTransportOffline();
			_mainActivity().updateFilterButtons();
			FlurryAgent.logEvent(FlurryConstants.tramFilterBtnPressed);
			break;
			
		case R.id.shipFilter:
			_model.setFilter(TransportKind.Ship);
			_mainActivity().updateTransportOffline();
			_mainActivity().updateFilterButtons();
			FlurryAgent.logEvent(FlurryConstants.shipFilterBtnPressed);
			break;
			
		case R.id.location:
			_mainActivity().moveCameraToMyLocation();
			break;
			
		case R.id.plus:
			_mainActivity().zoomCameraTo(1);
			break;
			
		case R.id.minus:
			_mainActivity().zoomCameraTo(-1);
			break;
			
		case R.id.about:
			_currentActivity.startActivity(new Intent(_currentActivity, AboutActivity.class));
			FlurryAgent.logEvent(FlurryConstants.aboutBtnPressed);
			break;
			
		case R.id.clearRouteText:
			EditText editText = (EditText)_currentActivity.findViewById(R.id.selectRouteText);
			editText.setText("");
			break;
			
		case R.id.back_btn:
			_currentActivity.finish();
			break;
			
		case R.id.internetDeny:
			_currentActivity.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
			break;
			
		case R.id.paths:
			_model.getModelPaths().setPathsOn(_mainActivity().pathsButton().checked());
			FlurryAgent.logEvent(FlurryConstants.pathBtnPressed);
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
					FlurryAgent.logEvent(FlurryConstants.menuBusFilterBtnPressed);
					break;
					
				case R.id.menuTrolleyFilter:
					kind = TransportKind.Trolley;
					FlurryAgent.logEvent(FlurryConstants.menuTrolleyFilterBtnPressed);
					break;
					
				case R.id.menuTramFilter:
					kind = TransportKind.Tram;
					FlurryAgent.logEvent(FlurryConstants.menuTramFilterBtnPressed);
					break;
					
				case R.id.menuShipFilter:
					kind = TransportKind.Ship;
					FlurryAgent.logEvent(FlurryConstants.menuShipFilterBtnPressed);
					break;

				default:
					break;
				}
				if (kind != TransportKind.None) {
					_model.setFilterMenu(kind);
					_mainActivity().updateListView();
					_mainActivity().updateFilterButtons();
				}
			}
		}
		
		if (_isMainActivity() && v.getClass() == CloseAllTickets.class) {
			LinearLayout ticketsLayout = (LinearLayout)_currentActivity.findViewById(R.id.selectRouteTickets);
			for (Route route : _model.getFavorite()) {
				_model.getAllRoutes().add(route);
			}
			_model.getFavorite().clear();
//			ticketsLayout.removeAllViews();
			_mainActivity().updateListView();
			
			for (int i = 0; i < ticketsLayout.getChildCount(); i++) {
				View view = ticketsLayout.getChildAt(i);
				if (view.getClass() == Ticket.class) {
					Ticket ticket = (Ticket)view;
					ticket.animatedRemove(new OnAnimationEndListener() {
						
						@Override
						public void onAnimated(final Ticket ticket_) {
							ticket_.setVisibility(View.GONE);
							((View)ticket_.getParent()).post(new Runnable() {
					            public void run() {
					            	((ViewGroup)ticket_.getParent()).removeView(ticket_);
					            }
					        });
						}
					});
				} else if (view.getClass() == CloseAllTickets.class) {
					CloseAllTickets closeAllTickets = (CloseAllTickets)view;
					closeAllTickets.animatedRemove(new CloseAllTickets.OnAnimationEndListener() {
						
						@Override
						public void onAnimated(final CloseAllTickets button) {
							button.setVisibility(View.GONE);
							((View)button.getParent()).post(new Runnable() {
					            public void run() {
					            	((ViewGroup)button.getParent()).removeView(button);
					            }
					        });
							HorizontalScrollView ticketsScrollView = (HorizontalScrollView)_currentActivity.findViewById(R.id.routeTicketsScrollView);
							ticketsScrollView.setVisibility(View.GONE);
							LinearLayout listViewAndProgressBarLinearLayout = (LinearLayout)_currentActivity.findViewById(R.id.listViewAndProgressBarLinearLayout);
							TranslateAnimation animation = new TranslateAnimation(0, 0, _model.dpToPx(60), 0);
							animation.setDuration(Animations.ANIMATION_DURATION);
							listViewAndProgressBarLinearLayout.startAnimation(animation);
						}
					});
				}
			}
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onCameraChange(CameraPosition cameraPosition) {
		if (_isMainActivity()) {
			if (Math.abs(_model.getZoom() - cameraPosition.zoom) > .1 || _model.isFirstCameraChange()) {
				_mainActivity().updateTransportOffline();
				_model.getModelPaths().updateStations();
			}
			_model.setZoom(cameraPosition.zoom);
			_model.setLocation(cameraPosition.target);
		}
	}

	@Override
	public void onTransportListOfRouteLoadComplete(ArrayList<Transport> array) {
		if (_isMainActivity()) {
			_mainActivity().showTransportListOnMap(array);
		}
	}

	@Override
	public void onRouteKindsLoadComplete(final ArrayList<Route> array) {
		getHandler().post(new Runnable() {
			
			@Override
			public void run() {
				if (_isMainActivity()) {
					if(array == null) {
						Toast.makeText(_currentActivity, R.string.server_access_deny, Toast.LENGTH_LONG).show();
					}
					_mainActivity().showMenuContent();
				}
			}
		});
	}

	@Override
	public void onImgLoadComplete(Bitmap img) {
		if (_isMainActivity()) {
			_mainActivity().showTransportImgOnMap(img);
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
		
		if (!(Boolean)_model.getData("TextEditUsed2", false)) {
			_model.setData("TextEditUsed2", true, false);
			FlurryAgent.logEvent(FlurryConstants.textEditUsed);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
		ListView listView = (ListView) _currentActivity.findViewById(R.id.selectRouteListView);
		final Adapter adapter = (Adapter)listView.getAdapter();
		final Route route = adapter.getItem(position);
		_model.setRouteToFavorite(route);
		Animations.listItemCollapse(view, new Animations.OnAnimationEndListener() {
			
			@Override
			public void onAnimated(View view) {
				adapter.removeRoute(position, view);
			}
		});
		
		Animations.addTicket(route);
		Animations.slideDownRoutesListView();
	}

	@Override
	public void willRemove(Ticket ticket) {
		_model.setRouteToAll(ticket.getRoute());
		_mainActivity().updateListView();
		_mainActivity().putCloseAllButtonToTicketsLayout();
		
		Animations.removeTicket(ticket);
	}
	
	@Override
	public void didRemove(Ticket ticket) {
		if (_model.getFavorite().size() == 0) {
			HorizontalScrollView ticketsScrollView = (HorizontalScrollView)_currentActivity.findViewById(R.id.routeTicketsScrollView);
			ticketsScrollView.setVisibility(View.GONE);
			LinearLayout listViewAndProgressBarLinearLayout = (LinearLayout)_currentActivity.findViewById(R.id.listViewAndProgressBarLinearLayout);
			TranslateAnimation animation = new TranslateAnimation(0, 0, _model.dpToPx(60), 0);
			animation.setDuration(Animations.ANIMATION_DURATION);
			listViewAndProgressBarLinearLayout.startAnimation(animation);
		}
	}

	@Override
	public void onMenuChangeState(boolean isOpen, MenuKind kind) {
		if (kind == MenuKind.Left) {
			_mainActivity().menuChangeState(isOpen);
		} else {
			_mainActivity().rightMenuChangeState(isOpen);
		}
	}

	@Override
	public void onHold(Boolean hold) {
		_mainActivity().enableMapGestures(!hold);
	}
	
	@Override
	public void onMove(double percent) {
		if (_currentActivity != null) {
			if (_isMainActivity()) {
				_mainActivity().moveLeftMenu(percent);
			}
		}
	}

	@Override
	public void onInternetAccessDeny() {
		if (_isMainActivity()) {
			_mainActivity().showInternetDenyIcon();
		}
	}

	@Override
	public void onInternetAccessSuccess() {
		if (_isMainActivity()) {
			_mainActivity().hideInternetDenyIcon();
			_mainActivity().clearMap();
		}
	}
	
	private boolean _isMainActivity() {
		return _currentActivity.getClass() == MainActivity.class;
	}
	
	private MainActivity _mainActivity() {
		if (_isMainActivity()) {
			return (MainActivity)_currentActivity;
		}
		return null;
	}

	@Override
	public boolean onKey(View view, int keyCode, KeyEvent arg2) {
		if (view.getClass() == EditText.class && _isMainActivity()) {
			if (keyCode == KeyEvent.KEYCODE_ENTER) {
				_mainActivity().keyboardTurnOff();
			}
		}
		return false;
	}

	@Override
	public void onPathLoaded(Path path) {
		_mainActivity().showPath(path);
	}

	@Override
	public void onStationsLoaded(Stations stations) {
		_mainActivity().showStations(stations);
	}

	@Override
	public void onInfoWindowClick(Marker arg0) {
		((MainActivity)_currentActivity).toggleMenu(MenuKind.Right);
		Station station = _model.getModelPaths().getStationByMarker(arg0);
		((MainActivity)_currentActivity).getRightMenu().loadByStation(station);
	}

	@Override
	public void onForecastLoaded(Forecasts forecasts) {
		((MainActivity)_currentActivity).getRightMenu().loadForecasts(forecasts);
	}

	@Override
	public void onRoutesNamesLoadComplete(ArrayList<RouteName> array) {
		// TODO Auto-generated method stub
		
	}
}