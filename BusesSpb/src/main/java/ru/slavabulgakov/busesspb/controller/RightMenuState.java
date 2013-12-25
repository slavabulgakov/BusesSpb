package ru.slavabulgakov.busesspb.controller;

import android.location.Location;

import java.util.TimerTask;

import ru.slavabulgakov.busesspb.controls.RightMenu;
import ru.slavabulgakov.busesspb.model.ForecastsContainer;
import ru.slavabulgakov.busesspb.model.Loader;
import ru.slavabulgakov.busesspb.model.RightMenuModel;
import ru.slavabulgakov.busesspb.model.RoutesNamesLoaderContainer;
import ru.slavabulgakov.busesspb.model.StationsContainer;
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
            _menu().setLoading();
			loadForecasts();
		}
	}
	
	public void loadForecasts() {
		Loader loader = _menuModel().getLoader(RoutesNamesLoaderContainer.class);
		if (loader == null) {
			_menuModel().loadForContainer(new RoutesNamesLoaderContainer(), _controller);
		} else {
			if (loader.getState().getValue() > Loader.State.staticLoading.getValue()) {
				setTimerTask(new UpdateMenuContentTimerTask());
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
	
	public void staticLoaded(Loader loader) {
		if (loader.getContainer().getClass() == StationsContainer.class) {
			_findNearblyStations();
		} else if (loader.getContainer().getClass() == RoutesNamesLoaderContainer.class) {
			loadForecasts();
		} else if (loader.getContainer().getClass() == ForecastsContainer.class) {
            _menu().setLoaded();
            _menu().loadForecasts(loader.getContainer().getData());
        }
	}
	
	class UpdateMenuContentTimerTask extends TimerTask {
		
		@Override
		public void run() {
            Loader loader = _menuModel().getLoader(ForecastsContainer.class);
            if (loader == null) {
                _menuModel().loadForContainer(new ForecastsContainer(_stationId, _menuModel()), _controller);
            } else {
                loader.reload();
            }
		}

	}

}
