package ru.slavabulgakov.busesspb;

import ru.slavabulgakov.busesspb.Model.Transport;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SelectRouteActivity extends BaseActivity {
	
	class Adapter extends ArrayAdapter<Transport> {
		
		public Adapter() {
			super(SelectRouteActivity.this, R.layout.listitem_selectroute, _model.transportList);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = SelectRouteActivity.this.getLayoutInflater();
				convertView = inflater.inflate(R.layout.listitem_selectroute, parent, false);
			}
			((TextView)convertView.findViewById(R.id.listItemSelectRouteRouteName)).setText(_model.transportList.get(position).routeNumber);
			return super.getView(position, convertView, parent);
		}
		
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selectroute);
		ListView listView = (ListView)findViewById(R.id.routesListView);
		Adapter adapter = new Adapter();
		listView.setAdapter(adapter);
	}
	
}
