package ru.slavabulgakov.busesspb.paths;

import java.io.Serializable;
import java.util.ArrayList;

import ru.slavabulgakov.busesspb.model.TransportKind;

public class Path implements Serializable {
    public Path(int routeId, int direction, TransportKind kind) {
        _routeId = routeId;
        _direction = direction;
        _kind = kind;
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

    private TransportKind _kind;
    public TransportKind getKind() {
        return _kind;
    }

    private ArrayList<Point> _points;
    public ArrayList<Point> getPoints() {
        if (_points == null) {
            _points = new ArrayList<Point>();
        }
        return _points;
    }
}