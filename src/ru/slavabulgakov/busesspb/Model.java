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

import ru.slavabulgakov.busesspb.Mercator.AxisType;
import ru.slavabulgakov.busesspb.ParserWebPageTask.IRequest;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Model extends Application {
	
	private ArrayList<ParserWebPageTask> _parsers;
	private ArrayList<Transport> _favorite;
	private ArrayList<Transport> _all;
	private OnLoadCompleteListener _listener;
	private ArrayList<Marker> _markers;
	
	public interface OnLoadCompleteListener {
		void onLoadComplete(ArrayList<Transport> array);
		void onAllRoutesLoadComplete(ArrayList<Transport> array);
		void onImgLoadComplete(Bitmap img);
	}
	
	class Transport {
		Integer id;
		Integer cost;
		String routeNumber;
		Double Lng;
		Double Lat;
		float direction;
		
		Transport copy() {
			Transport transport = new Transport();
			transport.cost = this.cost;
			transport.id = this.id;
			transport.Lat = this.Lat;
			transport.Lng = this.Lng;
			transport.routeNumber = this.routeNumber;
			transport.direction = this.direction;
			return transport;
		}
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<Transport> _loadFromFile(String fileName) {
		ArrayList<Transport> transportList = null;
		String ser = SerializeObject.ReadSettings(this, "myobject.dat");
		if (ser != null && !ser.equalsIgnoreCase("")) {
		    Object obj = SerializeObject.stringToObject(ser);
		    // Then cast it to your object and 
		    if (obj instanceof ArrayList) {
		        // Do something
		    	transportList = (ArrayList<Transport>)obj;
		    }
		}
		return transportList;
	}
	
	private void _saveToFile(ArrayList<Transport> transportList, String fileName) {
		String ser = SerializeObject.objectToString(transportList);
		if (ser != null && !ser.equalsIgnoreCase("")) {
		    SerializeObject.WriteSettings(this, ser, "myobject.dat");
		} else {
		    SerializeObject.WriteSettings(this, "", "myobject.dat");
		}
	}
	
	public ArrayList<Transport> getFavorite() {
		if (_favorite == null) {
			_favorite = _loadFromFile("favoriteTransportList.ser");
			_favorite = new ArrayList<Model.Transport>();
		}
		return _favorite;
	}
	
	public void saveFavorite() {
		_saveToFile(_favorite, "favoriteTransportList.ser");
	}
	
	public ArrayList<Transport> getAll() {
		if (_all == null) {
			_all = new ArrayList<Model.Transport>();
		}
		return _all;
	}
	
	public void setAll(ArrayList<Transport> all) {
		_all = all;
	}
	
	public void changeListener(OnLoadCompleteListener listener) {
		_listener = listener;
	}
	
	public void loadDataForAllRoutes(OnLoadCompleteListener listener) {
		_listener = listener;
		ArrayList<Transport> transportList = getFavorite();
		if (transportList.size() == 0) {
			transportList = getAll();
		}
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
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost("http://transport.orgp.spb.ru/Portal/transport/routes/list");//http://xakki-pc/test.php
				
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
			        _array = new ArrayList<Model.Transport>();
			        for (int i = 0; i < aaData.length(); i++) {
						JSONArray data = aaData.getJSONArray(i);
						Transport transport = new Transport();
						transport.id = data.getInt(0);
						transport.routeNumber = data.getString(2);
						if (!data.getString(7).equals("null")) {
							transport.cost = data.getInt(7);
						}
						_array.add(transport);
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
				_listener.onAllRoutesLoadComplete(_array);
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
	
	public void showFavoriteRoutes(OnLoadCompleteListener listener) {
		for (Transport transport : getFavorite()) {
			_loadDataForRoute(transport, listener);
		}
	}
	
	private void _loadDataForRoute(final Transport transport, final OnLoadCompleteListener listener) {
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
					url = new URL("http://transport.orgp.spb.ru/Portal/transport/map/routeVehicle?ROUTE=" + transport.id.toString() + "&SERVICE=WFS&VERSION=1.0.0&REQUEST=GetFeature&SRS=EPSG%3A900913&LAYERS=&WHEELCHAIRONLY=false&_OLSALT=0.4202592596411705&BBOX=3272267.2330292,8264094.7670049,3479564.4537096,8483621.912209");
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
						Transport transportCopy = transport.copy();
						transportCopy.Lat = m.deg(coordinates.getDouble(1), AxisType.LAT);
						transportCopy.Lng = m.deg(coordinates.getDouble(0), AxisType.LNG);
						transportCopy.direction = (float)ja.getJSONObject(i).getJSONObject("properties").getDouble("direction");
						_array.add(transportCopy);
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
				_parsers.remove(this);
				listener.onLoadComplete(_array);
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
	
	class Marker {
		int id;
		GroundOverlay groundOverlay;
	}
	
	public void setMarker(int id, GroundOverlay groundOverlay) {
		if (_markers == null) {
			_markers = new ArrayList<Model.Marker>();
		}
		Marker findMarker = null;
		for (Marker marker : _markers) {
			if (marker.id == id) {
				findMarker = marker;
				break;
			}
		}
	}
}