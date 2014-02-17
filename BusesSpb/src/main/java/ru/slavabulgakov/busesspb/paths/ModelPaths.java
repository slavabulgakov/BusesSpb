package ru.slavabulgakov.busesspb.paths;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.HashMap;

import ru.slavabulgakov.busesspb.Network.Loader;
import ru.slavabulgakov.busesspb.Network.StationsContainer;
import ru.slavabulgakov.busesspb.Network.StationsForRouteContainer;
import ru.slavabulgakov.busesspb.model.Model;
import ru.slavabulgakov.busesspb.model.Route;

public class ModelPaths implements Loader.Listener {
    private Model _model;
    private Handler _handler;
	public ModelPaths(Model model) {
		_model = model;
	}

    private void  _didLoad(final Loader loader) {
        if (loader.getContainer().getClass() == StationsContainer.class) {
            updateStationsAndPaths();
        } else if (loader.getContainer().getClass() == StationsForRouteContainer.class && loader.getState().getValue() > Loader.State.staticLoading.getValue()) {
            HashMap<String, Object> data = (HashMap<String, Object>)loader.getContainer().getData();
            Path path = (Path)data.get("path");
            _listener.onPathLoaded(path);
            ArrayList<?>stations = (ArrayList<?>)data.get("stops");
            _listener.onStationsLoaded(stations);
        }
    }

    @Override
    public void staticLoaded(Loader loader) {
        _didLoad(loader);
    }

    @Override
    public void netLoaded(Loader loader) {
        _didLoad(loader);
    }

    @Override
    public void netError(Loader loader) {

    }

    public interface OnPathLoaded {
		void onPathLoaded(Path path);
		void onStationsLoaded(ArrayList<?> stations);
	}
	private OnPathLoaded _listener;
	public void setListener(OnPathLoaded listener) {
		_listener = listener;
	}
	
	public void updateStationsAndPaths() {
        removeMapItems();
        removeMapShortTimeItems();
        if (pathsIsOn()) {
            Loader loader_ = _model.getNetwork().getLoader(StationsContainer.class);
            if (loader_ == null) {
                _model.getNetwork().loadForContainer(new StationsContainer(), this);
                return;
            } else if (loader_.getState().getValue() < Loader.State.netLoading.getValue()) {
                loader_.setListener(this);
                return;
            }

			ArrayList<Integer> routeIds = new ArrayList<Integer>();
			for (Route route : _model.getFavorite()) {
				routeIds.add(route.id);
			}
			
			for (Integer routeId : routeIds) {
                ArrayList<Integer> directions = new ArrayList<Integer>();
                directions.add(0);
                directions.add(1);
                ArrayList<Loader> loaders =  _model.getNetwork().getLoaders(StationsForRouteContainer.class);
                for (Loader loader : loaders) {
                    StationsForRouteContainer stationsForRouteContainer = (StationsForRouteContainer)loader.getContainer();
                    if (stationsForRouteContainer.getRouteId() == routeId && stationsForRouteContainer.getDirection() == directions.get(0)) {
                        directions.remove(0);
                        _didLoad(loader);
                    }
                }
                if (directions.size() > 0) {
                    for (Integer direction : directions) {
                        _model.getNetwork().loadForContainer(new StationsForRouteContainer(routeId, direction, _model), this);
                    }
                }
			}
		}
	}

	private ArrayList<MapItem>_mapItems;
	private ArrayList<MapItem> _getMapItems() {
		if (_mapItems == null) {
			_mapItems = new ArrayList<MapItem>();
		}
		return _mapItems;
	}

	public void removeMapItems() {
        _getHandler().post(new Runnable() {
            @Override
            public void run() {
                for (MapItem item : _getMapItems()) {
                    item.remove();
                }
                _getMapItems().clear();
            }
        });
	}
	public void addMapItem(MapItem item) {
        for (MapItem item_ : _getMapItems()) {
            if (item.equals(item_)) {
                item_.remove();
            }
        }
        _getMapItems().remove(item);
		_getMapItems().add(item);
	}

	@SuppressWarnings("unchecked")
	public Station getStationByMarker(Marker marker) {
		for (Object object : _getMapItems()) {
			if (object.getClass() == StationMarker.class) {
				StationMarker pair = (StationMarker)object;
				if (pair.marker.equals(marker)) {
					return pair.getStation();
				}
			}
		}
		return null;
	}
	
	private ArrayList<MapItem>_mapShortTimeItems;
	private ArrayList<MapItem> _getMapShortTimeItems() {
		if (_mapShortTimeItems == null) {
			_mapShortTimeItems = new ArrayList<MapItem>();
		}
		return _mapShortTimeItems;
	}
	public void removeMapShortTimeItems() {
        _getHandler().post(new Runnable() {
            @Override
            public void run() {
                for (MapItem item : _getMapShortTimeItems()) {
                    item.remove();
                }
                _getMapShortTimeItems().clear();
            }
        });
	}
	public void addMapShortTimeItem(MapItem item) {
        for (MapItem item_ : _getMapShortTimeItems()) {
            if (item.equals(item_)) {
                item_.remove();
            }
        }
        _getMapShortTimeItems().remove(item);
		_getMapShortTimeItems().add(item);
	}
	
	public boolean pathsIsOn() {
		Boolean on = (Boolean)_model.getData("pathsIsOn");
		if (on != null) {
			return on;
		}
		return false;
	}
	public void setPathsOn(boolean on) {
		_model.setData("pathsIsOn", on, true);
		if (on) {
            updateStationsAndPaths();
		} else {
			removeMapItems();
			removeMapShortTimeItems();
		}
	}

    private Stations _nearbyStations;
    public void setNearbyStations(Stations stations) {
        _nearbyStations = stations;
    }
	
    private Handler _getHandler() {
        if (_handler == null) {
            _handler = new Handler(Looper.getMainLooper());
        }
        return _handler;
    }
}
