package ru.slavabulgakov.busesspb;

import java.util.ArrayList;
import ru.slavabulgakov.busesspb.Model.OnLoadCompleteListener;
import ru.slavabulgakov.busesspb.Model.Transport;
import ru.slavabulgakov.busesspb.SelectRouteActivity.Adapter;
import ru.slavabulgakov.busesspb.Ticket.OnRemoveListener;

import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.CameraPosition;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class Contr implements OnClickListener, OnCameraChangeListener, OnLoadCompleteListener, TextWatcher, OnItemClickListener, OnRemoveListener {
	
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
		case R.id.mainButton:
			_currentActivity.startActivity(new Intent(_currentActivity, SelectRouteActivity.class));
			break;
		
		case R.id.selectRouteDone:
			_model.saveFavorite();
			_currentActivity.finish();
			break;
			
		default:
			break;
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onCameraChange(CameraPosition arg0) {
		if (_currentActivity.getClass() == MainActivity.class) {
			((MainActivity)_currentActivity).updateTransport();
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

	@Override
	public void afterTextChanged(Editable s) {
		String text = s.toString();
//		String filterString = "";
//		if (text.length() != 0) {
//			char lastChar = text.charAt(text.length() - 1);
//			if (lastChar != ',') {
//				String[] textBlocks = text.split(","); 
//				filterString = textBlocks[textBlocks.length - 1]; 
//			}
//		}
		ListView listView = (ListView) _currentActivity.findViewById(R.id.selectRouteListView);
		if (listView.getAdapter() != null) {
			((Adapter)listView.getAdapter()).getFilter().filter(text);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//		EditText editText = (EditText)_currentActivity.findViewById(R.id.selectRouteText);
//		String text = editText.getText().toString();
//		if (text.length() != 0) {
//			char lastChar = text.charAt(text.length() - 1);
//			if (lastChar != ',') {
//				int startRemoveIndex = 0;
//				if (text.contains(",")) {
//					for (int i = 0; i < text.length(); i++) {
//						if (text.charAt(i) == ',') {
//							startRemoveIndex = i + 1;
//						}
//					}
//				}
//				text = text.substring(0, startRemoveIndex);
//			}
//		}
//		editText.setText(text + cellText + ",");

		LinearLayout ll = (LinearLayout)_currentActivity.findViewById(R.id.selectRouteTickets);
		ListView listView = (ListView) _currentActivity.findViewById(R.id.selectRouteListView);
		Transport transport = ((Adapter)listView.getAdapter()).getItem(position);
		Ticket ticket = new Ticket(_currentActivity, null);
		ticket.setTransport(transport);
		ticket.setOnRemoveListener(this);
		ll.addView(ticket);
		_model.getFavorite().add(transport);
	}

	@Override
	public void onRemove(Ticket ticket) {
		_model.getFavorite().remove(ticket.getTransport());
	}
}