package ru.slavabulgakov.busesspb.controller;

import android.location.Location;

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
    @Override
    public void start() {
        super.start();

        _loadStations();
    }

    private Location _location;
    private void _loadStations() {
        _location = _controller.getMainActivity().getMapController().getMap().getMyLocation();
        // TODO remove mock location
        _location.setLatitude(59.932709);
        _location.setLongitude(30.346395);
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
        return _controller.getMainActivity().getRightMenu();
    }

    private RightMenuModel _menuModel() {
        return _controller.getModel().getRightMenuModel();
    }

    private double _distationOfStation(Station station) {
        double dist = Math.abs(_location.getLatitude() - station.point.getLatlng().latitude) + Math.abs(_location.getLongitude() - station.point.getLatlng().longitude);
        return dist;
    }

    private void _findNearblyStations() {
        if (_location == null) {
            return;
        }
        Loader loader = _menuModel().getLoader(StationsContainer.class);
        Stations nearblyStations = new Stations();
        for (Object obj: loader.getContainer().getData()) {
            Station station = (Station)obj;
            double dist = _distationOfStation(station);
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
            while (nearblyStations.size() > 10) {
                nearblyStations.remove(nearblyStations.size() - 1);
            }
        }
        _menu().loadNearblyStations(nearblyStations);
    }

    public void staticLoaded(Loader loader) {
        if (loader.getContainer().getClass() == StationsContainer.class) {
            _findNearblyStations();
        }
    }
}
