package ru.slavabulgakov.busesspb.model;

import java.io.Serializable;
import java.util.ArrayList;

public class LoaderContainer implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String _urlString;
	private String _staticFileName;
	private String _cacheFileName;
    protected boolean _isJson;
	protected ArrayList<Object> _data;
	
	public LoaderContainer(String urlString, String staticFileName, String cacheFileName) {
		_urlString = urlString;
		_staticFileName = staticFileName;
		_cacheFileName = cacheFileName;
	}
	
	public ArrayList<Object> getData() {
		return _data;
	}
	
	public String getUrlString() {
		return _urlString;
	}
	
	public String getStaticFileName() {
		return _staticFileName;
	}
	
	public String getCacheFileName() {
		return _cacheFileName;
	}
	
	public void handler(Object obj) {
		
	}
	
	public void loadData(ArrayList<Object> data) {
		if (_data != null) {
			_data.clear();
		}
		_data = data;
	}

    public boolean isJson() {
        return _isJson;
    }
}