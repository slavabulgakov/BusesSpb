package ru.slavabulgakov.busesspb.Network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ru.slavabulgakov.busesspb.model.Model;
import ru.slavabulgakov.busesspb.model.Route;
import ru.slavabulgakov.busesspb.model.Transport;

/**
 * Created by Slava Bulgakov on 19.03.14.
 */
public class TransportsContainer extends LoaderContainer {

    private Model _model;
    private String _ids;

    public TransportsContainer(String ids, Model model) {
        super("http://transport.orgp.spb.ru/Portal/transport/internalapi/vehicles/positions/?transports=bus,tram,trolley,ship&routeIDs=" + ids + "&bbox=29.857189,59.633459,30.983354,60.198390", null, null);
        _model = model;
        _isJson = true;
        _ids = ids;
    }

    public String getIds() {
        return _ids;
    }

    @Override
    public void handler(Object obj) {
        super.handler(obj);

        JSONObject response = (JSONObject)obj;
        ArrayList<Transport>transports = new ArrayList<Transport>();
        try {
            JSONArray array = response.getJSONArray("result");
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                Transport transport = _transportOfRouteId(object.getInt("routeId"));
                if (transport == null) {
                    continue;
                }
                transport.direction = (float) object.getDouble("direction");
                JSONObject position = object.getJSONObject("position");
                transport.Lat = position.getDouble("lat");
                transport.Lng = position.getDouble("lon");
                transports.add(transport);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        _data = transports;
    }

    private Transport _transportOfRouteId(Integer routeId) {
        for (Route route : _model.getFavorite()) {
            if (route.id.equals(routeId)) {
                return route.createTransport();
            }
        }
        return null;
    }

    @Override
    public boolean isEqual(LoaderContainer loaderContainer) {
        return ((TransportsContainer)loaderContainer).getIds().equals(_ids);
    }
}
