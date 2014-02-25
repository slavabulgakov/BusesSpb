package ru.slavabulgakov.busesspb.Network;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.ArrayList;

import ru.slavabulgakov.busesspb.Files;
import ru.slavabulgakov.busesspb.model.Model;

public class Loader {
	
	public enum State {
		staticLoading(0),
		netLoading(1),
		complete(2);
		
		private final int _value;
		private State(int value) {
			_value = value;
		}
		public int getValue() {
			return _value;
		}
	};
	
	public interface Listener {
		void staticLoaded(Loader loader);
		void netLoaded(Loader loader);
		void netError(Loader loader);
	}
	private ArrayList<Listener> _listeners;
	public void setListener(Listener listener) {
        if (_listeners == null) {
            _listeners = new ArrayList<Listener>();
        }
        if (!_listeners.contains(listener)) {
            _listeners.add(listener);
        }
    }
    private void _staticLoadedListeners() {
        for (Listener listener : _listeners) {
            listener.staticLoaded(Loader.this);
        }
    }
    private void _netLoadedListeners() {
        for (Listener listener : _listeners) {
            listener.netLoaded(Loader.this);
        }
    }
    private void _netErrorListeners() {
        for (Listener listener : _listeners) {
            listener.netError(Loader.this);
        }
    }

	private LoaderContainer _container;
	private Model _model;
	private State _state = State.staticLoading;
	private RequestQueue _queue;
	public Loader(LoaderContainer container, Model model, RequestQueue queue) {
		_container = container;
		_model = model;
		_queue = queue;
	}
	
	public State getState() {
		return _state;
	}
	
	public LoaderContainer getContainer() {
		return _container;
	}
	
	private void _cache() {
        if (_container.getCacheFileName() != null) {
            Files.saveToFile(_container.getData(), _container.getCacheFileName(), _model);
        }
	}

    private void _netLoad() {
        Request request = (Request)FacadeLoader.createRequest(_container.isJson(), _container.getUrlString(), new FacadeLoader.Listener() {

                    @Override
                    public void onResponse(final Object obj) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                _container.handler(obj);
                                _state = State.complete;
                                _netLoadedListeners();
                                _cache();
                                Log.d("data_loading", "did load from internet");
                            }
                        }).start();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (_container.getVersionUrlString() != null) {
                            _model.removeData(_container.getVersionKeyString());
                        }
                        _netErrorListeners();
                        Log.d("data_loading", "error loading data from internet");
                    }
                }
        );
        _queue.add(request);
    }

	public void load() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				_state = State.staticLoading;
                Object data = null;
                if (_container.getCacheFileName() != null) {
                    data = Files.loadFromFile(_container.getCacheFileName(), _model);
                }
				if (data == null) {
                    if (_container.getStaticFileName() != null) {
                        ArrayList<String> strings = Files.stringsArrayFromFile(_container.getStaticFileName(), _model);
                        _container.handler(strings);
                        _state = State.netLoading;
                        _staticLoadedListeners();
                        Log.d("data_loading", "did load from static");
                    }
				} else {
					_container.loadData(data);
                    _state = State.netLoading;
                    _staticLoadedListeners();
                    Log.d("data_loading", "did load from cache");
				}


                if (_container.getVersionUrlString() != null) {
                    StringRequest stringRequest = new StringRequest(_container.getVersionUrlString(), new Response.Listener<String>() {
                        @Override
                        public void onResponse(final String response) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    String versionString = (String)_model.getData(_container.getVersionKeyString(), "0");
                                    Integer versionCache = Integer.parseInt(versionString);
                                    Integer versionNet = 0;
                                    try {
                                        versionNet = Integer.parseInt(response);
                                    } catch (NumberFormatException ignored) {

                                    }

                                    if (versionNet > versionCache) {
                                        Log.d("data_loading", "exist new data, versionNet: " + versionNet.toString() + ", versionCache: " + versionCache.toString());
                                        _model.setData(_container.getVersionKeyString(), versionNet.toString(), true);
                                        _netLoad();
                                    } else {
                                        Log.d("data_loading", "not exist new data, versionNet: " + versionNet.toString() + ", versionCache: " + versionCache.toString());
                                    }
                                }
                            }).start();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("data_loading", "error loading version from internet");
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    _netLoad();
                                }
                            });
                        }
                    });
                    _queue.add(stringRequest);
                } else {
                    _netLoad();
                }
            }
		}).start();
	}
}
