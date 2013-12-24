package ru.slavabulgakov.busesspb.controller;

import java.util.TimerTask;

import ru.slavabulgakov.busesspb.MainActivity;
import ru.slavabulgakov.busesspb.model.Model;

public class NetCheckState extends State {

	@Override
	public void start() {
		super.start();
		setTimerTask(new CheckInternetConnectionTimerTask((MainActivity)_controller.getActivity(), _controller.getModel()));
	}

	class CheckInternetConnectionTimerTask extends TimerTask {
		
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
						cancel();
					}
				}
			});
		}
	}
}
