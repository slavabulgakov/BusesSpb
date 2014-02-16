package ru.slavabulgakov.busesspb.paths;

import com.google.android.gms.maps.model.Polyline;

/**
 * Created by Slava Bulgakov on 14.02.14.
 */
public class PolylineMapItem extends MapItem {
    private Polyline _polyline;
    private int _routeId;
    private  int _direction;

    public PolylineMapItem(Polyline polyline, int routeId, int direction) {
        this._polyline = polyline;
        this._routeId = routeId;
        _direction = direction;
    }

    public int getRouteId() {
        return _routeId;
    }

    public int getDirection() {
        return  _direction;
    }

    @Override
    public void remove() {
        _polyline.remove();
        super.remove();
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass() == PolylineMapItem.class) {
            PolylineMapItem polylineMapItem = (PolylineMapItem)o;
            return _routeId == polylineMapItem.getRouteId() && _direction == polylineMapItem.getDirection();
        }
        return false;
    }
}
