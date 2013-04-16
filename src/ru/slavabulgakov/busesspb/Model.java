package ru.slavabulgakov.busesspb;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import ru.slavabulgakov.busesspb.Mercator.AxisType;
import ru.slavabulgakov.busesspb.ParserWebPageTask.IRequest;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

enum TransportKind {
	None,
	Bus,
	Trolley,
	Tram,
}

class Transport {
	Integer id;
	Integer routeId;
	Integer cost;
	String routeNumber;
	Double Lng;
	Double Lat;
	float direction;
	int velocity;
	TransportKind kind;
}

 class Route implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Integer id;
	Integer cost;
	String routeNumber;
	TransportKind kind;
	Transport creatTransport() {
		Transport transport = new Transport();
		transport.routeNumber = this.routeNumber;
		transport.cost = this.cost;
		transport.kind = this.kind;
		transport.routeId = this.id;
		return transport;
	}
}

public class Model extends Application {
	
	private static final int BUS_FILTER = 1;
	private static final int TROLLEY_FILTER = 2;
	private static final int TRAM_FILTER = 4;
	
	private static final String STORAGE_NAME = "busesspb";
	
	public int enumKindToInt(TransportKind kind) {
		switch (kind) {
		case Bus:
			return Model.BUS_FILTER;
			
		case Trolley:
			return Model.TROLLEY_FILTER;
			
		case Tram:
			return Model.TRAM_FILTER;

		default:
			break;
		}
		
		return 0;
	}
	
	private void _setFilterToStorage(TransportKind kind, String name) {
		SharedPreferences settings = getSharedPreferences(STORAGE_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		int filter = settings.getInt(name, 0);
		int f = enumKindToInt(kind);
		filter ^= f;
		editor.putInt(name, filter);
		editor.commit();
	}
	
	private boolean _isEnabledFilterFromStorage(TransportKind kind, String name) {
		SharedPreferences settings = getSharedPreferences(STORAGE_NAME, 0);
		int filter = settings.getInt(name, 0);
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
	
	
	
	private ArrayList<ParserWebPageTask> _parsers;
	private ArrayList<Route> _favoriteRoutes;
	private ArrayList<Route> _allRoutes;
	private OnLoadCompleteListener _listener;
	private Date _lastNetErrorDate;
	
	public interface OnLoadCompleteListener {
		void onTransportListOfRouteLoadComplete(ArrayList<Transport> array);
		void onRouteKindsLoadComplete(ArrayList<Route> array);
		void onImgLoadComplete(Bitmap img);
		void onInternetAccessDeny();
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<Route> _loadFromFile(String fileName) {
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(openFileInput(fileName));
			try {
				return (ArrayList<Route>)in.readObject();
			} catch (Exception e) {
				// TODO: handle exception
			}
		} catch (Exception e) {
			if (!(e instanceof FileNotFoundException)) {
				
			}
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e2) {
					// TODO: handle exception
				}
			}
		}
		return new ArrayList<Route>();
	}
	
	private void _saveToFile(ArrayList<Route> transportList, String fileName) {
		ObjectOutputStream out = null;
		try {
			FileOutputStream fout = openFileOutput(fileName, 0);
			out = new ObjectOutputStream(fout);
			out.writeObject(transportList);
			fout.getFD().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}
		}
	}
	
	public ArrayList<Route> getFavorite() {
		if (_favoriteRoutes == null) {
			_favoriteRoutes = _loadFromFile("favoriteTransportList.ser");
		}
		return _favoriteRoutes;
	}
	
	public void saveFavorite() {
		_saveToFile(_favoriteRoutes, "favoriteTransportList.ser");
	}
	
	public ArrayList<Route> getAllRoutes() {
		if (_allRoutes == null) {
			_allRoutes = new ArrayList<Route>();
		}
		return _allRoutes;
	}
	
	public void setAll(ArrayList<Route> all) {
		_allRoutes = all;
	}
	
	public void changeListener(OnLoadCompleteListener listener) {
		_listener = listener;
	}
	
	public void loadDataForAllRoutes(OnLoadCompleteListener listener) {
		_listener = listener;
		ArrayList<Route> routeList = getFavorite();
		if (routeList.size() == 0) {
			routeList = getAllRoutes();
		}
		final int requestId = 3;
		IRequest req = new IRequest() {
			
			int _step = 0;
			ArrayList<Route> _array;
			
			@Override
			public void setCanceled() {
			}
			
			@Override
			public void nextExecute() {
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost("http://transport.orgp.spb.ru/Portal/transport/routes/list");
				
				try {
					List<NameValuePair> nameValuesPairs = new ArrayList<NameValuePair>(24);
					nameValuesPairs.add(new BasicNameValuePair("sEcho", "10"));
					nameValuesPairs.add(new BasicNameValuePair("iColumns", "11"));
					nameValuesPairs.add(new BasicNameValuePair("sColumns", "id,transportType,routeNumber,name,urban,poiStart,poiFinish,cost,forDisabled,scheduleLinkColumn,mapLinkColumn"));
					nameValuesPairs.add(new BasicNameValuePair("iDisplayStart", "0"));
					nameValuesPairs.add(new BasicNameValuePair("iDisplayLength", "1000"));
					nameValuesPairs.add(new BasicNameValuePair("sNames", "id,transportType,routeNumber,name,urban,poiStart,poiFinish,cost,forDisabled,scheduleLinkColumn,mapLinkColumn"));
					nameValuesPairs.add(new BasicNameValuePair("iSortingCols", "1"));
					nameValuesPairs.add(new BasicNameValuePair("iSortCol_0", "2"));
					nameValuesPairs.add(new BasicNameValuePair("sSortDir_0", "asc"));
					nameValuesPairs.add(new BasicNameValuePair("bSortable_0", "true"));
					nameValuesPairs.add(new BasicNameValuePair("bSortable_1", "true"));
					nameValuesPairs.add(new BasicNameValuePair("bSortable_2", "true"));
					nameValuesPairs.add(new BasicNameValuePair("bSortable_3", "true"));
					nameValuesPairs.add(new BasicNameValuePair("bSortable_4", "true"));
					nameValuesPairs.add(new BasicNameValuePair("bSortable_5", "true"));
					nameValuesPairs.add(new BasicNameValuePair("bSortable_6", "true"));
					nameValuesPairs.add(new BasicNameValuePair("bSortable_7", "true"));
					nameValuesPairs.add(new BasicNameValuePair("bSortable_8", "true"));
					nameValuesPairs.add(new BasicNameValuePair("bSortable_9", "false"));
					nameValuesPairs.add(new BasicNameValuePair("bSortable_10", "false"));
					nameValuesPairs.add(new BasicNameValuePair("transport-type", "1"));
					nameValuesPairs.add(new BasicNameValuePair("transport-type", "46"));
					nameValuesPairs.add(new BasicNameValuePair("transport-type", "2"));
					nameValuesPairs.add(new BasicNameValuePair("transport-type", "0"));
					httpPost.setEntity(new UrlEncodedFormEntity(nameValuesPairs));
					
					ResponseHandler<String> responseHandler=new BasicResponseHandler();
			        String responseBody = httpClient.execute(httpPost, responseHandler);
			        JSONObject response = new JSONObject(responseBody);
			        
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
						}
						
						_array.add(route);
					}
			        
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
					setAll(_array);
					_listener.onRouteKindsLoadComplete(_array);
				}
				_removeParserById(requestId);
			}

			@Override
			public int getRequestId() {
				return requestId;
			}
		};
		
		_startParserWithId(req, requestId);
	}
	
	private ArrayList<ParserWebPageTask> _getParsers() {
		if (_parsers == null) {
			_parsers = new ArrayList<ParserWebPageTask>();
		}
		return _parsers;
	}
	
	public void showFavoriteRoutes(OnLoadCompleteListener listener) {
		for (Route route : getFavorite()) {
			_loadDataForRoute(route, listener);
		}
	}
	
	private String _cookie = null;
	private String _scope = null;
	private boolean _scopeCookieIsLoading = false;
	private void _loadDataForRoute(final Route route, final OnLoadCompleteListener listener) {
		_listener = listener;
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
					listener.onTransportListOfRouteLoadComplete(_array);
				}
				_removeParserById(requestId);
			}

			@Override
			public int getRequestId() {
				return requestId;
			}
		};
		
		_startParserWithId(req, requestId);
	}
	
	private void _startParserWithId(IRequest req, int id) {
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
	
	public void loadImg(LatLngBounds bounds, final int width, final int height, final OnLoadCompleteListener listener) {
		_listener = listener;
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
					if (!isEnabledFilter(TransportKind.Bus) && !isEnabledFilter(TransportKind.Trolley) && !isEnabledFilter(TransportKind.Tram)) {
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
						filters += "%2Cvehicle_ship";
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
					listener.onImgLoadComplete(_img);
				}
				_removeParserById(requestId);
			}

			@Override
			public int getRequestId() {
				return requestId;
			}
		};
		
		_startParserWithId(req, requestId);
	}
	
	private void _removeParserById(int id) {
		int i = 0;
		boolean exist = false;
		for (ParserWebPageTask parser : _getParsers()) {
			if (parser.getRequestId() == id) {
				exist = true;
				break;
			}
			i++;
		}
		if (exist) {
			_getParsers().remove(i);
		}
	}
	
	public void cancel() {
		for (ParserWebPageTask parser : _getParsers()) {
			parser.cancel(true);
		}
	}
	
	static class TransportOverlay {
		Transport transport;
		GroundOverlay groundOverlay;
		Marker marker;
	}
	
	private ArrayList<TransportOverlay> _allTransportOverlay;
	
	public ArrayList<TransportOverlay> getAllTransportOverlay() {
		if (_allTransportOverlay == null) {
			_allTransportOverlay = new ArrayList<Model.TransportOverlay>();
		}
		return _allTransportOverlay;
	}
	
	private boolean _menuIsOpened = false;
	public void setMenuOpened(boolean opened) {
		_menuIsOpened = opened;
	}
	public boolean menuIsOpened() {
		return _menuIsOpened;
	}
	
	public boolean isOnline() {
		boolean online = true;
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    online = netInfo != null && netInfo.isConnectedOrConnecting();
	    
	    if (!online) {
	    	Date now = new Date();
			if (_lastNetErrorDate != null) {
				if (now.getTime() - _lastNetErrorDate.getTime() > 2000) {
					_lastNetErrorDate = now;
					_listener.onInternetAccessDeny();
				}
			} else {
				_lastNetErrorDate = now;
				_listener.onInternetAccessDeny();
			}
		}
	    
	    return online;
	}

	private boolean _fbIsLoggedInPressed;
	public boolean fbIsLoggedInPressed() {
		return _fbIsLoggedInPressed;
	}
	public void setfbLoggedInPressed(boolean l) {
		_fbIsLoggedInPressed = l;
	}
	
	private ShareModel _shareModel;
	public ShareModel getShareModel() {
		return _shareModel;
	}
	public void setShareModel(ShareModel shareModel) {
		_shareModel = shareModel;
	}
}