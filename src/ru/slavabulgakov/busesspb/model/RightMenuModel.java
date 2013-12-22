package ru.slavabulgakov.busesspb.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import ru.slavabulgakov.busesspb.model.Loader.State;
import ru.slavabulgakov.busesspb.model.RoutesNamesLoaderContainer.RouteName;
import ru.slavabulgakov.busesspb.paths.Forecast;
import ru.slavabulgakov.busesspb.paths.Forecasts;

public class RightMenuModel {
	private Model _model;
	private RequestQueue _queue;

	public RightMenuModel(Model model) {
		_model = model;
	}
	
	public interface Listener {
		void onForecastLoaded(Forecasts forecasts);
	}
	private Listener _listener;
	public void setListener(Listener listener) {
		_listener = listener;
	}

//	private ArrayList<RouteName> _routesNames;
//	public ArrayList<RouteName> getRoutesNames() {
//		if (_routesNames == null) {
//			_routesNames = new ArrayList<RouteName>();
//		}
//		return _routesNames;
//	}
//	private boolean _isStaticRoutesNamesLoading = false;
//	public boolean isStaticRoutesNamesLoading() {
//		return _isStaticRoutesNamesLoading;
//	}
//	private boolean _isStaticRoutesNamesLoaded = false;
//	public boolean isStaticRoutesNamesLoaded() {
//		return _isStaticRoutesNamesLoaded;
//	}
//	private boolean _isRoutesNamesLoading = false;
//	public boolean isRoutesNamesLoading() {
//		return _isRoutesNamesLoading;
//	}
//	private boolean _isRoutesNamesLoaded = false;
//	public boolean isRoutesNamesLoaded() {
//		return _isRoutesNamesLoaded;
//	}
//	public void loadRoutesNames() {
//		_isStaticRoutesNamesLoading = true;
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				ArrayList<String> strings = Files.stringsArrayFromFile("routesNames.txt", _model);
//				_fullRoutesNames(strings);
//				
//				_isStaticRoutesNamesLoading = false;
//				_isStaticRoutesNamesLoaded = true;
//				_listener.onStaticRoutesNameLoadComplete();
//				
//				_isRoutesNamesLoading = true;
//				StringRequest request = new StringRequest("http://futbix.ru/busesspb/v1_0/routesdata/", new Response.Listener<String>() {
//
//					@Override
//					public void onResponse(String response) {
//						_isRoutesNamesLoading = false;
//						_isRoutesNamesLoaded = true;
//					 	ArrayList<String> strings = new ArrayList<String>(Arrays.asList(response.split("\n")));
//					 	_fullRoutesNames(strings);
//						_listener.onRoutesNamesLoadComplete(null);
//					}
//				}, new Response.ErrorListener() {
//
//					@Override
//					public void onErrorResponse(VolleyError error) {
//						int asf = 0;
//					}
//				});
//				_getQueue().add(request);
//			}
//		}).start();
//	}
//	private void _fullRoutesNames(ArrayList<String> strings) {
//		strings.remove(0);
//		ArrayList<RouteName>routesNames = new ArrayList<RouteName>();
//		for (String line : strings) {
//			String items[] = line.split(",");
//			RouteName routeName = new RouteName();
//			routeName.id = Integer.parseInt(items[0]);
//			routeName.number = items[2];
//			String kind = items[items.length - 4];
//			if (kind.equals("bus")) {
//				 routeName.kind = TransportKind.Bus;
//			} else if (kind.equals("tram")) {
//				routeName.kind = TransportKind.Tram;
//			} else if (kind.equals("trolley")) {
//				routeName.kind = TransportKind.Trolley;
//			} else if (kind.equals("ship")) {
//				routeName.kind = TransportKind.Ship;
//			} else {
//				routeName.kind = TransportKind.None;
//			}
//			
//			routesNames.add(routeName);
//		}
//		_routesNames = routesNames;
//	}
//	
	private RoutesNamesLoaderContainer _routesNamesContainer;
	public RouteName getRouteName(int id) {
		if (_routesNamesContainer == null) {
			_routesNamesContainer = (RoutesNamesLoaderContainer)getLoader(RoutesNamesLoaderContainer.class).getContainer();
		}
		for (Object obj : _routesNamesContainer.getData()) {
			RouteName routeName = (RouteName)obj;
			if (routeName.id == id) {
				return routeName;
			}
		}
		return null;
	}
	
	private RequestQueue _getQueue() {
		if (_queue == null) {
			_queue = Volley.newRequestQueue(_model);
		}
		return _queue;
	}
	
	private boolean _isForecastsLoading = false;
	private boolean _isForecastsLoaded = false;
	public boolean isForecastsLoading() {
		return _isForecastsLoading;
	}
	public boolean isForecastsLoaded() {
		return _isForecastsLoaded;
	}
	private Forecasts _lastLoadedForecasts;
	public Forecasts getLastLoadedForecasts() {
		return _lastLoadedForecasts;
	}
	public void loadForecastForStationId(String stationId) {
		_isForecastsLoading = true;
		JsonObjectRequest request = new JsonObjectRequest("http://transport.orgp.spb.ru/Portal/transport/internalapi/forecast/bystop?stopID=" + stationId, null, new Response.Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject response) {
				SimpleDateFormat format = new SimpleDateFormat("yyyyy-mm-dd hh:mm:ss", Locale.US);
				Forecasts forecasts = new Forecasts();
				try {
					JSONArray array = response.getJSONArray("result");
					for (int i = 0; i < array.length(); i++) {
						JSONObject item = array.getJSONObject(i);
						Forecast forecast = new Forecast();
						forecast.time = format.parse(item.getString("arrivingTime"));
						RouteName routeName = getRouteName(item.getInt("routeId"));
						forecast.transportNumber = routeName.number;
						forecast.transportKind = routeName.kind;
						forecasts.add(forecast);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				_isForecastsLoading = false;
				_isForecastsLoaded = true;
				_lastLoadedForecasts = forecasts;
				_listener.onForecastLoaded(forecasts);
			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				int z = 0;
			}
		});
		_getQueue().add(request);
	}
	
	private ArrayList<Loader> _loaders;
	private ArrayList<Loader> _getLoaders() {
		if (_loaders == null) {
			_loaders = new ArrayList<Loader>();
		}
		return _loaders;
	}
	public void loadForContainer(LoaderContainer container, Loader.Listener listener) {
		boolean exist = false;
		for (Loader loader : _getLoaders()) {
			if (loader.getContainer().getClass() == container.getClass()) {
				exist = true;
				break;
			}
		}
		if (!exist) {
			Loader loader = new Loader(container, _model, _getQueue());
			loader.setListener(listener);
			_getLoaders().add(loader);
			loader.load();
		}
	}
	
	public Loader getLoader(Class<?> cl) {
		for (Loader loader : _getLoaders()) {
			if (loader.getContainer().getClass() == cl) {
				if (loader.getState().getValue() > State.staticLoading.getValue()) {
					return loader;
				}
			}
		}
		return null;
	}
}
