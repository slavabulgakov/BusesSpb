package ru.slavabulgakov.busesspb;

import java.util.TimerTask;

public class UpdateMenuContentTimerTask extends TimerTask {
	
	private MainActivity _mainActivity;
	private Model _model;

	public UpdateMenuContentTimerTask(MainActivity mainActivity, Model model) {
		super();
		this._mainActivity = mainActivity;
		_model = model;
	}

	@Override
	public void run() {
		 _mainActivity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if (_model.isOnline() && _model.menuIsOpened() && !_model.allRouteIsLoaded()) {
					if (!_model.allRoutesIsLoading()) {
						_mainActivity.loadMenuContent();
					}
				} else {
					_mainActivity.updateTimer();
				}
			}
		});
	}

}
