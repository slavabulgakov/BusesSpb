package ru.slavabulgakov.busesspb.model;

import java.io.Serializable;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class SimpleTransportView implements Serializable {
 	/**
 * 
 */
private static final long serialVersionUID = 1L;
	public Bitmap image;
	public double northeastLat;
	public double northeastLng;
	public double southwestLat;
	public double southwestLng;
	
	public LatLngBounds getBounds() {
		return new LatLngBounds(new LatLng(southwestLat, southwestLng), new LatLng(northeastLat, northeastLng));
	}
	
	public SimpleTransportView(Bitmap image_, LatLngBounds bounds_) {
		image = image_;
		northeastLat = bounds_.northeast.latitude;
		northeastLng = bounds_.northeast.longitude;
		southwestLat = bounds_.southwest.latitude;
		southwestLng = bounds_.southwest.longitude;
	}
	
	public SimpleTransportView() {}
}