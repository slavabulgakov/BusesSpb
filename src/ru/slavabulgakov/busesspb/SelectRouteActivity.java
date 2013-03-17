package ru.slavabulgakov.busesspb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.slavabulgakov.busesspb.Model.Transport;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SelectRouteActivity extends BaseActivity {
	
	private ListView _listView;
	private EditText _editText;
	private ProgressBar _progressBar;
	private Button _doneButton;
	
	class Adapter extends ArrayAdapter<Transport> {
		private Filter _filter;
		private List<Transport> _filtredList = Collections.synchronizedList(new ArrayList<Model.Transport>());
		
		public Adapter() {
			super(SelectRouteActivity.this, R.layout.listitem_selectroute, _model.getAll());
			_filter = new Filter() {
				
				ArrayList<Transport> _data = new ArrayList<Model.Transport>();
				
				@Override
				protected void publishResults(CharSequence constraint, FilterResults results) {
					synchronized (this) {
						_filtredList.clear();
						if (results != null && results.count > 0) {
							@SuppressWarnings("unchecked")
							ArrayList<Transport>objects = (ArrayList<Transport>)results.values;
							for (Transport transport : objects) {
								_filtredList.add(transport);
							}
							notifyDataSetChanged();
						}
					}
				}
				
				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					_data.clear();
					FilterResults filterResults = new FilterResults();
					if (constraint != null) {
						synchronized (this) {
							for (Transport transport : _model.getAll()) {
								if (constraint.length() == 0 || transport.routeNumber.contains(constraint)) {
									_data.add(transport);
								}
							}
						}
					}
					synchronized (this) {
						filterResults.values = _data;
						filterResults.count = _data.size();
					}
					return filterResults;
				}
			};
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = SelectRouteActivity.this.getLayoutInflater();
				convertView = inflater.inflate(R.layout.listitem_selectroute, parent, false);
			}
			((TextView)convertView.findViewById(R.id.listItemSelectRouteRouteName)).setText(_filtredList.get(position).routeNumber);
			return convertView;
		}

		@Override
		public int getCount() {
			return _filtredList.size();
		}

		@Override
		public Filter getFilter() {
			return _filter;
		}
		
	}
 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selectroute);
		
		_progressBar = (ProgressBar)findViewById(R.id.selectRouteProgressBar);
		
		_editText = (EditText)findViewById(R.id.selectRouteText);
		_editText.addTextChangedListener(Contr.getInstance());
		
		_listView = (ListView)findViewById(R.id.selectRouteListView);
		if (_model.getAll().size() == 0) {
			_progressBar.setVisibility(View.VISIBLE);
			_listView.setVisibility(View.INVISIBLE);
			_editText.setEnabled(false);
			_model.loadDataForAllRoutes(Contr.getInstance());
		} else {
			showTransportList();
		}
		_listView.setOnItemClickListener(Contr.getInstance());
		
		_doneButton = (Button)findViewById(R.id.selectRouteDone);
		_doneButton.setOnClickListener(Contr.getInstance());
		
		LinearLayout ll = (LinearLayout)findViewById(R.id.selectRouteTickets);
		for (Transport transport : _model.getFavorite()) {
			Ticket ticket = new Ticket(this);
			ticket.setTransport(transport);
			ll.addView(ticket);
		}
	}
	
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	        _model.saveFavorite();
	    }
		return super.onKeyDown(keyCode, event);
	}



	public void showTransportList() {
		_progressBar.setVisibility(View.INVISIBLE);
		_listView.setVisibility(View.VISIBLE);
		_editText.setEnabled(true);
		Adapter adapter = new Adapter();
		_listView.setAdapter(adapter);
		adapter.getFilter().filter(_editText.getText());
	}
}