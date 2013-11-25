package ru.slavabulgakov.busesspb.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.flurry.android.FlurryAgent;
import com.flurry.org.apache.avro.io.parsing.Symbol.Kind;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import ru.slavabulgakov.busesspb.Files;
import ru.slavabulgakov.busesspb.FlurryConstants;
import ru.slavabulgakov.busesspb.LoadTaskException;
import ru.slavabulgakov.busesspb.Mercator;
import ru.slavabulgakov.busesspb.ParserWebPageTask;
import ru.slavabulgakov.busesspb.ShareModel;
import ru.slavabulgakov.busesspb.Mercator.AxisType;
import ru.slavabulgakov.busesspb.ParserWebPageTask.IRequest;
import ru.slavabulgakov.busesspb.paths.ModelPaths;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Model extends Application {
	
 	public enum MenuKind {
		Left,
		Right
	};
	
	private ModelPaths _paths;
	public Model() {
		_paths = new ModelPaths(this);
	}
	
	public ModelPaths getModelPaths() {
		return _paths;
	}
	
	private static final int BUS_FILTER = 1;
	private static final int TROLLEY_FILTER = 2;
	private static final int TRAM_FILTER = 4;
	private static final int SHIP_FILTER = 8;
	
	private static final String STORAGE_NAME = "busesspb";
	
	private Map<String, Object> _data;
	public void setData(String key, Object value) {
		if (_data == null) {
			_data = new HashMap<String, Object>();
		}
		_data.put(key, value);
	}
	public void setData(String key, Object value, boolean storage) {
		setData(key, value);
		if (storage) {
			SharedPreferences settings = getSharedPreferences(STORAGE_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			if (value.getClass() == Integer.class) {
				editor.putInt(key, (Integer)value);
			} else if (value.getClass() == Boolean.class) {
				editor.putBoolean(key, (Boolean)value);
			} else if (value.getClass() == Float.class) {
				editor.putFloat(key, (Float)value);
			} else if (value.getClass() == Long.class) {
				editor.putLong(key, (Long)value);
			} else if (value.getClass() == String.class) {
				editor.putString(key, (String)value);
			}
			editor.commit();
		}
	}
	public Object getData(String key) {
		Object value = null;
		if (_data != null) {
			value = _data.get(key);
		}
		if (value == null) {
			SharedPreferences settings = getSharedPreferences(STORAGE_NAME, 0);
			Map<String, ?>map = settings.getAll();
			value = map.get(key);
		}
		return value;
	}
	public Object getData(String key, Object defValue) {
		Object value = getData(key);
		if (value == null) {
			value = defValue;
		}
		return value;
	}
	public void removeData(String key) {
		if (_data != null) {
			_data.remove(key);
		}
	}

	private boolean _isLowMemory = false;
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		_isLowMemory = true;
		_lastSimpleTransportView = null;
		FlurryAgent.logEvent(FlurryConstants.lowMemory);
	}
	public boolean isFreeDays() {
		Long ms = (Long)getData("freeDays");
		if (ms != null) {
			Date date = new Date(ms);
			Date now = new Date();
			return now.getTime() - date.getTime() < 14 * 24 * 60 * 60 * 1000;
		}
		return false;
	}
	public void setFreeDays() {
		Date now = new Date();
		setData("freeDays", now.getTime(), true);
	}
	
	public int enumKindToInt(TransportKind kind) {
		switch (kind) {
		case Bus:
			return Model.BUS_FILTER;
			
		case Trolley:
			return Model.TROLLEY_FILTER;
			
		case Tram:
			return Model.TRAM_FILTER;
			
		case Ship:
			return Model.SHIP_FILTER;

		default:
			break;
		}
		
		return 0;
	}
	
	private void _setFilterToStorage(TransportKind kind, String name) {
		int filter = (Integer)getData(name, 0);
		int f = enumKindToInt(kind);
		filter ^= f;
		setData(name, filter, true);
	}
	
	private boolean _isEnabledFilterFromStorage(TransportKind kind, String name) {
		int filter = (Integer)getData(name, 0);
		int f = enumKindToInt(kind);
		return (filter & f) > 0 || filter == 0;
	}
	
	public void setFilter(TransportKind kind) {
		_setFilterToStorage(kind, "map_filter");
	}
	public boolean isEnabledFilter(TransportKind kind) {
		return _isEnabledFilterFromStorage(kind, "map_filter");
	}
	
	public void setFilterMenu(TransportKind kind) {
		_setFilterToStorage(kind, "menu_filter");
	}
	public boolean isEnabledFilterMenu(TransportKind kind) {
		return _isEnabledFilterFromStorage(kind, "menu_filter");
	}
	
	
	private LatLng _location;
	public LatLng getLocation() {
		if (_location == null) {
			SharedPreferences settings = getSharedPreferences(STORAGE_NAME, 0);
			double lat = (double)settings.getFloat("current_lat", (float) 59.946282);
			double lng = (double)settings.getFloat("current_lng", (float) 30.356412);
			_location = new LatLng(lat, lng);
		}
		return _location;
	}
	public void saveLocation() {
		if (_location != null) {
			SharedPreferences settings = getSharedPreferences(STORAGE_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putFloat("current_lat", (float)_location.latitude);
			editor.putFloat("current_lng", (float)_location.longitude);
			editor.commit();
		}
	}
	public void setLocation(LatLng location) {
		_location = location;
	}
	
	public boolean openAnimationIsShowed() {
		boolean isShowed = (Boolean)getData("open_animation_is_showed", false);
		return isShowed;
	}
	public void setOpenAnimationIsShowed() {
		setData("open_animation_is_showed", true, true);
	}
	
	private float _zoom = 0;
	public double getZoom() {
		if (_zoom == 0) {
			SharedPreferences settings = getSharedPreferences(STORAGE_NAME, 0);
			_zoom = settings.getFloat("zoom", 10);

		}
		return _zoom;
	}
	public void saveZoom() {
		SharedPreferences settings = getSharedPreferences(STORAGE_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putFloat("zoom", _zoom == 0 ? 10 : _zoom);
		editor.commit();

	}
	public void setZoom(float zoom) {
		_zoom = zoom;
	}
	
	
	private ArrayList<ParserWebPageTask> _parsers;
	private ArrayList<Route> _favoriteRoutes;
	private ArrayList<Route> _allRoutes;
	private OnLoadCompleteListener _listener;
	
	public interface OnLoadCompleteListener {
		void onTransportListOfRouteLoadComplete(ArrayList<Transport> array);
		void onRouteKindsLoadComplete(ArrayList<Route> array);
		void onImgLoadComplete(Bitmap img);
		void onInternetAccessDeny();
		void onInternetAccessSuccess();
	}
	
	public void setListener(OnLoadCompleteListener listener) {
		_listener = listener;
	}
	
	
	@SuppressWarnings("unchecked")
	public ArrayList<Route> getFavorite() {
		if (_favoriteRoutes == null) {
			_favoriteRoutes = (ArrayList<Route>)Files.loadFromFile("favoriteTransportList.ser", this);
			if (_favoriteRoutes == null) {
				_favoriteRoutes = new ArrayList<Route>();
			}
		}
		return _favoriteRoutes;
	}
	
	private void _removeRouteFromList(Route route, ArrayList<Route> list) {
		boolean exist = false;
		do {
			exist = false;
			int index = 0;
			for (Route route_ : list) {
				if (route.id.equals(route_.id)) {
					exist = true;
					break;
				}
				index++;
			}
			if (exist) {
				list.remove(index);
			}
		} while (exist);
	}
	
	public void setRouteToFavorite(Route route) {
		_removeRouteFromList(route, _allRoutes);
		_favoriteRoutes.add(route);
	}
	
	public void setRouteToAll(Route route) {
		_removeRouteFromList(route, _favoriteRoutes);
		_allRoutes.add(route);
	}
	
	public void saveFavorite() {
		Files.saveToFile(_favoriteRoutes, "favoriteTransportList.ser", this);
	}
	
	
	
	public ArrayList<Route> getAllRoutes() {
		if (_allRoutes == null) {
			_allRoutes = new ArrayList<Route>();
		}
		return _allRoutes;
	}
	private void _setAllRoutes(ArrayList<Route> all) {
		_allRoutesIsLoaded = true;
		_allRoutes = all;
		for (Route route : _favoriteRoutes) {
			_removeRouteFromList(route, _allRoutes);
		}
	}
	private boolean _allRoutesIsLoaded = false;
	public boolean allRouteIsLoaded() {
		return _allRoutesIsLoaded;
	}
	static final int PARSER_ID_ALL_ROUTES = 3;
	public boolean allRoutesIsLoading() {
		boolean loading = _indexParserOfId(PARSER_ID_ALL_ROUTES) != -1;
		return loading;
	}
	public void loadDataForAllRoutes() {
		ArrayList<Route> routeList = getFavorite();
		if (routeList.size() == 0) {
			routeList = getAllRoutes();
		}
		final int requestId = PARSER_ID_ALL_ROUTES;
		IRequest req = new IRequest() {
			
			int _step = 0;
			ArrayList<Route> _array;
			
			@Override
			public void setCanceled() {
			}
			
			@SuppressWarnings("unchecked")
			@Override
			public void nextExecute() {
				try {
					Integer version = (Integer)getData("allRoutesVersion");
					URL url = new URL("http://futbix.ru/busesspb/version/");
					URLConnection conn = url.openConnection();
					conn.connect();
					BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					String line = rd.readLine();
					Integer serverVersion = Integer.parseInt(line);
					if (version == null || serverVersion > version) {
						String json = "";
						if (serverVersion == 1) {
							json = Files.stringFromFile("allRoutes.json", Model.this);
						} else {
							url = new URL("http://futbix.ru/busesspb/echo/");
							conn = url.openConnection();
							conn.setRequestProperty("Cookie", _cookie);
							conn.connect();
							rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
							line = "";
				  
							while ((line = rd.readLine()) != null) {
								json += line;
							}
						}
						
						
						JSONObject response = new JSONObject(json);
				        
				        JSONArray aaData = response.getJSONArray("aaData");
				        _array = new ArrayList<Route>();
				        for (int i = 0; i < aaData.length(); i++) {
							JSONArray data = aaData.getJSONArray(i);
							Route route = new Route();
							route.id = data.getInt(0);
							route.routeNumber = data.getString(2);
							if (!data.getString(7).equals("null")) {
								route.cost = data.getInt(7);
							}
							String kind = data.getJSONObject(1).getString("systemName");
							if (kind.equals("bus")) {
								route.kind = TransportKind.Bus;
							} else if (kind.equals("tram")) {
								route.kind = TransportKind.Tram;
							} else if (kind.equals("trolley")) {
								route.kind = TransportKind.Trolley;
							} else if (kind.equals("ship")) {
								route.kind = TransportKind.Ship;
							}
							
							_array.add(route);
						}
				        Files.saveToFile(_array, "allRoutes.ser", Model.this);
						setData("allRoutesVersion", serverVersion, true);
					} else {
						_array = (ArrayList<Route>)Files.loadFromFile("allRoutes.ser", Model.this);
					}
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
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
				if (isOnline()) {
					if (_array != null) {
						if (_array.size() > 0) {
							_setAllRoutes(_array);
							_listener.onRouteKindsLoadComplete(_array);
						}
					}
				}
				removeParserById(requestId);
			}

			@Override
			public int getRequestId() {
				return requestId;
			}
		};
		
		startParserWithId(req, requestId);
	}
	
	
	
	
	private ArrayList<ParserWebPageTask> _getParsers() {
		if (_parsers == null) {
			_parsers = new ArrayList<ParserWebPageTask>();
		}
		return _parsers;
	}
	
	public void showFavoriteRoutes() {
		for (Route route : getFavorite()) {
			_loadDataForRoute(route);
		}
	}
	
	private String _cookie = null;
	private String _scope = null;
	private boolean _scopeCookieIsLoading = false;
	private void _loadDataForRoute(final Route route) {
		final int requestId = route.id;
		IRequest req = new IRequest() {
			
			boolean _canceled;
			int _step = 0;
			ArrayList<Transport> _array;
			
			@Override
			public void setCanceled() {
				_canceled = true;
			}
			
			@Override
			public void nextExecute() {
				URL url;
				try {
					if (_scope == null || _cookie == null) {
						if (_scopeCookieIsLoading) {
							_step++;
							return;
						}
						_scopeCookieIsLoading = true;
						url = new URL("http://transport.orgp.spb.ru/Portal/transport/route/1329");
						URLConnection conn = url.openConnection();
						conn.connect();
						BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						String line = "";
						
						String headerName=null;
						for (int i=1; (headerName = conn.getHeaderFieldKey(i))!=null; i++) {
						 	if (headerName.equals("Set-Cookie")) {                  
						 		_cookie = conn.getHeaderField(i).split(";")[0];
						 		break;
						 	}
						}
			  
						int index =  0;
						while ((line = rd.readLine()) != null) {
							if ((index = line.indexOf("scope")) != -1) {
								break;
							}
							if (_canceled) {
								throw new LoadTaskException();
							}
						}
						
						index += 8;
						int end = index;
						while (line.charAt(end) != '"') {
							end++;
						}
						_scope = URLEncoder.encode(line.substring(index, end));
						_scopeCookieIsLoading = false;
					}
//					url = new URL("http://transport.orgp.spb.ru/Portal/transport/map/routeVehicle?ROUTE=" + route.id.toString() + "&SERVICE=WFS&VERSION=1.0.0&REQUEST=GetFeature&SRS=EPSG%3A900913&LAYERS=&WHEELCHAIRONLY=false&_OLSALT=0.4202592596411705&BBOX=3272267.2330292,8264094.7670049,3479564.4537096,8483621.912209");
					url = new URL("http://transport.orgp.spb.ru/Portal/transport/mapx/innerRouteVehicle?ROUTE=" + route.id.toString() + "&SCOPE=" + _scope + "&SERVICE=WFS&VERSION=1.0.0&REQUEST=GetFeature&SRS=EPSG%3A900913&LAYERS=&WHEELCHAIRONLY=false&_OLSALT=0.6481046043336391&BBOX=3272267.2330292,8264094.7670049,3479564.4537096,8483621.912209");
					URLConnection conn = url.openConnection();
					conn.setRequestProperty("Cookie", _cookie);
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
					_array = new ArrayList<Transport>();
					for (int i = 0; i < ja.length(); i++) {
						JSONArray coordinates = ja.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates");
						Transport transport = route.creatTransport();
						transport.Lat = m.deg(coordinates.getDouble(1), AxisType.LAT);
						transport.Lng = m.deg(coordinates.getDouble(0), AxisType.LNG);
						transport.direction = (float)ja.getJSONObject(i).getJSONObject("properties").getDouble("direction");
						transport.velocity = (int)ja.getJSONObject(i).getJSONObject("properties").getDouble("velocity");
						transport.id = ja.getJSONObject(i).getInt("id");
						_array.add(transport);
						if (_canceled) {
							throw new LoadTaskException();
						}
					}
				} catch (LoadTaskException e) {
					_array = null;
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
				if (isOnline()) {
					_listener.onTransportListOfRouteLoadComplete(_array);
				}
				removeParserById(requestId);
			}

			@Override
			public int getRequestId() {
				return requestId;
			}
		};
		
		startParserWithId(req, requestId);
	}
	
	public void startParserWithId(IRequest req, int id) {
		boolean exist = false;
		for (ParserWebPageTask parser : _getParsers()) {
			if (parser.getRequestId() == id) {
				exist = true;
				break;
			}
		}
		if (!exist) {
			ParserWebPageTask parser = new ParserWebPageTask(req);
			parser.execute((Void)null);
			_getParsers().add(parser);
		}
	}
	
	public void loadImg(LatLngBounds bounds, final int width, final int height) {
		Mercator m = new Mercator();
		final double left_lon = m.mer(bounds.southwest.longitude, AxisType.LNG);
		final double left_lat = m.mer(bounds.southwest.latitude, AxisType.LAT);
		final double right_lon = m.mer(bounds.northeast.longitude, AxisType.LNG);
		final double right_lat = m.mer(bounds.northeast.latitude, AxisType.LAT);
		
		final int requestId = 2;
		
		IRequest req = new IRequest() {
			
			int _step = 0;
			Bitmap _img = null;
			
			@Override
			public void setCanceled() {
			}
			
			@Override
			public void nextExecute() {
				try {
					String filters = "";
					if (!isEnabledFilter(TransportKind.Bus) && !isEnabledFilter(TransportKind.Trolley) && !isEnabledFilter(TransportKind.Tram) && !isEnabledFilter(TransportKind.Ship)) {
						filters = "vehicle_bus%2Cvehicle_ship%2Cvehicle_tram%2Cvehicle_trolley";
					} else {
						boolean more = false;
						if (isEnabledFilter(TransportKind.Bus)) {
							filters = "vehicle_bus";
							more = true;
						}
						if (isEnabledFilter(TransportKind.Trolley)) {
							if (more) {
								filters += "%2C";
							}
							more = true;
							filters += "vehicle_trolley"; 
						}
						if (isEnabledFilter(TransportKind.Tram)) {
							if (more) {
								filters += "%2C";
							}
							more = true;
							filters += "vehicle_tram"; 
						}
						if (isEnabledFilter(TransportKind.Ship)) {
							if (more) {
								filters += "%2C";
							}
							more = true;
							filters += "vehicle_ship"; 
						}
					}
					
					String src = "http://transport.orgp.spb.ru/cgi-bin/mapserv?TRANSPARENT=TRUE&FORMAT=image%2Fpng&MAP=vehicle_typed.map&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&LAYERS=" + filters + "&WHEELCHAIRONLY=false&SRS=EPSG%3A900913&BBOX=" + Double.toString(left_lon) + "," + Double.toString(left_lat) + "," + Double.toString(right_lon) + "," + Double.toString(right_lat) + "&WIDTH=" + Integer.toString(width) + "&HEIGHT=" + Integer.toString(height);
		            URL url = new URL(src);
		            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		            connection.setDoInput(true);
		            connection.connect();
		            InputStream input = connection.getInputStream();
		            _img = BitmapFactory.decodeStream(input);
		        } catch (IOException e) {
		        	_img = null;
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
				if (isOnline()) {
					_listener.onImgLoadComplete(_img);
				}
				removeParserById(requestId);
			}

			@Override
			public int getRequestId() {
				return requestId;
			}
		};
		
		startParserWithId(req, requestId);
	}
	
	public void removeParserById(int id) {
		int index = _indexParserOfId(id);
		if (index != -1) {
			_getParsers().remove(index);
		}
	}
	private int _indexParserOfId(int id) {
		int index = 0;
		boolean exist = false;
		for (ParserWebPageTask parser : _getParsers()) {
			if (parser.getRequestId() == id) {
				exist = true;
				break;
			}
			index++;
		}
		return exist ? index : -1;
	}
	
	public void cancel() {
		for (ParserWebPageTask parser : _getParsers()) {
			parser.cancel(true);
		}
	}
	
	private ArrayList<TransportOverlay> _allTransportOverlays;
	@SuppressWarnings("unchecked")
	public ArrayList<TransportOverlay> getAllTransportOverlays() {
		if (_allTransportOverlays == null) {
			ArrayList<Transport> array = (ArrayList<Transport>)Files.loadFromFile("allTransportOverlays.ser", this);
			_allTransportOverlays = new ArrayList<TransportOverlay>();
			if (array != null) {
				for (Transport transport : array) {
					TransportOverlay transportOverlay = new TransportOverlay();
					transportOverlay.transport = transport;
					_allTransportOverlays.add(transportOverlay);
				}
			}
			if (_allTransportOverlays == null) {
				_allTransportOverlays = new ArrayList<TransportOverlay>();
			}
		}
		return _allTransportOverlays;
	}
	public void removeAllTransportOverlays() {
		deleteFile("allTransportOverlays.ser");
		if (_allTransportOverlays != null) {
			for (TransportOverlay transportOverlay : _allTransportOverlays) {
				transportOverlay.groundOverlay.remove();
				transportOverlay.marker.remove();
			}
			_allTransportOverlays.clear();
		}
	}
	public void saveAllTransportOverlays() {
		if (_allTransportOverlays != null) {
			ArrayList<Transport> array = new ArrayList<Transport>();
			for (TransportOverlay transportOverlay : _allTransportOverlays) {
				array.add(transportOverlay.transport);
			}
			Files.saveToFile(array, "allTransportOverlays.ser", this);
		}
	}
	
	private GroundOverlay _simpleTransportOverlay = null;
	public void setSimpleTransportOverlay(GroundOverlay overlay) {
		_simpleTransportOverlay = overlay;
	}
	public void removeSimpleTransportOverlay() {
		if (_simpleTransportOverlay != null) {
			_simpleTransportOverlay.remove();
			_simpleTransportOverlay = null;
		}
	}
	
	private SimpleTransportView _lastSimpleTransportView;
	public void setLastSimpleTransportView(SimpleTransportView last) {
		if (!_isLowMemory) {
			_lastSimpleTransportView = last;
		}
	}
	public SimpleTransportView getLastSimpleTransportView() {
		if (_lastSimpleTransportView == null && !_isLowMemory) {
			_lastSimpleTransportView = (SimpleTransportView)Files.loadFromFile("lastSimpleTransportView.ser", true, this);
		}
		return _lastSimpleTransportView;
	}
	public void saveLastSimpleTransportView() {
		if (_lastSimpleTransportView != null) {
			Files.saveToFile(_lastSimpleTransportView, "lastSimpleTransportView.ser", this);
		}
	}
	public void removeLastSimpleTransportView() {
		_lastSimpleTransportView = null;
	}
	
	public String getCookie() throws IOException {
		if (_scope == null || _cookie == null) {
			if (_scopeCookieIsLoading) {
				return null;
			}
			_scopeCookieIsLoading = true;
			URL url = new URL("http://transport.orgp.spb.ru/Portal/transport/route/1329");
			URLConnection conn = url.openConnection();
			conn.connect();
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = "";
			
			String headerName=null;
			for (int i=1; (headerName = conn.getHeaderFieldKey(i))!=null; i++) {
			 	if (headerName.equals("Set-Cookie")) {                  
			 		_cookie = conn.getHeaderField(i).split(";")[0];
			 		break;
			 	}
			}
  
			int index =  0;
			while ((line = rd.readLine()) != null) {
				if ((index = line.indexOf("scope")) != -1) {
					break;
				}
			}
			
			index += 8;
			int end = index;
			while (line.charAt(end) != '"') {
				end++;
			}
			_scope = URLEncoder.encode(line.substring(index, end));
			_scopeCookieIsLoading = false;
		}
		
		return _cookie;
	}
	
	private boolean _leftMenuIsOpened = false;
	private boolean _rightMenuIsOpened = false;
	public void setMenuOpened(MenuKind kind, boolean opened) {
		if (kind == MenuKind.Left) {
			_leftMenuIsOpened = opened;
			if (_leftMenuIsOpened) {
				_setMenuIsOpenedOnce();
			}
		} else {
			_rightMenuIsOpened = opened;
		}
		
	}
	public boolean menuIsOpened(MenuKind kind) {
		if (kind == MenuKind.Left) {
			return _leftMenuIsOpened;
		}
		return _rightMenuIsOpened;
	}
	
	public boolean menuIsOpenedOnce() {
		boolean isOpenedOnce = (Boolean)getData("menu_is_opened_once", false);
		return isOpenedOnce;
	}
	private void _setMenuIsOpenedOnce() {
		setData("menu_is_opened_once", true, true);
	}
	
	private boolean _isOnline = false;
	private boolean _isFirstPerformIsOnlineMethod = true; 
	public boolean isOnline() {
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    boolean online = netInfo != null && netInfo.isConnectedOrConnecting();
	    
	    if (_isOnline != online) {
	    	_isOnline = online;
	    	if (_listener != null) {
	    		if (online) {
					_listener.onInternetAccessSuccess();
				} else {
					_listener.onInternetAccessDeny();
				}
			}
		} else if (_isOnline == false && _isFirstPerformIsOnlineMethod) {
			_isFirstPerformIsOnlineMethod = false;
			_listener.onInternetAccessDeny();
		}
	    
	    return online;
	}

	public boolean fbIsLoggedInPressed() {
		return (Boolean)getData("fbIsLoggedInPressed", false);
	}
	public void setfbLoggedInPressed(boolean l) {
		setData("fbIsLoggedInPressed", l);
	}
	
	private ShareModel _shareModel;
	public ShareModel getShareModel() {
		return _shareModel;
	}
	public void setShareModel(ShareModel shareModel) {
		_shareModel = shareModel;
	}
	
	public int dpToPx(double dp) {
		return (int)(getResources().getDisplayMetrics().density * dp);
	}
    
    public int pxToDp(int px) {
		return (int)(px / getResources().getDisplayMetrics().density);
	}
    
    private boolean _isFirstCameraChange = true;
    public boolean isFirstCameraChange() {
    	if (_isFirstCameraChange) {
			_isFirstCameraChange = false;
			return true;
		}
		return false;
	}
}