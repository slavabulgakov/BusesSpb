package ru.slavabulgakov.busesspb.paths;

import java.io.Serializable;

import com.google.android.gms.maps.model.LatLng;

public class Point implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	double lat;
	double lng;
	
	public Point(LatLng latlng) {
		this.lat = latlng.latitude;
		this.lng = latlng.longitude;
	}
	
	public LatLng getLatlng() {
		return new LatLng(this.lat, this.lng);
	}
}
