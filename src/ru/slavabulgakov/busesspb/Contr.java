package ru.slavabulgakov.busesspb;

import java.util.ArrayList;

import ru.slavabulgakov.busesspb.Model.OnLoadCompleteListener;
import ru.slavabulgakov.busesspb.Model.Transport;

import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.CameraPosition;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;

public class Contr implements OnClickListener, OnCameraChangeListener, OnLoadCompleteListener {
	
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
			_currentActivity.startActivity(new Intent(_currentActivity, SelectRouteActivity.class));
			break;

		default:
			break;
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onCameraChange(CameraPosition arg0) {
		if (_currentActivity.getClass() == MainActivity.class) {
			((MainActivity)_currentActivity).updateTransportImg();
		}
	}

	@Override
	public void onLoadComplete(ArrayList<Transport> array) {
		if (_currentActivity.getClass() == MainActivity.class) {
			((MainActivity)_currentActivity).showTransportListOnMap(array);
		}
	}

	@Override
	public void onAllRoutesLoadComplete(ArrayList<Transport> array) {
		if (_currentActivity.getClass() == SelectRouteActivity.class) {
			((SelectRouteActivity)_currentActivity).showTransportList();
		}
	}

	@Override
	public void onImgLoadComplete(Bitmap img) {
		if (_currentActivity.getClass() == MainActivity.class) {
			((MainActivity)_currentActivity).showTransportImgOnMap(img);
		}
		
	}
}