package ru.slavabulgakov.busesspb.Network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import ru.slavabulgakov.busesspb.model.Model;
import ru.slavabulgakov.busesspb.model.Route;
import ru.slavabulgakov.busesspb.paths.Forecast;

/**
 * Created by user on 24.12.13.
 */
public class ForecastsContainer extends LoaderContainer {

    private Model _model;
    private String _stationId;

    public ForecastsContainer(String stationId, Model model) {
        super("http://transport.orgp.spb.ru/Portal/transport/internalapi/forecast/bystop?stopID=" + stationId, null, null);
        _stationId = stationId;
        _model = model;
        _isJson = true;
    }

    public String getStationId() {
        return _stationId;
    }

    @Override
    public void handler(Object obj) {
        super.handler(obj);

        JSONObject response = (JSONObject)obj;
        SimpleDateFormat format = new SimpleDateFormat("yyyyy-mm-dd HH:mm:ss", Locale.US);
        ArrayList<Object> forecasts = new ArrayList<Object>();
        try {
            JSONArray array = response.getJSONArray("result");
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                Forecast forecast = new Forecast();
                forecast.time = format.parse(item.getString("arrivingTime"));
                Route route = _model.getRoute(item.getInt("routeId"));
                forecast.transportNumber = route.routeNumber;
                forecast.transportKind = route.kind;
                forecast.fullName = route.fullName;
                forecasts.add(forecast);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        _data = forecasts;
    }

    @Override
    public boolean isEqual(LoaderContainer loaderContainer) {
        return ((ForecastsContainer)loaderContainer).getStationId() == _stationId;
    }
}
