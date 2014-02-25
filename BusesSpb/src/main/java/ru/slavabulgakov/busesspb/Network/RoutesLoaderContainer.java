package ru.slavabulgakov.busesspb.Network;

import android.util.Log;

import java.util.ArrayList;

import ru.slavabulgakov.busesspb.model.Route;
import ru.slavabulgakov.busesspb.model.TransportKind;


public class RoutesLoaderContainer extends LoaderContainer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RoutesLoaderContainer() {
        super("http://futbix.ru/busesspb/v1_0/routesdata/", "routesNames.txt", "v1_routesNames.ser", "http://futbix.ru/busesspb/v1_0/feed/version/", "RoutesNamesLoaderContainerKey");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handler(Object obj) {
		super.handler(obj);
		
		ArrayList<String> strings = null;
		if (obj.getClass() == String.class) {
			String[] s = ((String)obj).split("\n");
			strings = new ArrayList<String>();
			for (int i = 0; i < s.length; i++) {
				strings.add(s[i]);
			}
		} else {
			strings = (ArrayList<String>)obj;
		}
		
		ArrayList<Object> data = new ArrayList<Object>();
		strings.remove(0);
		for (String line : strings) {
			String items[] = line.split(",");
            Route route = new Route();
            route.id = Integer.parseInt(items[0]);
            route.routeNumber = items[2];
            if (route.routeNumber.equals("0")) {
                Log.d("slava", "exist: " + line);
            }
            route.fullName = items[3];
			String kind = items[items.length - 4];
			if (kind.equals("bus")) {
				 route.kind = TransportKind.Bus;
			} else if (kind.equals("tram")) {
                route.kind = TransportKind.Tram;
			} else if (kind.equals("trolley")) {
                route.kind = TransportKind.Trolley;
			} else if (kind.equals("ship")) {
                route.kind = TransportKind.Ship;
			} else {
                route.kind = TransportKind.None;
			}
			
			data.add(route);
		}
		_data = data;
	}
}
