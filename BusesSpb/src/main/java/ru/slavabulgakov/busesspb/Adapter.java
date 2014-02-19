package ru.slavabulgakov.busesspb;

import android.app.Activity;
import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ru.slavabulgakov.busesspb.controls.TransportCellViewHolder;
import ru.slavabulgakov.busesspb.model.Model;
import ru.slavabulgakov.busesspb.model.Route;

public class Adapter extends ArrayAdapter<Route> {
	private MyFilter _filter;
	Context _context;
	Model _model;
	LayoutInflater _inflater;
	
	public class MyFilter extends Filter {

		CharSequence _constraint;
		
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			synchronized (this) {
				@SuppressWarnings("unchecked")
				ArrayList<Route>objects = (ArrayList<Route>)results.values;
                notifyDataSetChanged();
                clear();
				if (objects != null) {
					for (Route route : objects) {
						add(route);
					}
					notifyDataSetInvalidated();
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
			ArrayList<Route> data = new ArrayList<Route>();
			FilterResults filterResults = new FilterResults();
			if (constraint != null) {
				synchronized (this) {
					for (Route route : _model.getAllRoutes()) {
						if ((constraint.length() == 0 || route.routeNumber.toLowerCase().contains(constraint.toString().toLowerCase())) && _model.isEnabledFilterMenu(route.kind)) {
							data.add(route);
						}
					}
				}
			}

			Collections.sort(data, new Comparator<Route>() {

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
				filterResults.values = data;
				filterResults.count = data.size();
			}
			return filterResults;
		}
		
		public void filterByCurrentPrams() {
			filter(_constraint);
		}
		
	}
	
	public Adapter(Context context, Model model) {
		super(context, R.layout.listitem_selectroute, new ArrayList<Route>());
		_context = context;
		_model = model;
		_filter = new MyFilter();
		_inflater = ((Activity)_context).getLayoutInflater();
	}
	
	public void removeRoute(int position, View view) {
		TransportCellViewHolder vh = (TransportCellViewHolder)view.getTag();
		vh.needInflate = true;
        remove(getItem(position));
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = _inflater.inflate(R.layout.listitem_selectroute, parent, false);
			_setViewHolder(convertView);
		} else if (((TransportCellViewHolder)convertView.getTag()).needInflate) {
			convertView = _inflater.inflate(R.layout.listitem_selectroute, parent, false);
			_setViewHolder(convertView);
		}
		
		TransportCellViewHolder vh = (TransportCellViewHolder)convertView.getTag();
		
		vh.leftText.setText(getItem(position).routeNumber);
		Pair<Integer, Integer> res = vh.backgroundAndIconByKind(getItem(position).kind);
		convertView.setBackgroundResource(res.first);
		vh.leftIcon.setImageResource(res.second);
		
		Integer cost = getItem(position).cost;
		TextView costTextView = vh.rightText;
		ImageView currency = vh.rightIcon;
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
		TransportCellViewHolder vh = new TransportCellViewHolder();
		vh.leftIcon = (ImageView)view.findViewById(R.id.listItemSelectRouteKind);
		vh.leftText = (TextView)view.findViewById(R.id.listItemSelectRouteRouteName);
		vh.rightText = (TextView)view.findViewById(R.id.listItemSelectRouteCost);
		vh.rightIcon = (ImageView)view.findViewById(R.id.currency);
		vh.needInflate = false;
		view.setTag(vh);
	}

	@Override
	public MyFilter getFilter() {
		return _filter;
	}
}
