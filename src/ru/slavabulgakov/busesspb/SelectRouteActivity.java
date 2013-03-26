package ru.slavabulgakov.busesspb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.slavabulgakov.busesspb.Model.Route;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SelectRouteActivity extends BaseActivity {
	
	private ListView _listView;
	private EditText _editText;
	private ProgressBar _progressBar;
	private Button _doneButton;
	
	class Adapter extends ArrayAdapter<Route> {
		private Filter _filter;
		private List<Route> _filtredList = Collections.synchronizedList(new ArrayList<Model.Route>());
		
		public Adapter() {
			super(SelectRouteActivity.this, R.layout.listitem_selectroute, _model.getAllRoutes());
			_filter = new Filter() {
				
				ArrayList<Route> _data = new ArrayList<Model.Route>();
				
				@Override
				protected void publishResults(CharSequence constraint, FilterResults results) {
					synchronized (this) {
						_filtredList.clear();
						if (results != null && results.count > 0) {
							@SuppressWarnings("unchecked")
							ArrayList<Route>objects = (ArrayList<Route>)results.values;
							for (Route route : objects) {
								_filtredList.add(route);
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
							for (Route route : _model.getAllRoutes()) {
								if (constraint.length() == 0 || route.routeNumber.contains(constraint)) {
									_data.add(route);
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
		public Route getItem(int position) {
			return _filtredList.get(position);
		}



		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = SelectRouteActivity.this.getLayoutInflater();
				convertView = inflater.inflate(R.layout.listitem_selectroute, parent, false);
			}
			((TextView)convertView.findViewById(R.id.listItemSelectRouteRouteName)).setText(_filtredList.get(position).routeNumber);
			int resId = -1;
			switch (_filtredList.get(position).kind) {
			case Bus:
				resId = R.drawable.bus;
				break;
				
			case Trolley:
				resId = R.drawable.trolley;
				break;
				
			case Tram:
				resId = R.drawable.tram;
				break;

			default:
				break;
			}
			((ImageView)convertView.findViewById(R.id.listItemSelectRouteKind)).setImageResource(resId);
			
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
		if (_model.getAllRoutes().size() == 0) {
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
		
		LinearLayout ticketsLayout = (LinearLayout)findViewById(R.id.selectRouteTickets);
		for (Route route : _model.getFavorite()) {
			Ticket ticket = new Ticket(this);
			ticket.setRoute(route);
			ticket.setOnRemoveListener(Contr.getInstance());
			ticketsLayout.addView(ticket);
		}
		putCloseAllButtonToTicketsLayout();
	}
	
	public void putCloseAllButtonToTicketsLayout() {
		LinearLayout ticketsLayout = (LinearLayout)findViewById(R.id.selectRouteTickets);
		if (_model.getFavorite().size() > 1) {
			if (ticketsLayout.getChildAt(0).getClass() != Button.class) {
				Button closeAllBtn = new Button(this);
				closeAllBtn.setOnClickListener(Contr.getInstance());
				closeAllBtn.setText("X");
				ticketsLayout.addView(closeAllBtn, 0);
			}
		} else {
			if (ticketsLayout.getChildCount() > 0) {
				if (ticketsLayout.getChildAt(0).getClass() == Button.class) {
					ticketsLayout.removeViewAt(0);
				}
			}
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