package ru.slavabulgakov.busesspb.controller;

import android.location.Location;

import com.google.android.gms.location.LocationClient;

import ru.slavabulgakov.busesspb.controls.RightMenu;
import ru.slavabulgakov.busesspb.model.Loader;
import ru.slavabulgakov.busesspb.model.RightMenuModel;
import ru.slavabulgakov.busesspb.model.StationsContainer;
import ru.slavabulgakov.busesspb.paths.Station;
import ru.slavabulgakov.busesspb.paths.Stations;

/**
 * Created by user on 30.12.13.
 */
public class RightMenuStationsState extends State {
    private Location _location;

    public RightMenuStationsState(Location location) {
        _location = location;
    }

    @Override
    public void start() {
        super.start();
        _loadStations();
    }

    @Override
    public void resume() {
        super.resume();
        _loadStations();
    }

    private void _loadStations() {
        Loader loader = _menuModel().getLoader(StationsContainer.class);
        if (loader == null) {
            _menuModel().loadForContainer(new StationsContainer(), _controller);
        } else {
            if (loader.getState().getValue() > Loader.State.staticLoading.getValue()) {
                _findNearblyStations();
            }
        }
    }

    private RightMenu _menu() {
        if (_controller.getMainActivity() != null) {
            return _controller.getMainActivity().getRightMenu();
        }
        return null;
    }

    private RightMenuModel _menuModel() {
        return _controller.getModel().getRightMenuModel();
    }

    private Double _distationOfStation(Station station) {
        if (_location == null) {
            return null;
        }
        double loc_lat = _location.getLatitude();
        double loc_lng = _location.getLongitude();
        double station_lat = station.point.getLatlng().latitude;
        double station_lng = station.point.getLatlng().longitude;
        Double dist = Math.abs(loc_lat - station_lat) + Math.abs(loc_lng - station_lng);
        return dist;
    }

    private void _findNearblyStations() {
        Loader loader = _menuModel().getLoader(StationsContainer.class);
        Stations nearblyStations = new Stations();
        for (Object obj: loader.getContainer().getData()) {
            Station station = (Station)obj;
            Double dist = _distationOfStation(station);
            if (dist != null) {
                int index = 0;
                boolean setted = false;
                for (Station nearblyStation : nearblyStations) {
                    if (dist < _distationOfStation(nearblyStation)) {
                        nearblyStations.add(index, station);
                        setted = true;
                        break;
                    }
                    index++;
                }
                if (!setted) {
                    nearblyStations.add(station);
                }
            }
            while (nearblyStations.size() > 10) {
                nearblyStations.remove(nearblyStations.size() - 1);
            }
        }
        if (_menu() != null) {
            _menu().loadNearblyStations(nearblyStations);
        }
    }

    public void staticLoaded(Loader loader) {
        if (loader.getContainer().getClass() == StationsContainer.class) {
            _findNearblyStations();
        }
    }
}
