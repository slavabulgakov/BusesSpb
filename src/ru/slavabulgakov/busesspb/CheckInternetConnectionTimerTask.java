package ru.slavabulgakov.busesspb;

import java.util.TimerTask;

public class CheckInternetConnectionTimerTask extends TimerTask {
	
	private MainActivity _mainActivity;
	private Model _model;

	public CheckInternetConnectionTimerTask(MainActivity mainActivity, Model model) {
		_mainActivity = mainActivity;
		_model = model;
	}

	@Override
	public void run() {
		_mainActivity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if (_model.isOnline()) {
					_mainActivity.updateTimer();
				}
			}
		});
	}
}
