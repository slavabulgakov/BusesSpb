package ru.slavabulgakov.busesspb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Model extends Application {
	
	private ArrayList<ParserWebPageTask> _parsers;
	private ArrayList<Route> _favoriteRoutes;
	private ArrayList<Route> _allRoutes;
	private OnLoadCompleteListener _listener;
	
	public interface OnLoadCompleteListener {
		void onAllRoutesLoadComplete();
		void onTransportListOfRouteLoadComplete(ArrayList<Transport> array);
		void onRouteKindsLoadComplete(ArrayList<Route> array);
		void onImgLoadComplete(Bitmap img);
	}
	
	enum TransportKind {
		Bus,
		Trolley,
		Tram,
	}
	
	class Route {
		Integer id;
		Integer cost;
		String routeNumber;
		TransportKind kind;
		Transport creatTransport() {
			Transport transport = new Transport();
			transport.routeNumber = this.routeNumber;
			transport.cost = this.cost;
			transport.kind = this.kind;
			return transport;
		}
	}
	
	class Transport {
		Integer id;
		Integer cost;
		String routeNumber;
		Double Lng;
		Double Lat;
		float direction;
		TransportKind kind;
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<Route> _loadFromFile(String fileName) {
		ArrayList<Route> transportList = null;
		String ser = SerializeObject.ReadSettings(this, "myobject.dat");
		if (ser != null && !ser.equalsIgnoreCase("")) {
		    Object obj = SerializeObject.stringToObject(ser);
		    // Then cast it to your object and 
		    if (obj instanceof ArrayList) {
		        // Do something
		    	transportList = (ArrayList<Route>)obj;
		    }
		}
		return transportList;
	}
	
	private void _saveToFile(ArrayList<Route> transportList, String fileName) {
		String ser = SerializeObject.objectToString(transportList);
		if (ser != null && !ser.equalsIgnoreCase("")) {
		    SerializeObject.WriteSettings(this, ser, "myobject.dat");
		} else {
		    SerializeObject.WriteSettings(this, "", "myobject.dat");
		}
	}
	
	public ArrayList<Route> getFavorite() {
		if (_favoriteRoutes == null) {
			_favoriteRoutes = _loadFromFile("favoriteTransportList.ser");
			_favoriteRoutes = new ArrayList<Model.Route>();
		}
		return _favoriteRoutes;
	}
	
	public void saveFavorite() {
		_saveToFile(_favoriteRoutes, "favoriteTransportList.ser");
	}
	
	public ArrayList<Route> getAllRoutes() {
		if (_allRoutes == null) {
			_allRoutes = new ArrayList<Model.Route>();
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
		IRequest req = new IRequest() {
			
			boolean _canceled;
			int _step = 0;
			ArrayList<Route> _array;
			
			@Override
			public void setCanceled() {
				_canceled = true;
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
			        _array = new ArrayList<Model.Route>();
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
				setAll(_array);
				_listener.onRouteKindsLoadComplete(_array);
			}
		};
		
		if (_parsers == null) {
			_parsers = new ArrayList<ParserWebPageTask>();
		} else {
			_parsers.clear();
		}
		
		ParserWebPageTask parser = new ParserWebPageTask(req);
		parser.execute((Void)null);
	}
	
	private int _countLoadingFavoriteRoutes = 0;
	public void showFavoriteRoutes(OnLoadCompleteListener listener) {
		_countLoadingFavoriteRoutes =+ getFavorite().size();
		for (Route route : getFavorite()) {
			_loadDataForRoute(route, listener);
		}
	}
	
	private void _loadDataForRoute(final Route route, final OnLoadCompleteListener listener) {
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
					url = new URL("http://transport.orgp.spb.ru/Portal/transport/map/routeVehicle?ROUTE=" + route.id.toString() + "&SERVICE=WFS&VERSION=1.0.0&REQUEST=GetFeature&SRS=EPSG%3A900913&LAYERS=&WHEELCHAIRONLY=false&_OLSALT=0.4202592596411705&BBOX=3272267.2330292,8264094.7670049,3479564.4537096,8483621.912209");
					URLConnection conn = url.openConnection();
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
				_countLoadingFavoriteRoutes--;
				if (_countLoadingFavoriteRoutes == 0) {
					listener.onAllRoutesLoadComplete();
				}
				_parsers.remove(this);
				listener.onTransportListOfRouteLoadComplete(_array);
			}
		};
		ParserWebPageTask parser = new ParserWebPageTask(req);
		parser.execute((Void)null);
		_parsers.add(parser);
	}
	
	public void loadImg(LatLngBounds bounds, final int width, final int height, final OnLoadCompleteListener listener) {
		Mercator m = new Mercator();
		final double left_lon = m.mer(bounds.southwest.longitude, AxisType.LNG);
		final double left_lat = m.mer(bounds.southwest.latitude, AxisType.LAT);
		final double right_lon = m.mer(bounds.northeast.longitude, AxisType.LNG);
		final double right_lat = m.mer(bounds.northeast.latitude, AxisType.LAT);
		
		IRequest req = new IRequest() {
			
			boolean _canceled;
			int _step = 0;
			Bitmap _img;
			
			@Override
			public void setCanceled() {
				_canceled = true;
			}
			
			@Override
			public void nextExecute() {
				try {
					String src = "http://transport.orgp.spb.ru/cgi-bin/mapserv?TRANSPARENT=TRUE&FORMAT=image%2Fpng&MAP=vehicle_typed.map&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&LAYERS=vehicle_bus%2Cvehicle_ship%2Cvehicle_tram%2Cvehicle_trolley&WHEELCHAIRONLY=false&SRS=EPSG%3A900913&BBOX=" + Double.toString(left_lon) + "," + Double.toString(left_lat) + "," + Double.toString(right_lon) + "," + Double.toString(right_lat) + "&WIDTH=" + Integer.toString(width) + "&HEIGHT=" + Integer.toString(height);
		            URL url = new URL(src);
		            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		            connection.setDoInput(true);
		            connection.connect();
		            InputStream input = connection.getInputStream();
		            _img = BitmapFactory.decodeStream(input);
		            _step++;
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
			}
			
			@Override
			public boolean needExecute() {
				return _step == 0;
			}
			
			@Override
			public void finish() {
				listener.onImgLoadComplete(_img);
			}
		};
		ParserWebPageTask parser = new ParserWebPageTask(req);
		parser.execute((Void)null);
	}
	
	public void cancel() {
		for (ParserWebPageTask parser : _parsers) {
			parser.cancel(true);
		}
	}
	
	static class TransportOverlay {
		Transport transport;
		GroundOverlay groundOverlay;
		Marker marker;
	}
	
	private ArrayList<TransportOverlay> _allTransportOverlay;
	private ArrayList<TransportOverlay> _excessTransportOverlay;
	
	public ArrayList<TransportOverlay> getAllTransportOverlay() {
		if (_allTransportOverlay == null) {
			_allTransportOverlay = new ArrayList<Model.TransportOverlay>();
		}
		return _allTransportOverlay;
	}
	
	public ArrayList<TransportOverlay> getExcessTransportOverlay() {
		return _excessTransportOverlay;
	}
	
	@SuppressWarnings("unchecked")
	public void cloneExcessTransportOverlay() {
		if (_allTransportOverlay != null) {
			_excessTransportOverlay = (ArrayList<TransportOverlay>) _allTransportOverlay.clone();
		}
	}
}