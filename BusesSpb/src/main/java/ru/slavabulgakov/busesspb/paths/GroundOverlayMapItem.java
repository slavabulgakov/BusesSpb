package ru.slavabulgakov.busesspb.paths;

import com.google.android.gms.maps.model.GroundOverlay;

/**
 * Created by Slava Bulgakov on 15.02.14.
 */
public class GroundOverlayMapItem extends MapItem {
    private GroundOverlay _groundOverlay;

    private String _stationId;
    public String getStationId() {
        return _stationId;
    }

    public GroundOverlayMapItem(GroundOverlay groundOverlay, String stationId) {
        this._groundOverlay = groundOverlay;
        this._stationId = stationId;
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass() == GroundOverlayMapItem.class) {
            GroundOverlayMapItem groundOverlayMapItem = (GroundOverlayMapItem)o;
            return groundOverlayMapItem.getStationId().equals(_stationId);
        } else {
            return false;
        }
    }

    @Override
    public void remove() {
        _groundOverlay.remove();
        super.remove();
    }
}
