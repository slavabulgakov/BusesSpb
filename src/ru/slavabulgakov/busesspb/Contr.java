package ru.slavabulgakov.busesspb;

import java.util.ArrayList;

import ru.slavabulgakov.busesspb.Model.OnLoadCompleteListener;
import ru.slavabulgakov.busesspb.RootView.OnActionListener;
import ru.slavabulgakov.busesspb.Ticket.OnAnimationEndListener;
import ru.slavabulgakov.busesspb.Ticket.OnRemoveListener;

import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.CameraPosition;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
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

public class Contr implements OnClickListener, OnCameraChangeListener, OnLoadCompleteListener, TextWatcher, OnItemClickListener, OnRemoveListener, OnActionListener, OnKeyListener {
	
	private static volatile Contr _instance;
	private Model _model;
	private BaseActivity _currentActivity;
	
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
			_mainActivity().toggleLeftMenu();
			break;
			
		case R.id.busFilter:
			_model.setFilter(TransportKind.Bus);
			_mainActivity().updateTransportOffline();
			_mainActivity().updateFilterButtons();
			break;
			
		case R.id.trolleyFilter:
			_model.setFilter(TransportKind.Trolley);
			_mainActivity().updateTransportOffline();
			_mainActivity().updateFilterButtons();
			break;
			
		case R.id.tramFilter:
			_model.setFilter(TransportKind.Tram);
			_mainActivity().updateTransportOffline();
			_mainActivity().updateFilterButtons();
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
			_mainActivity().updateTransportOffline();
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
	public void onRouteKindsLoadComplete(ArrayList<Route> array) {
		if (_isMainActivity()) {
			if(array == null) {
				Toast.makeText(_currentActivity, R.string.server_access_deny, Toast.LENGTH_LONG).show();
			}
			_mainActivity().showMenuContent();
		}
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
	public void onMenuChangeState(boolean isOpen) {
		_mainActivity().menuChangeState(isOpen);
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
}