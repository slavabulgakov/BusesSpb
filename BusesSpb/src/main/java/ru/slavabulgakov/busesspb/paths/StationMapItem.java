package ru.slavabulgakov.busesspb.paths;

/**
 * Created by Slava Bulgakov on 13.02.14.
 */
public class StationMapItem extends MapItem {
    protected Station _station;

    public Station getStation() {
        return _station;
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass() == StationMapItem.class) {
            StationMapItem station = (StationMapItem)o;
            return _station.id.equals(station.getStation().id);
        }
        return false;
    }
}
