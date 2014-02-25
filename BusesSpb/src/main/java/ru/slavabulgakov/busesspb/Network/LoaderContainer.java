package ru.slavabulgakov.busesspb.Network;

import java.io.Serializable;

public class LoaderContainer implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String _urlString;
	private String _staticFileName;
	private String _cacheFileName;
    private String _versionUrlString;
    private String _versionKeyString;
    protected boolean _isJson;
	protected Object _data;
	
	public LoaderContainer(String urlString, String staticFileName, String cacheFileName) {
		_urlString = urlString;
		_staticFileName = staticFileName;
		_cacheFileName = cacheFileName;
        _versionUrlString = null;
	}

    public LoaderContainer(String urlString, String staticFileName, String cacheFileName, String versionUrlString, String versionKeyString) {
        _urlString = urlString;
        _staticFileName = staticFileName;
        _cacheFileName = cacheFileName;
        _versionUrlString = versionUrlString;
        _versionKeyString = versionKeyString;
    }
	
	public Object getData() {
		return _data;
	}
	
	public String getUrlString() {
		return _urlString;
	}
	
	public String getStaticFileName() {
		return _staticFileName;
	}

    public String getVersionUrlString() {
        return _versionUrlString;
    }

    public String getVersionKeyString() {
        return _versionKeyString;
    }
	
	public String getCacheFileName() {
		return _cacheFileName;
	}
	
	public void handler(Object obj) {
	}
	
	public void loadData(Object data) {
		_data = data;
	}

    public boolean isJson() {
        return _isJson;
    }

    public boolean isEqual(LoaderContainer loaderContainer) {
        return true;
    }
}