package ru.slavabulgakov.busesspb;

import ru.slavabulgakov.busesspb.Model.OnLoadCompleteListener;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

public class Contr implements OnClickListener {
	
	private static volatile Contr _instance;
	private Model _model;
	private Activity _currentActivity;
	
	public static Contr getInstance() {
    	Contr localInstance = _instance;
    	if (localInstance == null) {
    		synchronized (Contr.class) {
    			localInstance = _instance;
    			if (localInstance == null) {
    				_instance = localInstance = new Contr();
    			}
    		}
    	}
    	return localInstance;
    }
	
	public void setActivity(BaseActivity activity) {
		_currentActivity = activity;
		_model = (Model)_currentActivity.getApplicationContext();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button1:
//			_model.loadDataForRoute("3", (OnLoadCompleteListener)_currentActivity);
			_model.loadDataForAllRoutes((OnLoadCompleteListener)_currentActivity);
			break;

		default:
			break;
		}
	}
}