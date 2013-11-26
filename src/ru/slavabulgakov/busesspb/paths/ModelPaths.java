package ru.slavabulgakov.busesspb.paths;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import ru.slavabulgakov.busesspb.Files;
import ru.slavabulgakov.busesspb.LoadTaskException;
import ru.slavabulgakov.busesspb.Mercator;
import ru.slavabulgakov.busesspb.Mercator.AxisType;
import ru.slavabulgakov.busesspb.ParserWebPageTask.IRequest;
import ru.slavabulgakov.busesspb.model.Model;
import ru.slavabulgakov.busesspb.model.Route;
import android.annotation.SuppressLint;
import android.util.Pair;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

public class ModelPaths {
	private static String PATH_FILE_NAME = "paths.ser";
	private static String STATIONS_FILE_NAME = "stations.ser";
	private Model _model;
	private RequestQueue _queue;
	public ModelPaths(Model model) {
		_model = model;
	}
	
	public interface OnPathLoaded {
		void onPathLoaded(Path path);
		void onStationsLoaded(Stations stations);
	}
	private OnPathLoaded _listener;
	public void setListener(OnPathLoaded listener) {
		_listener = listener;
	}
	
	private HashMap<Integer, Path> _paths;
	@SuppressWarnings("unchecked")
	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, Path> _getPaths() {
		if (_paths == null) {
			_paths = (HashMap<Integer, Path>)Files.loadFromFile(PATH_FILE_NAME, _model);
		}
		if (_paths == null) {
			_paths = new HashMap<Integer, Path>();
		}
		return _paths;
	}
	private HashMap<Integer, Stations> _stationsOfRoutes;
	@SuppressWarnings("unchecked")
	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, Stations> _getStations() {
		if (_stationsOfRoutes == null) {
			_stationsOfRoutes = (HashMap<Integer, Stations>)Files.loadFromFile(STATIONS_FILE_NAME, _model);
		}
		
		if (_stationsOfRoutes == null) {
			_stationsOfRoutes = new HashMap<Integer, Stations>();
		}
		return _stationsOfRoutes;
	}
	public void loadPaths() {
		if (pathsIsOn()) {
			ArrayList<Integer> routeIds = new ArrayList<Integer>();
			for (Route route : _model.getFavorite()) {
				routeIds.add(route.id);
			}
			
			removeMapItems();
			removeMapShortTimeItems();
			for (Integer routeId : routeIds) {
				Path path = _getPaths().get(routeId);
				if (path == null) {
					_loadPathForRoute(routeId);
				} else {
					_listener.onPathLoaded(path);
				}
				
				Stations stations = _getStations().get(routeId);
				if (stations == null) {
					_loadStationsForRoute(routeId);
				} else {
					_listener.onStationsLoaded(stations);
				}
			}
		}
	}
	private void _loadPathForRoute(final Integer routeId) {
		
		final int requestId = routeId + 100000;
		IRequest req = new IRequest() {
			
			boolean _canceled;
			int _step = 0;
			Path _path;
			
			@Override
			public void setCanceled() {
				_canceled = true;
			}
			
			@Override
			public void nextExecute() {
				URL url;
				try {
					String cookie = null;
					while ((cookie = _model.getCookie()) == null);
					url = new URL("http://transport.orgp.spb.ru/Portal/transport/map/stage?ROUTE=" + routeId.toString() + "&SERVICE=WFS&VERSION=1.0.0&REQUEST=GetFeature&SRS=EPSG%3A900913&BBOX=3348677.355466,8345758.8650171,3403154.3312728,8401957.8141971");
					URLConnection conn = url.openConnection();
					conn.setRequestProperty("Cookie", cookie);
					conn.connect();
					BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					String line = "";
		  
					String json = "";
					while ((line = rd.readLine()) != null) {
						json += line;
						if (_canceled) {
							throw new LoadTaskException();
						}
					}
					
					Mercator m = new Mercator();
					
					JSONObject jo = new JSONObject(json);
					JSONArray ja = jo.getJSONArray("features");
					_path = new Path();
					for (int i = 0; i < ja.length(); i++) {
						SubPath subPath = new SubPath();
						JSONArray coordinates = ja.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates");
						for (int j = 0; j < coordinates.length(); j++) {
							JSONArray coordinate = coordinates.getJSONArray(j);
							LatLng latlng = new LatLng(m.deg(coordinate.getDouble(1), AxisType.LAT), m.deg(coordinate.getDouble(0), AxisType.LNG));
							subPath.add(new Point(latlng));
						}
						_path.add(subPath);
						if (_canceled) {
							throw new LoadTaskException();
						}
					}
				} catch (LoadTaskException e) {
					_path = null;
				} catch (Exception e) {
					e.printStackTrace();
				}
				_step++;
			}
			
			@Override
			public boolean needExecute() {
				return _step == 0;
			}
			
			@Override
			public void finish() {
				if (_model.isOnline()) {
					_getPaths().put(routeId, _path);
					_listener.onPathLoaded(_path);
				}
				_model.removeParserById(requestId);
			}

			@Override
			public int getRequestId() {
				return requestId;
			}
		};
		
		_model.startParserWithId(req, requestId);
	}
	
	public void loadStationForId(String stationId) {
		if (_queue == null) {
			_queue = Volley.newRequestQueue(_model);
		}
		JsonObjectRequest request = new JsonObjectRequest("http://transport.orgp.spb.ru/Portal/transport/internalapi/forecast/bystop?stopID=" + stationId, null, new Response.Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject response) {
				int sx = 0;
			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				int z = 0;
			}
		});
		_queue.add(request);
	}
	
	public void save() {
		Files.saveToFile(_getPaths(), PATH_FILE_NAME, _model);
		Files.saveToFile(_getStations(), STATIONS_FILE_NAME, _model);
	}
	
	private void _loadStationsForRoute(final Integer routeId) {
		final int requestId = routeId + 200000;
		IRequest req = new IRequest() {
			
			boolean _canceled;
			int _step = 0;
			Stations _stations;
			
			@Override
			public void setCanceled() {
				_canceled = true;
			}
			
			@Override
			public void nextExecute() {
				URL url;
				try {
					String cookie = null;
					while ((cookie = _model.getCookie()) == null);
					url = new URL("http://transport.orgp.spb.ru/Portal/transport/map/poi?ROUTE=" + routeId.toString() + "&REQUEST=GetFeature");
					URLConnection conn = url.openConnection();
					conn.setRequestProperty("Cookie", cookie);
					conn.connect();
					BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					String line = "";
		  
					String json = "";
					while ((line = rd.readLine()) != null) {
						json += line;
						if (_canceled) {
							throw new LoadTaskException();
						}
					}
					
					Mercator m = new Mercator();
					
					JSONObject jo = new JSONObject(json);
					JSONArray ja = jo.getJSONArray("features");
					_stations = new Stations();
					for (int i = 0; i < ja.length(); i++) {
						Station station = new Station();
						JSONArray coordinates = ja.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates");
						LatLng latlng = new LatLng(m.deg(coordinates.getDouble(1), AxisType.LAT), m.deg(coordinates.getDouble(0), AxisType.LNG));
						String name = ja.getJSONObject(i).getJSONObject("properties").getString("name");
						String id = ja.getJSONObject(i).getJSONObject("properties").getString("id");
						station.point = new Point(latlng);
						station.name = name;
						station.id = id;
						_stations.add(station);
						if (_canceled) {
							throw new LoadTaskException();
						}
					}
				} catch (LoadTaskException e) {
					_stations = null;
				} catch (Exception e) {
					e.printStackTrace();
				}
				_step++;
			}
			
			@Override
			public boolean needExecute() {
				return _step == 0;
			}
			
			@Override
			public void finish() {
				if (_model.isOnline()) {
					_getStations().put(routeId, _stations);
					_listener.onStationsLoaded(_stations);
				}
				_model.removeParserById(requestId);
			}

			@Override
			public int getRequestId() {
				return requestId;
			}
		};
		
		_model.startParserWithId(req, requestId);
	}
	
	private ArrayList<Object>_mapItems;
	private ArrayList<Object> _getMapItems() {
		if (_mapItems == null) {
			_mapItems = new ArrayList<Object>();
		}
		return _mapItems;
	}
	public void removeMapItems() {
		for (Object item : _getMapItems()) {
			if (item.getClass() == Marker.class) {
				((Marker)item).remove();
			} else if (item.getClass() == Polyline.class) {
				((Polyline)item).remove();
			} else if (item.getClass() == GroundOverlay.class) {
				((GroundOverlay)item).remove();
			}
		}
	}
	public void addMapItem(Object item) {
		_getMapItems().add(item);
	}
	public boolean isStationMarker(Object item) {
		boolean exist = _getMapItems().contains(item);
		boolean isMarker = item.getClass() == Pair.class;
		return exist && isMarker;
	}
	
	@SuppressWarnings("unchecked")
	public Station getStationByMarker(Marker marker) {
		for (Object object : _getMapItems()) {
			if (object.getClass() == Pair.class) {
				Pair<Marker, Station> pair = (Pair<Marker, Station>)object;
				if (pair.first.equals(marker)) {
					return pair.second;
				}
			}
		}
		return null;
	}
	
	private ArrayList<Object>_mapShortTimeItems;
	private ArrayList<Object> _getMapShortTimeItems() {
		if (_mapShortTimeItems == null) {
			_mapShortTimeItems = new ArrayList<Object>();
		}
		return _mapShortTimeItems;
	}
	public void removeMapShortTimeItems() {
		for (Object item : _getMapShortTimeItems()) {
			if (item.getClass() == Marker.class) {
				((Marker)item).remove();
			} else if (item.getClass() == Polyline.class) {
				((Polyline)item).remove();
			} else if (item.getClass() == GroundOverlay.class) {
				((GroundOverlay)item).remove();
			}
		}
	}
	public void addMapShortTimeItem(Object item) {
		_getMapShortTimeItems().add(item);
	}
	
	public boolean pathsIsOn() {
		Boolean on = (Boolean)_model.getData("pathsIsOn");
		if (on != null) {
			return on;
		}
		return false;
	}
	public void setPathsOn(boolean on) {
		_model.setData("pathsIsOn", on, true);
		if (on) {
			loadPaths();
		} else {
			removeMapItems();
			removeMapShortTimeItems();
		}
	}
	
	public void updateStations() {
		if (pathsIsOn()) {
			ArrayList<Integer> routeIds = new ArrayList<Integer>();
			for (Route route : _model.getFavorite()) {
				routeIds.add(route.id);
			}
			
			removeMapShortTimeItems();
			for (Integer routeId : routeIds) {
				Stations stations = _getStations().get(routeId);
				if (stations != null) {
					_listener.onStationsLoaded(stations);
				}
			}
		}
	}
}
