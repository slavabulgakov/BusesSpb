package ru.slavabulgakov.busesspb.controller;

import java.util.TimerTask;

import android.location.Location;

import ru.slavabulgakov.busesspb.controls.RightMenu;
import ru.slavabulgakov.busesspb.model.Loader;
import ru.slavabulgakov.busesspb.model.RightMenuModel;
import ru.slavabulgakov.busesspb.model.RoutesNamesLoaderContainer;
import ru.slavabulgakov.busesspb.model.StationsContainer;
import ru.slavabulgakov.busesspb.paths.Forecasts;
import ru.slavabulgakov.busesspb.paths.Station;
import ru.slavabulgakov.busesspb.paths.Stations;

public class RightMenuState extends State {
	
	private String _stationId;
	private Location _location;
	
	public RightMenuState(String stationId) {
		_stationId = stationId;
	}
	
	private RightMenu _menu() {
		return _controller.getMainActivity().getRightMenu();
	}
	
	private RightMenuModel _menuModel() {
		return _controller.getModel().getRightMenuModel();
	}

	@Override
	public void start() {
		super.start();
		
		if (_stationId == null) {
			loadStations();
		} else {
			loadForecasts();
		}
	}
	
	public void loadForecasts() {
		_menu().setLoading();
		Loader loader = _menuModel().getLoader(RoutesNamesLoaderContainer.class);
		if (loader == null) {
			_menuModel().loadForContainer(new RoutesNamesLoaderContainer(), _controller);
		} else {
			if (loader.getState().getValue() > Loader.State.staticLoading.getValue()) {
				setTimerTask(new UpdateMenuContentTimerTask());
			} else {
				_menuModel().loadForContainer(new RoutesNamesLoaderContainer(), _controller);
			}
		}
	}
	
	public void loadStations() {
		_location = _controller.getMainActivity().getMapController().getMap().getMyLocation();
		Loader loader = _menuModel().getLoader(StationsContainer.class);
		if (loader == null) {
			_menuModel().loadForContainer(new StationsContainer(), _controller);
		} else {
			if (loader.getState().getValue() > Loader.State.staticLoading.getValue()) {
				_findNearblyStations();
			} else {
				_menuModel().loadForContainer(new StationsContainer(), _controller);
			}
		}
	}
	
	private void _findNearblyStations() {
		double epsilon = .005;
		Loader loader = _menuModel().getLoader(StationsContainer.class);
		Stations nearblyStations = new Stations();
		for (Object obj: loader.getContainer().getData()) {
			Station station = (Station)obj;
			if (Math.abs(_location.getLatitude() - station.point.getLatlng().latitude) < epsilon && Math.abs(_location.getLongitude() - station.point.getLatlng().longitude) < epsilon) {
				nearblyStations.add(station);
			}
		}
		_menu().loadNearblyStations(nearblyStations);
	}
	
	private void _routesNamesLoaded() {
		// обновляем
		if (_menuModel().isForecastsLoaded()) {
			_menu().loadForecasts(_menuModel().getLastLoadedForecasts());
		} else if (!_menuModel().isForecastsLoading()) {
			setTimerTask(new UpdateMenuContentTimerTask());
		}
	}
	
	public void staticLoaded(Loader loader) {
		if (loader.getContainer().getClass() == StationsContainer.class) {
			_findNearblyStations();
		} else if (loader.getContainer().getClass() == RoutesNamesLoaderContainer.class) {
			_routesNamesLoaded();
		}
	}
	
	public void forecastsLoaded(Forecasts forecasts) {
		if (_menuModel().getLoader(RoutesNamesLoaderContainer.class).getState().getValue() > Loader.State.staticLoading.getValue()) {
			_menu().setLoaded();
			_menu().loadForecasts(forecasts);
		}
	}

	class UpdateMenuContentTimerTask extends TimerTask {
		
		@Override
		public void run() {
			if (!_menuModel().isForecastsLoading()) {
				_menuModel().loadForecastForStationId(_stationId);
			}
		}

	}

}
