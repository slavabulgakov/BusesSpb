package ru.slavabulgakov.busesspb.controller;

import java.util.TimerTask;

import ru.slavabulgakov.busesspb.MainActivity;
import ru.slavabulgakov.busesspb.model.Model;
import ru.slavabulgakov.busesspb.model.Model.MenuKind;

public class MapState extends State {

	@Override
	public void start() {
		super.start();
		
		_prepareMap();
	}

    @Override
    public void resume() {
        super.resume();
        _prepareMap();
    }

    private void _prepareMap() {
        _controller.getModel().removeSimpleTransportOverlay();
        if (_controller.getModel().isOnline()) {
            _controller.getModel().removeAllTransportOverlays();
            _controller.getModel().removeLastSimpleTransportView();
            _controller.getMainActivity().updateTransport();
        } else {
            _controller.getMainActivity().updateTransportOffline();
        }

        UpdateTransportTimerTask timerTask = new UpdateTransportTimerTask((MainActivity)_controller.getActivity(), _controller.getModel());
        setTimerTask(timerTask);
    }

    class UpdateTransportTimerTask extends TimerTask {
		
		private MainActivity _mainActivity;
		private Model _model;

		public UpdateTransportTimerTask(MainActivity mainActivity, Model model) {
			_mainActivity = mainActivity;
			_model = model;
		}

		@Override
		public void run() {
			if (_model.isOnline()) {
				if (!_model.menuIsOpened(MenuKind.Left)) {
					_mainActivity.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							_mainActivity.updateTransport();
						}
					});
	    		}
			} else {
				_controller.switchToState(new NetCheckState());
			}
		}
	}
}
