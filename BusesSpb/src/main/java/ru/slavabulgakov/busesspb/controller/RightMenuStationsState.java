package ru.slavabulgakov.busesspb.controller;

import com.google.android.gms.maps.model.LatLng;

import ru.slavabulgakov.busesspb.controls.RightMenu;
import ru.slavabulgakov.busesspb.model.Loader;
import ru.slavabulgakov.busesspb.model.RightMenuModel;
import ru.slavabulgakov.busesspb.model.StationsContainer;
import ru.slavabulgakov.busesspb.paths.Station;
import ru.slavabulgakov.busesspb.paths.Stations;

/**
 * Created by Slava Bulgakov on 30.12.13.
 */
public class RightMenuStationsState extends State {
    private LatLng _location;

    public RightMenuStationsState(LatLng location) {
        _location = location;
    }

    @Override
    public void start() {
        super.start();
        _controller.getMainActivity().setRightMenuButtonLoading(true);
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
                _findNearbyStations();
            }
        }
    }

    private RightMenuModel _menuModel() {
        return _controller.getModel().getRightMenuModel();
    }

    private Double _distationOfStation(Station station) {
        if (_location == null) {
            return null;
        }
        double loc_lat = _location.latitude;
        double loc_lng = _location.longitude;
        double station_lat = station.point.getLatlng().latitude;
        double station_lng = station.point.getLatlng().longitude;
        Double dist = Math.abs(loc_lat - station_lat) + Math.abs(loc_lng - station_lng);
        return dist;
    }

    private void _findNearbyStations() {
        Loader loader = _menuModel().getLoader(StationsContainer.class);
        Stations nearbyStations = new Stations();
        for (Object obj: loader.getContainer().getData()) {
            Station station = (Station)obj;
            Double dist = _distationOfStation(station);
            if (dist != null) {
                int index = 0;
                boolean setted = false;
                for (Station nearblyStation : nearbyStations) {
                    if (dist < _distationOfStation(nearblyStation)) {
                        nearbyStations.add(index, station);
                        setted = true;
                        break;
                    }
                    index++;
                }
                if (!setted) {
                    nearbyStations.add(station);
                }
            }
            while (nearbyStations.size() > 20) {
                nearbyStations.remove(nearbyStations.size() - 1);
            }
        }
        _controller.getModel().getModelPaths().setNearblyStations(nearbyStations);
        _controller.getModel().getModelPaths().updateStations();
        _controller.switchToLastState();
        _controller.getMainActivity().setRightMenuButtonLoading(false);
    }

    public void staticLoaded(Loader loader) {
        if (loader.getContainer().getClass() == StationsContainer.class) {
            _findNearbyStations();
        }
    }
}
