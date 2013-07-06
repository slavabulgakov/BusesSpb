package ru.slavabulgakov.busesspb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ru.slavabulgakov.busesspb.model.Model;
import ru.slavabulgakov.busesspb.model.Route;

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
	private MyFilter _filter;
	private List<Route> _filtredList = Collections.synchronizedList(new ArrayList<Route>());
	Context _context;
	Model _model;
	LayoutInflater _inflater;
	
	class MyFilter extends Filter {

		ArrayList<Route> _data = new ArrayList<Route>();
		CharSequence _constraint;
		
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			synchronized (this) {
				_filtredList.clear();
				@SuppressWarnings("unchecked")
				ArrayList<Route>objects = (ArrayList<Route>)results.values;
				if (objects != null) {
					for (Route route : objects) {
						_filtredList.add(route);
					}
					notifyDataSetChanged();
				}
			}
		}
		
		private int _routeNumberToInt(String routeNumber) {
			int result = -1;
			boolean next = false;
			do {
				try {
					result = Integer.parseInt(routeNumber);
					next = false;
				} catch (NumberFormatException e) {
					if (routeNumber.length() > 1) {
						routeNumber = routeNumber.substring(0, routeNumber.length() - 2);
						next = true;
					} else {
						next = false;
					}
				}
			} while (next);
			
			return result;
		}
		
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			_constraint = constraint;
			_data.clear();
			FilterResults filterResults = new FilterResults();
			if (constraint != null) {
				synchronized (this) {
					for (Route route : _model.getAllRoutes()) {
						if ((constraint.length() == 0 || route.routeNumber.toLowerCase().contains(constraint.toString().toLowerCase())) && _model.isEnabledFilterMenu(route.kind)) {
							_data.add(route);
						}
					}
				}
			}
			
			
			Collections.sort(_data, new Comparator<Route>() {

				@Override
				public int compare(Route lhs, Route rhs) {
					int left = 0;
					int right = 0;
					int result = 0;
					
					
					left = lhs.routeNumber.length();
					right = rhs.routeNumber.length();
					result = left - right;

					
					if (result == 0) {
						left = _routeNumberToInt(lhs.routeNumber);
						right = _routeNumberToInt(rhs.routeNumber);
						result = left - right;
					}
					
					
					if (result == 0) {
						result = lhs.routeNumber.compareToIgnoreCase(rhs.routeNumber);
					}
					 
					if (result == 0) {
						left = _model.enumKindToInt(lhs.kind);
						right = _model.enumKindToInt(rhs.kind);
						result = left - right;
					}
					return result;
				}
			});
			
			
			synchronized (this) {
				filterResults.values = _data;
				filterResults.count = _data.size();
			}
			return filterResults;
		}
		
		public void filterByCurrentPrams() {
			filter(_constraint);
		}
		
	}
	
	private class ViewHolder {
		public boolean needInflate;
		public TextView routeNumber;
		public ImageView routeKindImg;
		public TextView cost;
		public ImageView currency;
	}
	
	public Adapter(Context context, Model model) {
		super(context, R.layout.listitem_selectroute, model.getAllRoutes());
		_context = context;
		_model = model;
		_filter = new MyFilter();
		_inflater = ((Activity)_context).getLayoutInflater();
	}
	
	@Override
	public Route getItem(int position) {
		return _filtredList.get(position);
	}

	public void removeRoute(int position, View view) {
		ViewHolder vh = (ViewHolder)view.getTag();
		vh.needInflate = true;
		_filtredList.remove(position);
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = _inflater.inflate(R.layout.listitem_selectroute, parent, false);
			_setViewHolder(convertView);
		} else if (((ViewHolder)convertView.getTag()).needInflate) {
			convertView = _inflater.inflate(R.layout.listitem_selectroute, parent, false);
			_setViewHolder(convertView);
		}
		
		ViewHolder vh = (ViewHolder)convertView.getTag();
		
		vh.routeNumber.setText(_filtredList.get(position).routeNumber);
		int bgResId = -1;
		int iconResId = -1;
		switch (_filtredList.get(position).kind) {
		case Bus:
			bgResId = R.drawable.listitem_bg_bus;
			iconResId = R.drawable.bus_30_30;
			break;
			
		case Trolley:
			bgResId = R.drawable.listitem_bg_trolley;
			iconResId = R.drawable.trolley_30_30;
			break;
			
		case Tram:
			bgResId = R.drawable.listitem_bg_tram;
			iconResId = R.drawable.tram_30_30;
			break;
			
		case Ship:
			bgResId = R.drawable.listitem_bg_ship;
			iconResId = R.drawable.ship_30_30;
			break;

		default:
			break;
		}
		
		convertView.setBackgroundResource(bgResId);
		vh.routeKindImg.setImageResource(iconResId);
		
		Integer cost = _filtredList.get(position).cost;
		TextView costTextView = vh.cost;
		ImageView currency = vh.currency;
		if (cost == null) {
			costTextView.setVisibility(View.INVISIBLE);
			currency.setVisibility(View.INVISIBLE);
		} else {
			costTextView.setText(cost.toString());
			costTextView.setVisibility(View.VISIBLE);
			currency.setVisibility(View.VISIBLE);
		}
		
		return convertView;
	}
	
	private void _setViewHolder(View view) {
		ViewHolder vh = new ViewHolder();
		vh.routeKindImg = (ImageView)view.findViewById(R.id.listItemSelectRouteKind);
		vh.routeNumber = (TextView)view.findViewById(R.id.listItemSelectRouteRouteName);
		vh.cost = (TextView)view.findViewById(R.id.listItemSelectRouteCost);
		vh.currency = (ImageView)view.findViewById(R.id.currency);
		vh.needInflate = false;
		view.setTag(vh);
	}

	@Override
	public int getCount() {
		return _filtredList.size();
	}

	@Override
	public MyFilter getFilter() {
		return _filter;
	}
}
