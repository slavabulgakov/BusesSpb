package ru.slavabulgakov.busesspb.model;

import java.io.Serializable;

public class Transport implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Integer routeId;
	public Integer cost;
	public String routeNumber;
	public Double Lng;
	public Double Lat;
	public float direction;
	public TransportKind kind;
}