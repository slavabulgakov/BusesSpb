package ru.slavabulgakov.busesspb.Network;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import ru.slavabulgakov.busesspb.model.Model;
import ru.slavabulgakov.busesspb.paths.Path;
import ru.slavabulgakov.busesspb.paths.Point;
import ru.slavabulgakov.busesspb.paths.Station;

/**
 * Created by Slava Bulgakov on 10.02.14.
 */
public class StationsForRouteContainer extends LoaderContainer {

    private int _routeId;
    private int _direction;
    private Model _model;
    public StationsForRouteContainer(int routeId, int direction, Model model) {
        super("http://transport.orgp.spb.ru/Portal/transport/internalapi/routes/stops?routeIDs=" + routeId + "&directions=" + String.valueOf(direction), null, "v1_stationsForRouteId" + routeId + "andDirection" + String.valueOf(direction));
        _model = model;
        _routeId = routeId;
        _direction = direction;
        _isJson = true;
    }

    public int getRouteId() {
        return _routeId;
    }

    public int getDirection() {
        return _direction;
    }

    @Override
    public void handler(Object obj) {
        super.handler(obj);
        JSONObject response = (JSONObject)obj;
        ArrayList<Object> stops = new ArrayList<Object>();
        Path path = null;
        try {
            JSONArray stopIDs = response.getJSONArray("result").getJSONObject(0).getJSONArray("stopIDs");
            int length = stopIDs.length();
            for (int i = 0; i < length; i++) {
                String stopID = stopIDs.getString(i);
                Station station = _getStationById(stopID);
                stops.add(station);
            }

            path = new Path(_routeId, _direction, ((Station)stops.get(0)).kind);
            JSONArray pathJSON = response.getJSONArray("result").getJSONObject(0).getJSONArray("path");
            length = pathJSON.length();
            for (int i = 0; i < length; i++) {
                JSONObject point = pathJSON.getJSONObject(i);
                double lon = point.getDouble("lon");
                double lat = point.getDouble("lat");
                Point p = new Point(new LatLng(lat, lon));
                path.getPoints().add(p);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        HashMap<String, Object>data = new HashMap<String, Object>();
        data.put("stops", stops);
        data.put("path", path);
        _data = data;
    }

    private Station _getStationById(String stationId) {
        StationsContainer stationsContainer = (StationsContainer)_model.getNetwork().getLoader(StationsContainer.class).getContainer();
        ArrayList<Station>stations = (ArrayList<Station>)stationsContainer.getData();
        for(Station station : stations) {
            if (station.id.equals(stationId)) {
                return station;
            }
        }
        return null;
    }

    @Override
    public boolean isEqual(LoaderContainer loaderContainer) {
        StationsForRouteContainer container = (StationsForRouteContainer)loaderContainer;
        return _routeId == container.getRouteId() && _direction == container.getDirection();
    }
}
