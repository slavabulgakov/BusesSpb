package ru.slavabulgakov.busesspb.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import ru.slavabulgakov.busesspb.paths.Forecast;

/**
 * Created by user on 24.12.13.
 */
public class ForecastsContainer extends LoaderContainer {

    private RightMenuModel _rightMenuModel;

    public ForecastsContainer(String stationId, RightMenuModel rightMenuModel) {
        super("http://transport.orgp.spb.ru/Portal/transport/internalapi/forecast/bystop?stopID=" + stationId, null, null);
        _rightMenuModel = rightMenuModel;
        _isJson = true;
    }

    @Override
    public void handler(Object obj) {
        super.handler(obj);

        JSONObject response = (JSONObject)obj;
        SimpleDateFormat format = new SimpleDateFormat("yyyyy-mm-dd hh:mm:ss", Locale.US);
        ArrayList<Object> forecasts = new ArrayList<Object>();
        try {
            JSONArray array = response.getJSONArray("result");
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                Forecast forecast = new Forecast();
                forecast.time = format.parse(item.getString("arrivingTime"));
                RoutesNamesLoaderContainer.RouteName routeName = _rightMenuModel.getRouteName(item.getInt("routeId"));
                forecast.transportNumber = routeName.number;
                forecast.transportKind = routeName.kind;
                forecasts.add(forecast);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        _data = forecasts;
    }
}
