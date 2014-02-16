package ru.slavabulgakov.busesspb.paths;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by user on 06.02.14.
 */
public class StationMarker extends StationMapItem {
    public Marker marker;

    public StationMarker(Marker marker, Station station) {
        this.marker = marker;
        _station = station;
    }

    @Override
    public void remove() {
        marker.remove();
    }
}
