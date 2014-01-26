package ru.slavabulgakov.busesspb.controller;

import java.util.TimerTask;

import ru.slavabulgakov.busesspb.controls.RightMenu;
import ru.slavabulgakov.busesspb.model.ForecastsContainer;
import ru.slavabulgakov.busesspb.model.Loader;
import ru.slavabulgakov.busesspb.model.RightMenuModel;
import ru.slavabulgakov.busesspb.model.RoutesNamesLoaderContainer;

public class ForecastsState extends State {
	
	private String _stationId;

	public ForecastsState() {

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
        if (_menu().getState() != RightMenu.State.FORECASTS) {
            _menu().changeToState(RightMenu.State.FORECASTS, false);
        }
        loadForecasts();
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
	
	public void staticLoaded(Loader loader) {
		if (loader.getContainer().getClass() == RoutesNamesLoaderContainer.class) {
			loadForecasts();
		} else if (loader.getContainer().getClass() == ForecastsContainer.class) {
            _menu().setLoaded();
            _menu().loadForecasts(loader.getContainer().getData());
        }
	}
	
	class UpdateMenuContentTimerTask extends TimerTask {
		
		@Override
		public void run() {
            _menuModel().loadForContainer(new ForecastsContainer(_stationId, _menuModel()), _controller);
		}

	}

}
