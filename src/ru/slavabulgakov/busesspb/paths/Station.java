package ru.slavabulgakov.busesspb.paths;

import java.io.Serializable;

import ru.slavabulgakov.busesspb.model.TransportKind;

public class Station implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Point point;
	public String name;
	public String id;
	public TransportKind kind;
}
