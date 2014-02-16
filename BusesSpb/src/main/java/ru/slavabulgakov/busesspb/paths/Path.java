package ru.slavabulgakov.busesspb.paths;

import java.io.Serializable;
import java.util.ArrayList;

public class Path implements Serializable {
    public Path(int routeId, int direction) {
        _routeId = routeId;
        _direction = direction;
    }

	private static final long serialVersionUID = 1L;

    private int _direction;
    public int getDirection() {
        return _direction;
    }

    private int _routeId;
    public int getRouteId() {
        return _routeId;
    }

    private ArrayList<Point> _points;
    public ArrayList<Point> getPoints() {
        if (_points == null) {
            _points = new ArrayList<Point>();
        }
        return _points;
    }
}