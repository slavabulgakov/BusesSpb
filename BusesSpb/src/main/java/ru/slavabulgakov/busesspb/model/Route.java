package ru.slavabulgakov.busesspb.model;

import java.io.Serializable;

public class Route implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Integer id;
	public Integer cost;
	public String routeNumber;
	public TransportKind kind;
	Transport creatTransport() {
		Transport transport = new Transport();
		transport.routeNumber = this.routeNumber;
		transport.cost = this.cost;
		transport.kind = this.kind;
		transport.routeId = this.id;
		return transport;
	}
}