package ru.slavabulgakov.busesspb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

class Adapter extends ArrayAdapter<Route> {
	private Filter _filter;
	private List<Route> _filtredList = Collections.synchronizedList(new ArrayList<Route>());
	Context _context;
	Model _model;
	
	public Adapter(Context context, Model model) {
		super(context, R.layout.listitem_selectroute, model.getAllRoutes());
		_context = context;
		_model = model;
		_filter = new Filter() {
			
			ArrayList<Route> _data = new ArrayList<Route>();
			
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
			LayoutInflater inflater = ((Activity)_context).getLayoutInflater();
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
		
		Integer cost = _filtredList.get(position).cost;
		TextView costTextView = (TextView)convertView.findViewById(R.id.listItemSelectRouteCost);
		if (cost == null) {
			costTextView.setVisibility(View.GONE);
		} else {
			costTextView.setText(cost.toString());
			costTextView.setVisibility(View.VISIBLE);
		}
		
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