package ru.slavabulgakov.busesspb;

import java.util.TimerTask;

import ru.slavabulgakov.busesspb.model.Model;


public class UpdateTransportTimerTask extends TimerTask {
	
	private MainActivity _mainActivity;
	private Model _model;

	public UpdateTransportTimerTask(MainActivity mainActivity, Model model) {
		_mainActivity = mainActivity;
		_model = model;
	}

	@Override
	public void run() {
		if (_model.isOnline()) {
			if (!_model.menuIsOpened()) {
				_mainActivity.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						_mainActivity.updateTransport();
					}
				});
    		}
		} else {
			_mainActivity.updateTimer();
		}
	}

}
