package ru.slavabulgakov.busesspb.controller;

import java.util.TimerTask;

import ru.slavabulgakov.busesspb.MainActivity;
import ru.slavabulgakov.busesspb.model.Model;
import ru.slavabulgakov.busesspb.model.Model.MenuKind;

public class LeftMenuState extends State {

	@Override
	public void start() {
		super.start();
		_startTimer();
	}

    @Override
    public void resume() {
        super.resume();
        _startTimer();
    }

    private void _startTimer() {
        if (!_controller.getModel().allRouteIsLoaded()) {
            setTimerTask(new UpdateMenuContentTimerTask((MainActivity)_controller.getActivity(), _controller.getModel()));
        }
    }

    class UpdateMenuContentTimerTask extends TimerTask {
		
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
					if (_model.isOnline() && _model.menuIsOpened(MenuKind.Left) && !_model.allRouteIsLoaded()) {
						if (!_model.allRoutesIsLoading()) {
							_mainActivity.getLeftMenu().loadMenuContent();
						}
					} else {
						cancel();
					}
				}
			});
		}

	}
}
