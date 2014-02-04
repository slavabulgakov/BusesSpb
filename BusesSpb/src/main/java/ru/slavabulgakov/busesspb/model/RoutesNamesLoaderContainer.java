package ru.slavabulgakov.busesspb.model;

import java.io.Serializable;
import java.util.ArrayList;


public class RoutesNamesLoaderContainer extends LoaderContainer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RoutesNamesLoaderContainer() {
		super("http://futbix.ru/busesspb/v1_0/routesdata/", "routesNames.txt", "routesNames.ser");
	}

	public class RouteName implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public String number;
		public int id;
		public TransportKind kind;
        public String fullName;
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
			RouteName routeName = new RouteName();
			routeName.id = Integer.parseInt(items[0]);
			routeName.number = items[2];
            routeName.fullName = items[3];
			String kind = items[items.length - 4];
			if (kind.equals("bus")) {
				 routeName.kind = TransportKind.Bus;
			} else if (kind.equals("tram")) {
				routeName.kind = TransportKind.Tram;
			} else if (kind.equals("trolley")) {
				routeName.kind = TransportKind.Trolley;
			} else if (kind.equals("ship")) {
				routeName.kind = TransportKind.Ship;
			} else {
				routeName.kind = TransportKind.None;
			}
			
			data.add(routeName);
		}
		_data = data;
	}
}
