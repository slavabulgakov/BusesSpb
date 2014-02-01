package ru.slavabulgakov.busesspb.controller;

import java.util.TimerTask;

public class State {
	protected Controller _controller;
	private TimerTask _timerTask;
	
	public void setController(Controller controller) {
		_controller = controller;
	}
	
	public void start() {
		
	}
	
	protected void setTimerTask(TimerTask timerTask) {
		_timerTask = timerTask;
		_controller.getTimer().schedule(_timerTask, 0, 5000);
	}

	public void removeState() {
		if (_timerTask != null) {
            _controller.cancelTimer();
 		}
	}
	
	public void pause() {
		if (_timerTask != null) {
            _controller.cancelTimer();
		}
	}

    public void resume() {}
}
