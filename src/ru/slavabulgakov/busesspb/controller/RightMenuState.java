package ru.slavabulgakov.busesspb.controller;

import java.util.TimerTask;

import ru.slavabulgakov.busesspb.controls.RightMenu;
import ru.slavabulgakov.busesspb.model.RightMenuModel;
import ru.slavabulgakov.busesspb.paths.Forecasts;

public class RightMenuState extends State {
	
	private String _stationId;
	
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
		
		_menu().setLoading();
		if (_menuModel().isStaticRoutesNamesLoaded()) {
			setTimerTask(new UpdateMenuContentTimerTask());
		} else if (!_menuModel().isStaticRoutesNamesLoading()) {
			_menuModel().loadRoutesNames();
		}
	}
	
	public void staticRoutesNamesLoaded() {
		if (_menuModel().isForecastsLoaded()) {
			_menu().loadForecasts(_menuModel().getLastLoadedForecasts());
		} else if (!_menuModel().isForecastsLoading()) {
			setTimerTask(new UpdateMenuContentTimerTask());
		}
	}
	
	public void routesNamesLoaded() {
		// обновляем
		if (_menuModel().isForecastsLoaded()) {
			_menu().loadForecasts(_menuModel().getLastLoadedForecasts());
		} else if (!_menuModel().isForecastsLoading()) {
			setTimerTask(new UpdateMenuContentTimerTask());
		}
	}
	
	public void forecastsLoaded(Forecasts forecasts) {
		if (_menuModel().isStaticRoutesNamesLoaded()) {
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
