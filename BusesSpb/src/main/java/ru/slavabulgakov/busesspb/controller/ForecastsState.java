package ru.slavabulgakov.busesspb.controller;

import java.util.ArrayList;
import java.util.TimerTask;

import ru.slavabulgakov.busesspb.Network.Network;
import ru.slavabulgakov.busesspb.controls.RightMenu;
import ru.slavabulgakov.busesspb.Network.ForecastsContainer;
import ru.slavabulgakov.busesspb.Network.Loader;
import ru.slavabulgakov.busesspb.Network.RoutesNamesLoaderContainer;
import ru.slavabulgakov.busesspb.paths.Forecast;

public class ForecastsState extends State {
	
	private String _stationId;

	public ForecastsState() {

	}
	
	private RightMenu _menu() {
		return _controller.getMainActivity().getRightMenu();
	}
	
	private Network _getNetwork() {
		return _controller.getModel().getNetwork();
	}

	@Override
	public void start() {
		super.start();

        _stationId = (String)_controller.getModel().getData("stationId");
        String title = (String)_controller.getModel().getData("stationTitle");
        _menu().setTitle(title);
        _menu().setLoading();
        loadForecasts();
	}

    @Override
    public void resume() {
        super.resume();
        String title = (String)_controller.getModel().getData("stationTitle");
        _menu().setTitle(title);
        loadForecasts();
    }

    public void loadForecasts() {
		Loader loader = _getNetwork().getLoader(RoutesNamesLoaderContainer.class);
		if (loader == null) {
			_getNetwork().loadForContainer(new RoutesNamesLoaderContainer(), _controller);
		} else {
			if (loader.getState().getValue() > Loader.State.staticLoading.getValue()) {
				setTimerTask(new UpdateMenuContentTimerTask());
			}
		}
	}
	
	public void staticLoaded(Loader loader) {
		if (loader.getContainer().getClass() == RoutesNamesLoaderContainer.class) {
			loadForecasts();
		} else if (loader.getContainer().getClass() == ForecastsContainer.class) {
            _menu().setLoaded();
            _menu().loadForecasts((ArrayList<Forecast>)loader.getContainer().getData());
        }
	}
	
	class UpdateMenuContentTimerTask extends TimerTask {
		
		@Override
		public void run() {
            _getNetwork().loadForContainer(new ForecastsContainer(_stationId, _controller.getModel()), _controller);
		}

	}

}
