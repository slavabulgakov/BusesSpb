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
import ru.slavabulgakov.busesspb.model.StationsContainer;
import ru.slavabulgakov.busesspb.paths.Station;
import ru.slavabulgakov.busesspb.paths.Stations;

public class StationsAdapter extends ArrayAdapter<Station> {
	private MyFilter _filter;
	Context _context;
	Model _model;
    Stations _nearblyStations;

    public void setNearblyStations(Stations nearblyStations) {
        _nearblyStations = nearblyStations;
    }

	public class MyFilter extends Filter {

		CharSequence _constraint;

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			synchronized (this) {
				clear();
                //noinspection unchecked
                ArrayList<Station>objects = (ArrayList<Station>)results.values;
				if (objects != null) {
					for (Station station : objects) {
						add(station);
					}
					notifyDataSetChanged();
				}
			}
		}

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			_constraint = constraint;
			ArrayList<Station> data = new ArrayList<Station>();
			FilterResults filterResults = new FilterResults();
			if (constraint != null) {
				synchronized (this) {
                    ArrayList<Object> list = _model.getRightMenuModel().getLoader(StationsContainer.class).getContainer().getData();
                    for (Object object : list) {
                        Station station = (Station)object;
                        if (station.name.toLowerCase().contains(constraint.toString().toLowerCase())) {
                            data.add(station);
                        }
                    }
                }

                Collections.sort(data, new Comparator<Station>() {
                    @Override
                    public int compare(Station lhs, Station rhs) {
                        return lhs.name.compareToIgnoreCase(rhs.name);
                    }
                });
			}

			synchronized (this) {
                if (_constraint.length() == 0) {
                    filterResults.values = _nearblyStations;
                    filterResults.count = _nearblyStations.size();
                } else {
                    filterResults.values = data;
                    filterResults.count = data.size();
                }
			}
			return filterResults;
		}

		public void filterByCurrentParams() {
			filter(_constraint);
		}

	}

	public StationsAdapter(Context context, Model model, Stations nearblyStations) {
		super(context, R.layout.listitem_forecast);
        _nearblyStations = nearblyStations;
		_context = context;
		_model = model;
		_filter = new MyFilter();
	}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listitem_forecast, parent, false);
            _setViewHolder(convertView);
        } else if (((TransportCellViewHolder)convertView.getTag()).needInflate) {
            convertView = inflater.inflate(R.layout.listitem_forecast, parent, false);
            _setViewHolder(convertView);
        }

        Station station = getItem(position);

        TransportCellViewHolder vh = (TransportCellViewHolder)convertView.getTag();
        vh.rightText.setText("");
        vh.leftText.setText(station.name);
        Pair<Integer, Integer> res = vh.backgroundAndIconByKind(station.kind);
        convertView.setBackgroundResource(res.first);
        vh.leftIcon.setImageResource(res.second);

        return convertView;
    }

    private void _setViewHolder(View view) {
        TransportCellViewHolder vh = new TransportCellViewHolder();
        vh.leftIcon = (ImageView)view.findViewById(R.id.listItemForecastKind);
        vh.leftText = (TextView)view.findViewById(R.id.listItemForecastRouteName);
        vh.rightText = (TextView)view.findViewById(R.id.listItemForecastTime);
        vh.needInflate = false;
        view.setTag(vh);
    }

	@Override
	public MyFilter getFilter() {
		return _filter;
	}
}
